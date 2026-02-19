export interface Filters {
  projectId?: number;
  ownerId?: number;
  materialTypeId?: number;
  containerId?: number;
  storageClassId?: number;
  hazardIds?: number[];
  deactivated?: boolean;
  searchText?: string;
}