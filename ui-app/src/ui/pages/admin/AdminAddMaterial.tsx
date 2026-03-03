import React, { useState } from "react";
import { MaterialService } from "../../../domain/material/MaterialService";
import { materialRepository } from "../../../infrastructure/repositories/InMemoryMaterialRepository";
import { dummyData } from "../../../data/dummyData";

const materialService = new MaterialService(materialRepository);

const AdminAddMaterial: React.FC = () => {
    const [form, setForm] = useState({
        materialName: "",
        materialTypeId: 1,
        projectId: 1,
        ownerId: 1,
        serverId: 1,
        deactivated: false,
    });

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const { name, value, type } = e.target;
        setForm({
            ...form,
            [name]:
                type === "checkbox"
                    ? (e.target as HTMLInputElement).checked
                    : value,
        });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        materialService.addMaterial({
            ...form,
            materialTypeId: Number(form.materialTypeId),
            projectId: Number(form.projectId),
            ownerId: Number(form.ownerId),
            serverId: Number(form.serverId),
        });

        alert("Material added successfully");

        setForm({
            materialName: "",
            materialTypeId: 1,
            projectId: 1,
            ownerId: 1,
            serverId: 1,
            deactivated: false,
        });
    };

    return (
        <div
            style={{
                padding: "1.5rem",
                maxWidth: "540px",
                margin: "1rem auto",
                background: "#fff",
                borderRadius: "12px",
                boxShadow: "0 4px 14px rgba(0,0,0,0.08)",
            }}
        >
            <h2 style={{ marginBottom: "1.25rem" }}>Add New Material (Admin)</h2>

            <form onSubmit={handleSubmit}>
                {/* Materila Name */}
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: 600 }}>Materila Name</span>
                    <input
                        name="materialName"
                        value={form.materialName}
                        onChange={handleChange}
                        required
                        style={{
                            width: "100%",
                            padding: "0.5rem",
                            marginTop: "0.25rem",
                            borderRadius: "6px",
                            border: "1px solid #d0d7de",
                        }}
                    />
                </label>

                {/* Materila Type */}
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: 600 }}>Materila Type</span>
                    <select
                        name="materialTypeId"
                        value={form.materialTypeId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: "0.5rem",
                            marginTop: "0.25rem",
                            borderRadius: "6px",
                            border: "1px solid #d0d7de",
                        }}
                    >
                        {dummyData.materialTypes.map((t) => (
                            <option key={t.id} value={t.id}>
                                {t.name}
                            </option>
                        ))}
                    </select>
                </label>

                {/* Project */}
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: 600 }}>Project</span>
                    <select
                        name="projectId"
                        value={form.projectId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: "0.5rem",
                            marginTop: "0.25rem",
                            borderRadius: "6px",
                            border: "1px solid #d0d7de",
                        }}
                    >
                        {dummyData.projects.map((p) => (
                            <option key={p.id} value={p.id}>
                                {p.name}
                            </option>
                        ))}
                    </select>
                </label>

                {/* Owner */}
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: 600 }}>Owner</span>
                    <select
                        name="ownerId"
                        value={form.ownerId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: "0.5rem",
                            marginTop: "0.25rem",
                            borderRadius: "6px",
                            border: "1px solid #d0d7de",
                        }}
                    >
                        {dummyData.usersGroups.map((u) => (
                            <option key={u.id} value={u.id}>
                                {u.name}
                            </option>
                        ))}
                    </select>
                </label>

                {/* Server */}
                <label style={{ display: "block", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: 600 }}>Server</span>
                    <select
                        name="serverId"
                        value={form.serverId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: "0.5rem",
                            marginTop: "0.25rem",
                            borderRadius: "6px",
                            border: "1px solid #d0d7de",
                        }}
                    >
                        {dummyData.servers.map((s) => (
                            <option key={s.id} value={s.id}>
                                {s.name}
                            </option>
                        ))}
                    </select>
                </label>

                {/* Deactivated */}
                <label
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "0.5rem",
                        marginTop: "0.25rem",
                        marginBottom: "1rem",
                    }}
                >
                    <input
                        type="checkbox"
                        name="deactivated"
                        checked={form.deactivated}
                        onChange={handleChange}
                    />
                    Deactivated
                </label>

                <button
                    type="submit"
                    style={{
                        width: "100%",
                        padding: "0.6rem",
                        background: "#00509d",
                        color: "white",
                        borderRadius: "6px",
                        border: "none",
                        fontWeight: 600,
                        cursor: "pointer",
                    }}
                >
                    Add Material
                </button>
            </form>
        </div>
    );
};

export default AdminAddMaterial;