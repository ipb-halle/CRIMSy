// src/data/dummyData.ts

export const dummyData = {
  projects: [{ name: "Project A" }, { name: "Project B" }],
  users: [{ name: "Alice" }, { name: "Bob" }],
  materials: [
    { name: "Material 1", type: "Structure", project: "Project A", user: "Alice", container: "Box", storageClass: "Cold", hazard: "Low", deactivationStatus: "Active" },
    { name: "Material 2", type: "Sequence", project: "Project B", user: "Bob", container: "Shelf", storageClass: "Room", hazard: "Medium", deactivationStatus: "Inactive" },
    { name: "Material 3", type: "Biomaterial", project: "Project B", user: "Alice", container: "Cabinet", storageClass: "Frezer", hazard: "High", deactivationStatus: "Inactive" },
    { name: "Material 4", type: "Composition", project: "Project A", user: "Bob", container: "Cabinet", storageClass: "Room", hazard: "Low", deactivationStatus: "Active" },
  ],

  materialTypes: ["Structure", "Sequence", "Biomaterial", "Composition"],
  containers: ["Box", "Shelf", "Cabinet"],
  storageClasses: ["Cold", "Room", "Freezer"],
  hazards: ["Low", "Medium", "High"],
  deactivationStatus: ["Active", "Inactive"],
};
