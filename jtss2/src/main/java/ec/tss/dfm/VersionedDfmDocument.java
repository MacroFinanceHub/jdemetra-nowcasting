/*
 * Copyright 2013-2014 National Bank of Belgium
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

package ec.tss.dfm;

import ec.tss.documents.VersionedDocument;

/**
 *
 * @author Jean Palate
 */
public class VersionedDfmDocument extends VersionedDocument<DfmDocument>{

    @Override
    protected DfmDocument newDocument() {
        return new DfmDocument(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected DfmDocument restore(DfmDocument document) {
        document.setLocked(false);
        document.unfreezeTs();
        return document;
    }

    @Override
    protected DfmDocument archive(DfmDocument document) {
        document.setLocked(true);
        document.freezeTs();
        return document;
    }
}
