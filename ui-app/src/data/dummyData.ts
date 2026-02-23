export interface Project {
  id: string;
  name: string;
}

export interface User {
  id: string;
  name: string;
}

export interface Material {
  id: string;
  name: string;
  type: string;
  project: string;
  user: string;
  container: string;
  storageClass: string;
  hazard: string;
  deactivationStatus: string;
}

export const dummyData = {
  projects: [
    { id: "p1", name: "Project A" },
    { id: "p2", name: "Project B" },
    { id: "p3", name: "Project C" }
  ] as Project[],

  users: [
    { id: "u1", name: "Alice" },
    { id: "u2", name: "Bob" },
    { id: "u3", name: "Charlie" }
  ] as User[],

  materials: [
    {
      id: "m1",
      name: "Material 1",
      type: "Structure",
      project: "Project A",
      user: "Alice",
      container: "Box",
      storageClass: "Cold",
      hazard: "Low",
      deactivationStatus: "Active"
    },
    {
      id: "m2",
      name: "Material 2",
      type: "Sequence",
      project: "Project B",
      user: "Bob",
      container: "Shelf",
      storageClass: "Room",
      hazard: "Medium",
      deactivationStatus: "Inactive"
    },
    {
      id: "m3",
      name: "Material 3",
      type: "Biomaterial",
      project: "Project B",
      user: "Alice",
      container: "Cabinet",
      storageClass: "Freezer",
      hazard: "High",
      deactivationStatus: "Inactive"
    },
    {
      id: "m4",
      name: "Material 4",
      type: "Composition",
      project: "Project A",
      user: "Bob",
      container: "Cabinet",
      storageClass: "Room",
      hazard: "Low",
      deactivationStatus: "Active"
    },
    {
      id: "m5",
      name: "Material 5",
      type: "Structure",
      project: "Project C",
      user: "Charlie",
      container: "Box",
      storageClass: "Cold",
      hazard: "Medium",
      deactivationStatus: "Active"
    },
    {
      id: "m5",
      name: "Material 6",
      type: "Sequence",
      project: "Project C",
      user: "Alice",
      container: "Shelf",
      storageClass: "Freezer",
      hazard: "High",
      deactivationStatus: "Inactive"
    },
  ] as Material[],

  materialTypes: ["Structure", "Sequence", "Biomaterial", "Composition"],
  containers: ["Box", "Shelf", "Cabinet"],
  storageClasses: ["Cold", "Room", "Freezer"],
  hazards: ["Low", "Medium", "High"],
  deactivationStatus: ["Active", "Inactive"],
};

export default dummyData;