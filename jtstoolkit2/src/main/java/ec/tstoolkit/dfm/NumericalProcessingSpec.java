/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.dfm;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;

/**
 *
 * @author palatej
 */
public class NumericalProcessingSpec implements IProcSpecification, Cloneable {
    
    public static enum Method{
        Lbfgs,
        LevenbergMarquardt
    }

    public static final int DEF_VERSION = 2, DEF_MAXITER = 1000, DEF_MAXSITER = 15,
            DEF_NITER = 5;
    public static final Boolean DEF_BLOCK = true, DEF_MIXED=true, DEF_IVAR=false;
    public static final String ENABLED = "enabled", MAXITER = "maxiter", MAXSITER = "maxsiter", NITER = "niter", 
            BLOCKITER = "blockiter", METHOD="method", EPS = "eps", MIXED="mixed", IVAR="ivar";
    public static final double DEF_EPS = 1e-9;
    private boolean enabled_;
    private int maxiter_ = DEF_MAXITER, maxsiter_ = DEF_MAXSITER, niter_ = DEF_NITER;
    private boolean block_ = DEF_BLOCK, mixed_=DEF_MIXED, ivar_=DEF_IVAR;
    private double eps_ = DEF_EPS;
    private Method method_ = Method.LevenbergMarquardt;

    public void setEnabled(boolean use) {
        enabled_ = use;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setMaxIter(int iter) {
        maxiter_ = iter;
    }

    public int getMaxIter() {
        return maxiter_;
    }

    public void setMaxInitialIter(int iter) {
        maxsiter_ = iter;
    }

    public int getMaxInitialIter() {
        return maxsiter_;
    }

    public void setMaxIntermediateIter(int iter) {
        niter_ = iter;
    }

    public int getMaxIntermediateIter() {
        return niter_;
    }
    
    public boolean isBlockIterations(){
        return block_;
    }
    
    public void setBlockIterations(boolean b){
        block_=b;
    }
    
    public boolean isMixedEstimation(){
        return mixed_;
    }
    
    public void setMixedEstimation(boolean b){
        mixed_=b;
    }

    public boolean isIndependentVarShocks(){
        return ivar_;
    }
    
    public void setIndependentVarShocks(boolean b){
        ivar_=b;
    }

    public Method getMethod(){
        return method_;
    }
    
    public void setMethod(Method m){
        method_=m;
    }
    
    public double getPrecision(){
        return eps_;
    }

    public void setPrecision(double eps){
        eps_=eps;
    }
    
    @Override
    public NumericalProcessingSpec clone() {
        try {
            return (NumericalProcessingSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ENABLED, enabled_);
        if (block_ != DEF_BLOCK || verbose) {
            info.set(BLOCKITER, block_);
        }
        if (mixed_ != DEF_MIXED || verbose) {
            info.set(MIXED, mixed_);
        }
        if (mixed_ != DEF_IVAR || verbose) {
            info.set(IVAR, ivar_);
        }
        if (eps_ != DEF_EPS || verbose) {
            info.set(EPS, eps_);
        }
        if (maxiter_ != DEF_MAXITER || verbose) {
            info.set(MAXITER, maxiter_);
        }
        if (maxsiter_ != DEF_MAXSITER || verbose) {
            info.set(MAXSITER, maxsiter_);
        }
        if (niter_ != DEF_NITER || verbose) {
            info.set(NITER, niter_);
        }
        info.set(METHOD, method_.name());
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (info == null) {
            return true;
        }
        Boolean enabled = info.get(ENABLED, Boolean.class);
        if (enabled != null) {
            enabled_ = enabled;
        }
        Boolean block = info.get(BLOCKITER, Boolean.class);
        if (block != null) {
            block_ = block;
        }
        Boolean mixed = info.get(MIXED, Boolean.class);
        if (mixed != null) {
            mixed_ = mixed;
        }
        Boolean ivar = info.get(IVAR, Boolean.class);
        if (ivar != null) {
            ivar_ = ivar;
        }
        Integer ni = info.get(MAXITER, Integer.class);
        if (ni != null) {
            maxiter_ = ni;
        }
        ni = info.get(MAXSITER, Integer.class);
        if (ni != null) {
            maxsiter_ = ni;
        }
        ni = info.get(NITER, Integer.class);
        if (ni != null) {
            niter_ = ni;
        }
        Double eps = info.get(EPS, Double.class);
        if (eps != null) {
            eps_ = eps;
        }
        String m=info.get(METHOD, String.class);
        if (m != null)
            method_=Method.valueOf(m);
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof NumericalProcessingSpec && equals((NumericalProcessingSpec) obj));
    }

    public boolean equals(NumericalProcessingSpec obj) {
        return obj.enabled_ == enabled_ && obj.block_ == block_ && obj.mixed_ == mixed_
                && obj.ivar_== ivar_ && obj.eps_ == eps_ && obj.method_ == method_
                && obj.maxiter_ == maxiter_ && obj.maxsiter_ == obj.maxsiter_ && obj.niter_ == niter_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.enabled_ ? 1 : 0);
        hash = 17 * hash + this.maxiter_;
        hash = 17 * hash + this.maxsiter_;
        hash = 17 * hash + this.niter_;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.eps_) ^ (Double.doubleToLongBits(this.eps_) >>> 32));
        return hash;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, MAXITER), Integer.class);
        dic.put(InformationSet.item(prefix, MAXSITER), Integer.class);
        dic.put(InformationSet.item(prefix, NITER), Integer.class);
        dic.put(InformationSet.item(prefix, BLOCKITER), Boolean.class);
        dic.put(InformationSet.item(prefix, MIXED), Boolean.class);
        dic.put(InformationSet.item(prefix, IVAR), Boolean.class);
        dic.put(InformationSet.item(prefix, EPS), Double.class);
        dic.put(InformationSet.item(prefix, METHOD), String.class);
    }
}
