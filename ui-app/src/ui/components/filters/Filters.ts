export interface Filters {
    projects?: string[];
    users?: string[];
    materials?: string[];
    materialTypes?: string[];
    containers?: string[];
    storageClasses?: string[];
    hazards?: string[];
    deactivationStatus?: string[];
}

export interface FilterGroup {
    key: keyof Filters;
    label: string;
    options: string[];
}