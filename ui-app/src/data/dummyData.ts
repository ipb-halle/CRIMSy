export interface Project {
  id: number;
  name: string;
}

export interface User {
  id: number;
  name: string;
}

export interface MaterialIndex {
  type: string;
  value: string;
}

export interface Material {
  materialId: number;
  materialName: string;
  materialTypeId: number;
  projectId: number;
  ownerId: number;
  serverId: number;
  deactivated: boolean;
}

export interface Storage {
  materialId: number;
  storageClass: number;
}

export interface MaterialHazard {
  materialId: number;
  typeId: number;
}

export interface Item {
  id: number;
  materialId: number;
  containerId: number;
}

export interface Server {
  id: number;
  name: string;
}

export const dummyData = {
  materialTypes: [
    { id: 1, name: "Structure" },
    { id: 2, name: "Composition" },
    { id: 3, name: "Biomaterial" },
    { id: 4, name: "Consumable" },
    { id: 5, name: "Sequence" },
    { id: 6, name: "Tissue" },
    { id: 7, name: "Taxonomy" },
  ],

  projects: [
    { id: 1, name: "Project A (Chemical)" },
    { id: 2, name: "Project B (IT)" },
    { id: 3, name: "Project C (Finance)" },
    { id: 4, name: "Project D (Biological)" },
    { id: 5, name: "Project E (Biocheical)" },
  ],

  usersGroups: [
    { id: 1, name: "Admin", memberType: "A" },
    { id: 2, name: "Alice", memberType: "U" },
    { id: 3, name: "Bob", memberType: "U" },
    { id: 4, name: "Charlie", memberType: "U" },
  ],

  storageClasses: [
    { id: 1, name: "1" },
    { id: 2, name: "2B" },
    { id: 3, name: "2C" },
    { id: 4, name: "3" },
  ],

  hazards: [
    { id: 1, name: "GHS07" },
    { id: 2, name: "HS" },
    { id: 3, name: "PS" },
    { id: 4, name: "GMO" },
  ],

  containers: [
    { id: 1, label: "ROOM 101", type: "ROOM" },
    { id: 2, label: "FREEZER A", type: "FREEZER" },
    { id: 3, label: "CUPBOARD 1", type: "CUPBOARD" },
  ],

  materials: [
    { materialId: 1, materialName: "benzin", materialTypeId: 4, projectId: 2, ownerId: 1, serverId: 3, deactivated: false },
    { materialId: 2, materialName: "sulfat", materialTypeId: 2, projectId: 1, ownerId: 2, serverId: 2, deactivated: false },
    { materialId: 3, materialName: "oil", materialTypeId: 3, projectId: 3, ownerId: 1, serverId: 3, deactivated: true },
    { materialId: 4, materialName: "water", materialTypeId: 7, projectId: 4, ownerId: 3, serverId: 1, deactivated: true },
    { materialId: 5, materialName: "carbon", materialTypeId: 4, projectId: 5, ownerId: 1, serverId: 3, deactivated: true },
    { materialId: 6, materialName: "gas", materialTypeId: 2, projectId: 4, ownerId: 2, serverId: 1, deactivated: true },
  ] as Material[],

  storages: [
    { materialId: 1, storageClass: 1 },
    { materialId: 2, storageClass: 3 },
    { materialId: 3, storageClass: 2 },
    { materialId: 4, storageClass: 3 },
    { materialId: 6, storageClass: 4 },
    { materialId: 5, storageClass: 1 },
  ] as Storage[],

  materialHazards: [
    { materialId: 1, typeId: 3 },
    { materialId: 2, typeId: 2 },
    { materialId: 3, typeId: 4 },
    { materialId: 5, typeId: 2 },
    { materialId: 3, typeId: 1 },
    { materialId: 4, typeId: 3 },
  ] as MaterialHazard[],

  items: [
    { id: 1, materialId: 1, containerId: 1 },
    { id: 2, materialId: 2, containerId: 2 },
    { id: 3, materialId: 3, containerId: 3 },
  ] as Item[],

  servers: [
    { id: 1, name: "Institute A Cloud" },
    { id: 2, name: "Institute B Cloud" },
    { id: 3, name: "Local Lab Server" },
  ] as Server[],
};

export default dummyData;