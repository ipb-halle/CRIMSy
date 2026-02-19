// src/ui/components/views/ViewModeToggle.tsx
import React from "react";

interface Props {
  viewMode: "card" | "table";
  onToggle: (mode: "card" | "table") => void;
  style?: React.CSSProperties; // <-- add this
}

export const ViewModeToggle: React.FC<Props> = ({ viewMode, onToggle, style }) => {
  return (
    <div style={{ display: "flex", gap: "0.5rem", ...style }}>
      <button
        onClick={() => onToggle("card")}
        style={{
          padding: "0.5rem 1rem",
          background: viewMode === "card" ? "#029ACF" : "#f0f0f0",
          color: viewMode === "card" ? "white" : "black",
          border: "1px solid #029ACF",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        Card View
      </button>
      <button
        onClick={() => onToggle("table")}
        style={{
          padding: "0.5rem 1rem",
          background: viewMode === "table" ? "#029ACF" : "#f0f0f0",
          color: viewMode === "table" ? "white" : "black",
          border: "1px solid #029ACF",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        Table View
      </button>
    </div>
  );
};
