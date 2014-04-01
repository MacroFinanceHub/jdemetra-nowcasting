/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.dfm;

import ec.tstoolkit.algorithm.IProcessingHookProvider;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.mssf2.IMSsfData;
import ec.tstoolkit.mssf2.MSsfAlgorithm;
import ec.tstoolkit.mssf2.MSsfFunction;
import ec.tstoolkit.mssf2.MSsfFunctionInstance;
import ec.tstoolkit.mssf2.MultivariateSsfData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class DfmEstimator implements IDfmEstimator {

    private static final String SIMPLIFIED = "Optimizing simplified model",
            MSTEP = "Optimizing measurements", VSTEP = "Optimizing Var model", ALL = "Optimizing all parameters";
    private int maxiter_ = 500;
    private boolean converged_;
    private final IFunctionMinimizer min_;
    private int nstart_ = 15, nnext_ = 5;
    private TsDomain idom_;
    private boolean useBlockIterations_ = true, mixed_ = true;
    private Likelihood ll_;
    private DataBlock factors_;

    public DfmEstimator() {
        min_ = new ProxyMinimizer(new LevenbergMarquardtMethod());
    }

    public DfmEstimator(IFunctionMinimizer min) {
        min_ = min.exemplar();
    }

    public TsDomain getEstimationDomain() {
        return idom_;
    }

    public void setEstimationDomain(TsDomain dom) {
        idom_ = dom;
    }

    public int getMaxIter() {
        return maxiter_;
    }

    public int getMaxInitialIter() {
        return nstart_;
    }

    public void setMaxInitialIter(int n) {
        nstart_ = n;
    }

    public int getMaxIntermediateIter() {
        return nnext_;
    }

    public void setMaxIter(int iter) {
        maxiter_ = iter;
    }

    public void setMaxIntermediateIter(int n) {
        nnext_ = n;
    }

    public boolean isUsingBlockIterations() {
        return this.useBlockIterations_;
    }

    public void setUsingBlockIterations(boolean block) {
        this.useBlockIterations_ = block;
    }

    public boolean isMixedMethod() {
        return mixed_;
    }

    public void setMixedMethod(boolean b) {
        mixed_ = b;
    }

    public boolean hasConverged() {
        return converged_;
    }

    private void setMessage(String msg) {

        if (min_ instanceof IProcessingHookProvider) {
            ((IProcessingHookProvider) min_).setHookMessage(msg);
        } else if (min_ instanceof ProxyMinimizer) {
            ISsqFunctionMinimizer core = ((ProxyMinimizer) min_).getCore();
            if (core instanceof IProcessingHookProvider) {
                ((IProcessingHookProvider) core).setHookMessage(msg);
            }
        }
    }

    @Override
    public boolean estimate(final DynamicFactorModel dfm, DfmInformationSet input) {
        converged_ = false;
        Matrix m = input.generateMatrix(idom_);
        MSsfAlgorithm algorithm = new MSsfAlgorithm();
        IMSsfData mdata = new MultivariateSsfData(m.subMatrix().transpose(), null);
        DfmMapping mapping;
        MSsfFunction fn;
        MSsfFunctionInstance pt;
        int niter = 0;
        DynamicFactorModel model = dfm.clone();
        model.normalize();
        try {
            if (nstart_ > 0) {
                setMessage(SIMPLIFIED);
                min_.setMaxIter(nstart_);
                SimpleDfmMapping smapping = new SimpleDfmMapping(model);
                smapping.validate(model);
                fn = new MSsfFunction(mdata, smapping, algorithm);
                min_.minimize(fn, fn.evaluate(smapping.map(model)));
                pt = (MSsfFunctionInstance) min_.getResult();
                double var = pt.getLikelihood().getSigma();
                DynamicFactorModel nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
                if (nmodel.isValid()) {
                    nmodel.rescaleVariances(var);
                    model = nmodel;
                }
            }
            if (useBlockIterations_) {
                min_.setMaxIter(nnext_);
                while (true) {
                    model.normalize();
                    mapping = new DfmMapping(model, true, false);
                    fn = new MSsfFunction(mdata, mapping, algorithm);
                    setMessage(VSTEP);
                    min_.minimize(fn, fn.evaluate(mapping.map(model)));
                    niter += min_.getIterCount();
                    pt = (MSsfFunctionInstance) min_.getResult();
                    DynamicFactorModel nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
                    double var = pt.getLikelihood().getSigma();
                    model = nmodel;
                    model.rescaleVariances(var);
                    model.normalize();
                    if (mixed_) {
                        DfmEM2 em = new DfmEM2(null);
                        em.setEstimateVar(false);
                        em.setMaxIter(nnext_);
                        em.initialize(model, input);
                    } else {
                        mapping = new DfmMapping(model, false, true);
                        fn = new MSsfFunction(mdata, mapping, algorithm);
                        setMessage(MSTEP);
                        min_.minimize(fn, fn.evaluate(mapping.map(model)));
                        niter += min_.getIterCount();
                        pt = (MSsfFunctionInstance) min_.getResult();
                        nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
                        var = pt.getLikelihood().getSigma();
                        model = nmodel;
                        model.rescaleVariances(var);
                        model.normalize();

                    }
                    mapping = new DfmMapping(model, false, false);
                    fn = new MSsfFunction(mdata, mapping, algorithm);
                    setMessage(ALL);
                    converged_ = min_.minimize(fn, fn.evaluate(mapping.map(model)))
                            && min_.getIterCount() < nnext_;
                    niter += min_.getIterCount();
                    pt = (MSsfFunctionInstance) min_.getResult();
                    nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
                    var = pt.getLikelihood().getSigma();
                    ll_ = pt.getLikelihood();
                    model = nmodel.clone();
                    model.rescaleVariances(var);
                    if (converged_ || niter >= maxiter_) {
                        break;
                    }
                }
            } else {
                model.normalize();
                mapping = new DfmMapping(model, false, false);
                fn = new MSsfFunction(mdata, mapping, algorithm);
                min_.setMaxIter(maxiter_);
                setMessage(ALL);
                converged_ = min_.minimize(fn, fn.evaluate(mapping.map(model)));
                pt = (MSsfFunctionInstance) min_.getResult();
                double var = pt.getLikelihood().getSigma();
                model = ((DynamicFactorModel.Ssf) pt.ssf).getModel().clone();
                model.rescaleVariances(var);
                ll_ = pt.getLikelihood();
            }
            return true;
        } catch (Exception err) {
            return false;
        } finally {
            model.normalize();
            dfm.copy(model);
            DfmMapping fmapping = new DfmMapping(model);
            IReadDataBlock mp = fmapping.parameters();
            IReadDataBlock up = min_.getResult().getParameters();
            factors_ = new DataBlock(mp.getLength());
            for (int i = 0; i < factors_.getLength(); ++i) {
                factors_.set(i, mp.get(i) / up.get(i));
            }
        }
    }

    @Override
    public Matrix getHessian() {
        Matrix h = min_.getCurvature();
        if (h != null && !isLogLikelihood()) {
            // we have to correct the hessian 
            int ndf = ll_.getN() - h.getRowsCount();
            h = h.times(.5 * ndf / min_.getObjective());
        }
        DataBlockIterator rows=h.rows(), cols=h.columns();
        DataBlock row=rows.getData(), col=cols.getData();
        do{
            row.mul(factors_);
        }while (rows.next());
        do{
            col.mul(factors_);
        }while (cols.next());
        return h;
    }

    @Override
    public DataBlock getGradient() {
        DataBlock grad = new DataBlock(min_.getGradient());
        // the 
        if (!isLogLikelihood()) {
            // we have to correct the gradient 
            int ndf = ll_.getN() - grad.getLength();
            grad.mul(-.5 * ndf / min_.getObjective());
        } else {
            grad.chs();
        }
        grad.mul(factors_);
        return grad;
    }

    public double getObjective() {
        return min_.getObjective();
    }

    public Likelihood geLikelihood() {
        return ll_;
    }

    public boolean isLogLikelihood() {
        MSsfAlgorithm algorithm = new MSsfAlgorithm();
        if (min_ instanceof ProxyMinimizer) {
            return false;
        } else {
            return !algorithm.isUsingSsq();
        }
    }
}
