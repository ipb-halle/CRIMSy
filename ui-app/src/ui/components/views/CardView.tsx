// src/ui/components/views/CardView.tsx
import React from "react";

interface CardViewProps {
  items: any[];
}

export const CardView: React.FC<CardViewProps> = ({ items }) => {
  if (!items.length) return <div>No items to display</div>;

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
        gap: "1rem",
      }}
    >
      {items.map((item, idx) => (
        <div
          key={idx}
          style={{
            border: "1px solid #029ACF",
            borderRadius: "6px",
            padding: "0.75rem",
            boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            background: "#fff",
          }}
        >
          {Object.entries(item).map(([key, value]) => (
            <div key={key}>
              <strong>{key}:</strong> {String(value)}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};
