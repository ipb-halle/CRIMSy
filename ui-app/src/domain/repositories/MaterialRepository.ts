import { Material } from "../../data/dummyData";

export interface MaterialRepository {
    getAll(): Material[];
    add(material: Material): void;
}