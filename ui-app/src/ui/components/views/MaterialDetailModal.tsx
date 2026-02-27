import React from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { colors, radius, spacing } from "../../theme/designTokens";

interface Props {
  material: Material;
  onClose: () => void;
}

const MaterialDetailModal: React.FC<Props> = ({ material, onClose }) => {
  const hazards = dummyData.materialHazards
    .filter(h => h.materialId === material.materialId)
    .map(h => dummyData.hazards.find(d => d.id === h.typeId)?.name)
    .filter(Boolean);

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.45)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 1000,
      }}
    >
      <div
        style={{
          background: colors.surface,
          padding: spacing.lg,
          borderRadius: radius.md,
          width: "520px",
          maxWidth: "95%",
          boxShadow: "0 10px 35px rgba(0,0,0,0.2)",
          borderTop: "4px solid #00a651",
          animation: "fadeIn 0.25s ease",
        }}
      >
        <h2
          style={{ color: "#003c78", marginBottom: spacing.md }}>
          Material #{material.materialId}
        </h2>

        <div style={{ fontSize: "0.95rem", lineHeight: 1.6 }}>
          <p><strong>Project ID:</strong> {material.projectId}</p>
          <p><strong>Owner ID:</strong> {material.ownerId}</p>
          <p><strong>Server ID:</strong> {material.serverId}</p>
          <p><strong>Hazards:</strong> {hazards.join(", ") || "None"}</p>

        </div>

        <button
          onClick={onClose}
          style={{
            marginTop: spacing.md,
            padding: "0.5rem 1rem",
            background: "#00509e",
            color: "white",
            border: "none",
            borderRadius: radius.sm,
            cursor: "pointer",
          }}
        >
          Close
        </button>
      </div>
    </div>
  );
};

export default MaterialDetailModal;