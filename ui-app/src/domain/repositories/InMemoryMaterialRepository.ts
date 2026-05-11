import dummyData, { Material } from "../../data/dummyData";
import { MaterialRepository } from "./MaterialRepository";

class InMemoryMaterialRepository implements MaterialRepository {
    getAll(): Material[] {
        return dummyData.materials;
    }

    add(material: Material): void {
        dummyData.materials.push(material);
    }
}

export const materialRepository = new InMemoryMaterialRepository();