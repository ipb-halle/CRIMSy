import React from "react";
import { Material, dummyData } from "../../../data/dummyData";
import { colors } from "../../theme/designTokens";


interface TableViewProps {
  items: Material[];
}

const TableView: React.FC<TableViewProps> = ({ items }) => {

  return (
    <table
      style={{
        width: "100%",
        borderCollapse: "collapse",
        backgroundColor: colors.surface,
      }}
    >
      <thead>
        <tr style={{ backgroundColor: colors.primary, color: "white" }}>
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
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border}` }}>
                {m.materialId}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border}` }}>
                {project?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border}` }}>
                {owner?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border}` }}>
                {server?.name}
              </td>
              <td style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border}` }}>
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