import React from "react";

const ImageView: React.FC<{ items: any[] }> = ({ items }) => {
  return (
    <div style={{ display: "flex", gap: "1rem" }}>
      {items.map((m) => (
        <div key={m.id} style={{ textAlign: "center" }}>
          <div
            style={{
              width: "100px",
              height: "100px",
              background: "#ddd",
              marginBottom: "0.5rem",
            }}
          />
          <div>{m.label}</div>
        </div>
      ))}
    </div>
  );
};

export default ImageView;
