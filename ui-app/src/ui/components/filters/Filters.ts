export interface Filters {
    materialTypeIds?: number[];
    projectIds?: number[];
    ownerIds?: number[];
    storageClassIds?: number[];
    hazardIds?: number[];
    containerIds?: number[];
    deactivated?: boolean[];
}

export interface FilterGroup {
    key: keyof Filters;
    label: string;
    options: { value: number | boolean; label: string }[];
}