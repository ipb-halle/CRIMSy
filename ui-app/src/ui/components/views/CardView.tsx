import React, { useState } from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { useTheme } from "../../theme/ThemeContext";
import MaterialDetailModal from "./MaterialDetailModal";

interface CardViewProps {
  items: Material[];
}

const CardView: React.FC<CardViewProps> = ({ items }) => {
  const [selectedMaterial, setSelectedMaterial] = useState<Material | null>(null);

  const { theme } = useTheme();

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
          const project = dummyData.projects.find(p => p.id === material.projectId);
          const owner = dummyData.usersGroups.find(u => u.id === material.ownerId);
          const server = dummyData.servers.find(s => s.id === material.serverId);

          return (
            <div
              key={material.materialId}
              onClick={() => setSelectedMaterial(material)}
              style={{
                border: `1px solid ${theme.border}`,
                borderRadius: "10px",
                padding: "1rem",
                boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
                background: theme.surface,
                cursor: "pointer",
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: "0.5rem" }}>
                Material #{material.materialId}
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
                    ? theme.danger
                    : theme.success,
                  color: material.deactivated
                    ? theme.danger
                    : theme.success,
                }}
              >
                {material.deactivated ? "Deactivated" : "Active"}
              </div>
            </div>
          );
        })}
      </div>

      {selectedMaterial && (
        <MaterialDetailModal
          material={selectedMaterial}
          onClose={() => setSelectedMaterial(null)}
        />
      )}
    </>
  );
};

export default CardView;