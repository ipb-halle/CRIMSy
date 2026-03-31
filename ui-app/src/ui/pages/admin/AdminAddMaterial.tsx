import React, { useState } from "react";
import { MaterialService } from "../../../domain/material/MaterialService";
import { materialRepository } from "../../../infrastructure/repositories/InMemoryMaterialRepository";
import { dummyData } from "../../../data/dummyData";
import { useTheme } from "../../../assets/css/theme/ThemeContext";
import { radius, spacing } from "../../../assets/css/theme/designTokens";

const materialService = new MaterialService(materialRepository);

const AdminAddMaterial: React.FC = () => {
    const { theme } = useTheme();

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
                padding: spacing.lg,
                maxWidth: 520,
                margin: "1rem auto",
                background: theme.surface,
                borderRadius: radius.md,
                border: `1px solid  ${theme.border}`,
                boxShadow: "0 4px 14px rgba(0,0,0,0.06)",
            }}
        >
            <h2
                style={{
                    marginBottom: spacing.md,
                    color: theme.textPrimary,
                }}
            >
                Add New Material (Admin)
            </h2>

            <form onSubmit={handleSubmit}>
                {/* Materila Name */}
                <label style={{ display: "block", marginBottom: spacing.sm }}>
                    <span style={{ fontWeight: 600, color: theme.textPrimary }}>Material Name</span>
                    <input
                        name="materialName"
                        value={form.materialName}
                        onChange={handleChange}
                        required
                        style={{
                            width: "100%",
                            padding: spacing.sm,
                            marginTop: "0.25rem",
                            borderRadius: radius.sm,
                            border: `1px solid ${theme.border}`,
                        }}
                    />
                </label>

                {/* Materila Type */}
                <label style={{ display: "block", marginBottom: spacing.sm }}>
                    <span style={{ fontWeight: 600, color: theme.textPrimary }}>Material Type</span>
                    <select
                        name="materialTypeId"
                        value={form.materialTypeId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: spacing.sm,
                            marginTop: "0.25rem",
                            borderRadius: radius.sm,
                            border: `1px solid ${theme.border}`,
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
                <label style={{ display: "block", marginBottom: spacing.sm }}>
                    <span style={{ fontWeight: 600, color: theme.textPrimary }}>Project</span>
                    <select
                        name="projectId"
                        value={form.projectId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: spacing.sm,
                            marginTop: "0.25rem",
                            borderRadius: radius.sm,
                            border: `1px solid ${theme.border}`,
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
                <label style={{ display: "block", marginBottom: spacing.sm }}>
                    <span style={{ fontWeight: 600, color: theme.textPrimary }}>Owner</span>
                    <select
                        name="ownerId"
                        value={form.ownerId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: spacing.sm,
                            marginTop: "0.25rem",
                            borderRadius: radius.sm,
                            border: `1px solid ${theme.border}`,
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
                <label style={{ display: "block", marginBottom: spacing.sm }}>
                    <span style={{ fontWeight: 600, color: theme.textPrimary }}>Server</span>
                    <select
                        name="serverId"
                        value={form.serverId}
                        onChange={handleChange}
                        style={{
                            width: "100%",
                            padding: spacing.sm,
                            marginTop: "0.25rem",
                            borderRadius: radius.sm,
                            border: `1px solid ${theme.border}`,
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
                        gap: spacing.sm,
                        marginBottom: spacing.md,
                        color: theme.textPrimary,
                    }}>
                    <input
                        type="checkbox"
                        name="deactivated"
                        checked={form.deactivated}
                        onChange={handleChange}
                    />
                    Deactivated
                </label>

                <button type="submit"
                    style={{
                        width: "100%",
                        padding: spacing.sm,
                        background: theme.primary,
                        color: "white",
                        borderRadius: radius.sm,
                        border: "none",
                        fontWeight: 600,
                        cursor: "pointer",
                    }}>
                    Add Material
                </button>
            </form>
        </div>
    );
};

export default AdminAddMaterial;