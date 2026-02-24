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
            selected.includes(material.ownerId),
    },
    {
        key: "storageClassIds",
        matches: (material: Material, selected: number[]) => {
            const storage = dummyData.storages.find(
                s => s.materialId === material.materialId
            );
            return storage ? selected.includes(storage.storageClass) : false;
        },
    },
    {
        key: "hazardIds",
        matches: (material: Material, selected: number[]) => {
            const hazards = dummyData.materialHazards
                .filter(h => h.materialId === material.materialId)
                .map(h => h.typeId);
            return hazards.some(h => selected.includes(h));
        },
    },
    {
        key: "containerIds",
        matches: (material: Material, selected: number[]) => {
            const item = dummyData.items.find(i => i.materialId === material.materialId);
            return item ? selected.includes(item.containerId) : false;
        },
    },
    {
        key: "serverIds",
        matches: (material: Material, selected: number[]) =>
            selected.includes(material.serverId),
    },
    {
        key: "deactivated",
        matches: (material: Material, selected: boolean[]) =>
            selected.includes(material.deactivated),
    },
];