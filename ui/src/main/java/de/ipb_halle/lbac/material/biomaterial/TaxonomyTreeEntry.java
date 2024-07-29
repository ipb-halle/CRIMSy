/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author fabian
 */
public class TaxonomyTreeEntry extends Taxonomy {

    private int parentId;

    public TaxonomyTreeEntry(int id, String name, List<Taxonomy> hierarchy, User owner, TaxonomyLevel level, int parentId) {
        super(id,
                Arrays.asList(new MaterialName(name, "en", 0)),
                new HazardInformation(),
                new StorageInformation(),
                hierarchy,
                owner,
                new Date());
        this.level = level;
        this.parentId = parentId;
    }

}
