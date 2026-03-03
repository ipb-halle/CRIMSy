import React, { useState } from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { colors, radius, spacing } from "../../../assets/css/theme/designTokens";
import MaterialDetailModal from "./MaterialDetailModal";

interface CardViewProps {
  items: Material[];
}

const CardView: React.FC<CardViewProps> = ({ items }) => {
  const [selectedMaterial, setSelectedMaterial] = useState<Material | null>(null);

  return (
    <>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
          gap: "1rem",
        }}
      >

        {items.map(material => {
          const name = dummyData.materials.find(m => m.materialName === material.materialName);
          const project = dummyData.projects.find(p => p.id === material.projectId);
          const owner = dummyData.usersGroups.find(u => u.id === material.ownerId);
          const server = dummyData.servers.find(s => s.id === material.serverId);

          return (
            <div
              key={material.materialId}
              onClick={() => setSelectedMaterial(material)}
              style={{
                border: `1px solid ${colors.border}`,
                borderRadius: radius.md,
                padding: spacing.md,
                background: colors.surface,
                cursor: "pointer",
                transition: "transform 0.15s ease",
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: "0.5rem" }}>
                {material.materialName.toUpperCase()}
              </div>

              <div><strong>Project:</strong> {project?.name}</div>
              <div><strong>Owner:</strong> {owner?.name}</div>
              <div><strong>Server:</strong> {server?.name}</div>

              <div
                style={{
                  marginTop: "0.75rem",
                  display: "inline-block",
                  padding: "0.25rem 0.6rem",
                  borderRadius: "12px",
                  fontSize: "0.75rem",
                  fontWeight: 600,
                  backgroundColor: material.deactivated
                    ? colors.danger
                    : colors.success,
                  color: "white",
                }}
              >
                {material.deactivated ? "Deactivated" : "Active"}
              </div>
            </div>
          );
        })}
      </div >

      {selectedMaterial && (
        <MaterialDetailModal
          material={selectedMaterial}
          onClose={() => setSelectedMaterial(null)}
        />
      )
      }
    </>
  );
};

export default CardView;