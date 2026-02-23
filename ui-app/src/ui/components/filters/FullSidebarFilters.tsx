import React, { useState, useRef, useEffect, CSSProperties } from "react";
import { Filters } from "./Filters";
import { filterGroups } from "./filterConfig";

interface Props {
  onFilterChange: (filters: Filters) => void;
  style?: CSSProperties;
  isDropdown?: boolean;
}

const FullSidebarFilters: React.FC<Props> = ({
  onFilterChange,
  style,
  isDropdown
}) => {
  const [filters, setFilters] = useState<Filters>({});
  const [open, setOpen] = useState(!isDropdown);
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({});
  const dropdownRef = useRef<HTMLDivElement | null>(null);

  const handleToggle = (key: keyof Filters, value: string) => {
    const current = filters[key] || [];
    const updated = current.includes(value)
      ? current.filter((v) => v !== value)
      : [...current, value];

    const newFilters = { ...filters, [key]: updated };
    setFilters(newFilters);
    onFilterChange(newFilters);

    setOpenSections((prev) => ({ ...prev, [key]: true }));
  };

  // Toggle handler for sctions
  const toggleSection = (key: keyof Filters) => {
    setOpenSections((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  }

  // Close dropdown on outside click
  useEffect(() => {
    if (!isDropdown) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isDropdown]);

  const renderCheckboxGroup = (group: (typeof filterGroups)[0]) => {
    const isOpen = openSections[group.key];

    return (
      <div key={group.key} style={{ marginBottom: "0.75rem" }}>
        <div
          onClick={() => toggleSection(group.key)}
          style={{
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            fontWeight: "bold",
            padding: "0.25rem 0",
          }}
        >
          <span>{group.label}</span>
          <span>{isOpen ? "▴" : "▾"}</span>
        </div>

        {isOpen && (
          <div style={{ paddingLeft: "0.5rem", marginTop: "0,25rem" }}>
            {group.options.map((opt) => (
              <div key={opt}>
                <label>
                  <input
                    type="checkbox"
                    checked={filters[group.key]?.includes(opt) || false}
                    onChange={() => handleToggle(group.key, opt)}
                  />
                  {opt}
                </label>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div
      ref={dropdownRef}
      style={{
        border: "1px solid #029ACF",
        borderRadius: "6px",
        background: "#fff",
        padding: "0.5rem 1rem",
        maxHeight: "80vh",
        overflowY: "auto",
        ...style,
      }}
    >
      {isDropdown && (
        <button
          onClick={() => setOpen(!open)}
          style={{
            width: "100%",
            padding: "0.5rem",
            marginBottom: "0.5rem",
            border: "1px solid #029ACF",
            borderRadius: "3px",
            background: "#029ACF",
            color: "#fff",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          {open ? "Hide Filters ▴" : "Show Filters ▾"}
        </button>
      )}

      {open && filterGroups.map(renderCheckboxGroup)}
    </div>
  );
};

export default FullSidebarFilters;
