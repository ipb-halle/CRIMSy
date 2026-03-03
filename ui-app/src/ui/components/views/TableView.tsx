import React from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { useTheme } from "../../../assets/css/theme/ThemeContext";

interface TableViewProps {
  items: Material[];
}

const TableView: React.FC<TableViewProps> = (
  { items }
) => {

  const { theme } = useTheme();

  return (
    <table
      style={{
        width: "100%",
        borderCollapse: "collapse",
        backgroundColor: theme.surface,
      }}
    >
      <thead>
        <tr style={{ backgroundColor: theme.primary, color: "white" }}>
          <th style={{ padding: "0.75rem" }}>ID</th>
          <th style={{ padding: "0.75rem" }}>Name</th>
          <th style={{ padding: "0.75rem" }}>Project</th>
          <th style={{ padding: "0.75rem" }}>Owner</th>
          <th style={{ padding: "0.75rem" }}>Server</th>
          <th style={{ padding: "0.75rem" }}>Status</th>
        </tr>
      </thead>
      <tbody>
        {items.map(material => {
          const name = dummyData.materials.find(m => m.materialName === material.materialName);
          const project = dummyData.projects.find(p => p.id === material.projectId);
          const owner = dummyData.usersGroups.find(u => u.id === material.ownerId);
          const server = dummyData.servers.find(s => s.id === material.serverId);

          return (
            <tr key={material.materialId}>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {material.materialId}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {name?.materialName}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {project?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {owner?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {server?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {material.deactivated ? "Deactivated" : "Active"}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
};

export default TableView;