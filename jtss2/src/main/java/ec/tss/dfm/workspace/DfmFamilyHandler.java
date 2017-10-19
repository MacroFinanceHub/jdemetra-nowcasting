/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tss.dfm.workspace;

import ec.demetra.workspace.WorkspaceFamily;
import ec.demetra.workspace.file.FileFormat;
import ec.demetra.workspace.file.spi.FamilyHandler;
import ec.demetra.workspace.file.util.InformationSetSupport;
import ec.tss.dfm.VersionedDfmDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mats Maggi
 */
@ServiceProvider(service = FamilyHandler.class)
public class DfmFamilyHandler implements FamilyHandler {

    private static final WorkspaceFamily SA_DOC_DFM = WorkspaceFamily
            .parse("Nowcasting@documents@DynamicFactorModel");

    @lombok.experimental.Delegate
    private final FamilyHandler delegate = InformationSetSupport
            .of(VersionedDfmDocument::new, "DfmDoc")
            .asHandler(SA_DOC_DFM, FileFormat.GENERIC);
}
