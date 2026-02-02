import { useState } from "react";

export const SearchDropdown = () => {
  const [open, setOpen] = useState(false);

  const navigate = (path: string) => {
    window.location.href = path;
  };

  return (
    <div style={{ position: "relative", display: "inline-block" }}>
      <button
        onClick={() => setOpen(!open)}
        style={{
          padding: "8px 16px",
          border: "1px solid #1976d2",
          background: "#1976d2",
          color: "white",
          borderRadius: "4px",
          cursor: "pointer"
        }}
      >
        Search ▾
      </button>

      {open && (
        <div
          style={{
            position: "absolute",
            top: "110%",
            left: 0,
            background: "white",
            border: "1px solid #ccc",
            borderRadius: "4px",
            boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
            minWidth: "180px",
            zIndex: 1000
          }}
        >
          <DropdownItem
            label="Normal Search"
            onClick={() => navigate("/crimsy/default")}
          />
          <DropdownItem
            label="Word Cloud Search"
            onClick={() => navigate("/crimsy/wordCloud2")}
          />
          <DropdownItem
            label="Sequence Search"
            onClick={() => navigate("/crimsy/sequence/sequenceSearch")}
          />
        </div>
      )}
    </div>
  );
};

const DropdownItem = ({
  label,
  onClick
}: {
  label: string;
  onClick: () => void;
}) => (
  <div
    onClick={onClick}
    style={{
      padding: "10px",
      cursor: "pointer",
      borderBottom: "1px solid #eee"
    }}
    onMouseEnter={(e) =>
      (e.currentTarget.style.backgroundColor = "#f0f6ff")
    }
    onMouseLeave={(e) =>
      (e.currentTarget.style.backgroundColor = "white")
    }
  >
    {label}
  </div>
);
