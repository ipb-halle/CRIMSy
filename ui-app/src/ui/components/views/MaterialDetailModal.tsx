import React from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { colors } from "../../theme/designTokens";

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
        background: "rgba(0,0,0,0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <div
        style={{
          background: colors.surface,
          padding: "2rem",
          borderRadius: "10px",
          width: "500px",
        }}
      >
        <h2>Material #{material.materialId}</h2>

        <p><strong>Project ID:</strong> {material.projectId}</p>
        <p><strong>Owner ID:</strong> {material.ownerId}</p>
        <p><strong>Server ID:</strong> {material.serverId}</p>
        <p><strong>Hazards:</strong> {hazards.join(", ") || "None"}</p>

        <button
          onClick={onClose}
          style={{
            marginTop: "1rem",
            padding: "0.5rem 1rem",
            background: colors.primary,
            color: "white",
            border: "none",
            borderRadius: "6px",
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