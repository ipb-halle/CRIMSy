package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.material.common.MaterialNameValidator;
import de.ipb_halle.lbac.material.common.MaterialSaver;
import de.ipb_halle.lbac.material.common.MaterialValidator;
import de.ipb_halle.lbac.material.common.history.IMaterialComparator;
import de.ipb_halle.lbac.material.common.history.StructureComparator;
import de.ipb_halle.lbac.material.common.service.MaterialFactory;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;

/**
 *
 * @author fmauz
 */
public class StructureFactory implements MaterialFactory {

    private static final long serialVersionUID = 1L;

    StructureInformationSaver saver = new StructureInformationSaver();

    @Override
    public MaterialSaver createSaver() {
        return saver;
    }

    @Override
    public MaterialLoader createLoader() {
        return new StructureLoader();
    }

    public void setSaver(StructureInformationSaver saver) {
        this.saver = saver;
    }

    @Override
    public IMaterialComparator createComparator() {
        return new StructureComparator();
    }

    @Override
    public MaterialValidator createValidator() {
       return new MaterialNameValidator();
    }

}
