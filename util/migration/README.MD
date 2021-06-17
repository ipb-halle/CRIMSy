#Checklist for Migration of InhouseDB

* check all file names
* review owner_id, project_id and ACList-Id for the import process
* add Mol_ID and IPBCode to indextypes table
* add Plants, Fungi and an "Unknown" taxon to the materials / taxonomy tables
* replace the last column in the classes table with the material Ids of these taxa 
* add an unknown compound to the materials tables
* update config.json with the materialIds of unknown taxa and compounds
* unless fixed upstream: update the container type "GLAS_VIAL" to "GLASS_VIAL" (or rework the entire container type part)
* if necessary: adjust container naming scheme, geometries and flags

