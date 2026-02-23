import { Filters } from "./Filters";
import { Material } from "../../../data/dummyData";

export interface FilterRule {
    key: keyof Filters;
    matches: (item: Material, selected: string[]) => boolean;
}

export const filterRuls: FilterRule[] = [
    {
        key: "materials",
        matches: (item, selected) => selected.includes(item.name),
    },
    {
        key: "materialTypes",
        matches: (item, selected) => selected.includes(item.type),
    },
    {
        key: "projects",
        matches: (item, selected) => selected.includes(item.project),
    },
    {
        key: "users",
        matches: (item, selected) => selected.includes(item.user),
    },
    {
        key: "containers",
        matches: (item, selected) => selected.includes(item.container),
    },
    {
        key: "storageClasses",
        matches: (item, selected) => selected.includes(item.storageClass),
    },
    {
        key: "hazards",
        matches: (item, selected) => selected.includes(item.hazard),
    },
    {
        key: "deactivationStatus",
        matches: (item, selected) => selected.includes(item.deactivationStatus),
    },
];