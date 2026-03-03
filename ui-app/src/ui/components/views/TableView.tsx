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
          <th style={{ padding: "0.75rem" }}>Project</th>
          <th style={{ padding: "0.75rem" }}>Owner</th>
          <th style={{ padding: "0.75rem" }}>Server</th>
          <th style={{ padding: "0.75rem" }}>Status</th>
        </tr>
      </thead>
      <tbody>
        {items.map(m => {
          const project = dummyData.projects.find(p => p.id === m.projectId);
          const owner = dummyData.usersGroups.find(u => u.id === m.ownerId);
          const server = dummyData.servers.find(s => s.id === m.serverId);

          return (
            <tr key={m.materialId}>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${theme.border}` }}>
                {m.materialId}
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
                {m.deactivated ? "Deactivated" : "Active"}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
};

export default TableView;