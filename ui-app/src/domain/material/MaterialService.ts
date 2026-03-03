import { Material } from "../../data/dummyData";
import { MaterialRepository } from "../repositories/MaterialRepository";

export class MaterialService {
    constructor(private repository: MaterialRepository) { }

    getAllMaterials(): Material[] {
        return this.repository.getAll();
    }

    addMaterial(data: Omit<Material, "materialId">): Material {
        const existing = this.repository.getAll();
        const nextId =
            existing.length > 0
                ? Math.max(...existing.map((m) => m.materialId)) + 1
                : 1;

        const newMaterial: Material = {
            materialId: nextId,
            ...data,
        };

        this.repository.add(newMaterial);
        return newMaterial;
    }
}