import { dummyData } from "../../../data/dummyData";
import { FilterGroup } from "./Filters";

export const filterGroups: FilterGroup[] = [
    { key: "projects", label: "Projects", options: dummyData.projects.map(p => p.name) },
    { key: "users", label: "Users", options: dummyData.users.map(u => u.name) },
    { key: "materials", label: "Material Types", options: dummyData.materials.map(m => m.name) },
    { key: "materialTypes", label: "Material Types", options: dummyData.materialTypes },
    { key: "containers", label: "containers", options: dummyData.containers },
    { key: "storageClasses", label: "Storage Classes", options: dummyData.storageClasses },
    { key: "hazards", label: "Hazards", options: dummyData.hazards },
    { key: "deactivationStatus", label: "Deactivation Status", options: dummyData.deactivationStatus },
]
