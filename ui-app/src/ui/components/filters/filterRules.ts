import { Filters } from "./Filters";
import { dummyData, Material } from "../../../data/dummyData";

export const filterRuls = [
    {
        key: "materialTypeIds",
        matches: (material: Material, selected: number[]) =>
            selected.includes(material.materialTypeId),
    },
    {
        key: "projectIds",
        matches: (material: Material, selected: number[]) =>
            selected.includes(material.projectId),
    },
    {
        key: "ownerIds",
        matches: (material: Material, selected: number[]) =>
            selected.includes(material.owner_id),
    },
    {
        key: "storageClasses",
        matches: (material: Material, selected: number[]) => {
            const storage = dummyData.storages.find(s => s.materialid === material.materialid);
            return storage ? selected.includes(storage.storageclass) : false;
        },
    },
    {
        key: "hazardIds",
        matches: (material: Material, selected: number[]) => {
            const hazards = dummyData.material_hazards
                .filter(h => h.materialid === material.materialid)
                .map(h => h.typeid);
            return hazards.some(h => selected.includes(h));
        },
    },

    {
        key: "containerIds",
        matches: (material: Material, selected: number[]) => {
            const item = dummyData.items.find(i => i.materialid === material.materialid);
            return item ? selected.includes(item.containerid) : false;
        },
    },
    {
        key: "deactivated",
        matches: (material: Material, selected: boolean[]) =>
            selected.includes(material.deactivated),
    },
];