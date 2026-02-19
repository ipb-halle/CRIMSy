// src/ui/components/views/TableView.tsx
import React from "react";

interface TableViewProps {
  items: any[];
}

export const TableView: React.FC<TableViewProps> = ({ items }) => {
  if (!items.length) return <div>No items to display</div>;

  return (
    <div style={{ overflowX: "auto" }}>
      <table style={{ width: "100%", borderCollapse: "collapse", minWidth: "600px" }}>
        <thead style={{ background: "#029ACF", color: "white" }}>
          <tr>
            {Object.keys(items[0]).map((key) => (
              <th
                key={key}
                style={{ padding: "0.5rem", border: "1px solid #ccc", textAlign: "left" }}
              >
                {key}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {items.map((item, idx) => (
            <tr key={idx} style={{ background: idx % 2 === 0 ? "#f9f9f9" : "#fff" }}>
              {Object.values(item).map((val, i) => (
                <td key={i} style={{ padding: "0.5rem", border: "1px solid #ccc" }}>
                  {String(val)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
