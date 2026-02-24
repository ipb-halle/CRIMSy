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
  materialid: number;
  materialTypeId: number;
  projectId: number;
  owner_id: number;
  deactivated: boolean;
}

export interface Storage {
  materialid: number;
  storageclass: number;
}

export interface MaterialHazard {
  materialid: number;
  typeid: number;
}

export interface Item {
  id: number;
  materialid: number;
  containerid: number;
}

export const dummyData = {
  materialtypes: [
    { id: 1, name: "STRUCTURE" },
    { id: 2, name: "MATERIAL_COMPOSITION" },
    { id: 3, name: "BIOMATERIAL" },
    { id: 4, name: "CONSUMABLE" },
    { id: 5, name: "SEQUENCE" },
    { id: 6, name: "TISSUE" },
    { id: 7, name: "TAXONOMY" },
  ],

  projects: [
    { id: 1, name: "Project A (Chem Analysis)" },
    { id: 2, name: "Project B (Biomaterials)" },
    { id: 3, name: "Project C (Sequencing)" },
  ],

  usersGroups: [
    { id: 1, name: "Alice", memberType: "U" },
    { id: 2, name: "Bob", memberType: "U" },
    { id: 3, name: "Charlie", memberType: "U" },
  ],

  storageClasses: [
    { id: 1, name: "1" },
    { id: 2, name: "2A" },
    { id: 3, name: "2B" },
  ],

  hazards: [
    { id: 7, name: "GHS07" },
    { id: 10, name: "HS" },
    { id: 11, name: "PS" },
  ],

  containers: [
    { id: 1, label: "ROOM 101", type: "ROOM" },
    { id: 2, label: "FREEZER A", type: "FREEZER" },
    { id: 3, label: "CUPBOARD 1", type: "CUPBOARD" },
  ],

  materials: [
    { materialid: 1, materialTypeId: 1, projectId: 1, owner_id: 1, deactivated: false },
    { materialid: 2, materialTypeId: 5, projectId: 2, owner_id: 2, deactivated: false },
    { materialid: 3, materialTypeId: 3, projectId: 2, owner_id: 1, deactivated: true },
  ] as Material[],

  storages: [
    { materialid: 1, storageclass: 1 },
    { materialid: 2, storageclass: 3 },
    { materialid: 3, storageclass: 2 },
  ] as Storage[],

  material_hazards: [
    { materialid: 1, typeid: 1 },
    { materialid: 2, typeid: 2 },
    { materialid: 3, typeid: 6 },
  ] as MaterialHazard[],

  items: [
    { id: 1, materialid: 1, containerid: 1 },
    { id: 2, materialid: 2, containerid: 2 },
    { id: 3, materialid: 3, containerid: 3 },
  ] as Item[],
};

export default dummyData;