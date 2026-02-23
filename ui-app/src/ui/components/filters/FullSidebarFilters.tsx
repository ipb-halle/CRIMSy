// src/ui/components/filters/FullSidebarFilters.tsx
import React, { useState, useRef, useEffect, CSSProperties } from "react";
import { dummyData } from "../../../data/dummyData";

export interface Filters {
  projects?: string[];
  users?: string[];
  materials?: string[];
  materialTypes?: string[];
  containers?: string[];
  storageClasses?: string[];
  hazards?: string[];
  deactivationStatus?: string[];
}

interface Props {
  onFilterChange: (filters: Filters) => void;
  style?: CSSProperties;
  isDropdown?: boolean;
}

const FullSidebarFilters: React.FC<Props> = ({ onFilterChange, style, isDropdown }) => {
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
  };

  // Close dropdown on outside click
  useEffect(() => {
    if (!isDropdown) return;
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isDropdown]);


  // Toggle handler for sctions

  const toggleSection = (key: keyof Filters) => {
    setOpenSections((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  }

  const renderCheckboxGroup = (
    label: string,
    key: keyof Filters,
    options: string[]
  ) => {
    const isOpen = openSections[key];

    return (
      <div style={{ marginBottom: "0.75rem" }}>
        <div
          onClick={() => toggleSection(key)}
          style={{
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            fontWeight: "bold",
            padding: "0.25rem 0",
          }}
        >
          <span>{label}</span>
          <span>{isOpen ? "▴" : "▾"}</span>
        </div>
        {isOpen && (
          <div style={{ paddingLeft: "0.5rem", marginTop: "0,25rem" }}>
            {options.map((opt) => (
              <div key={opt}>
                <label>
                  <input
                    type="checkbox"
                    checked={filters[key]?.includes(opt) || false}
                    onChange={() => handleToggle(key, opt)}
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

      {open && (
        <>
          {renderCheckboxGroup("Projects", "projects", dummyData.projects.map(p => p.name))}
          {renderCheckboxGroup("Users", "users", dummyData.users.map(u => u.name))}
          {renderCheckboxGroup("Materials", "materials", dummyData.materials.map(m => m.name))}
          {renderCheckboxGroup("Material Types", "materialTypes", dummyData.materialTypes)}
          {renderCheckboxGroup("Containers", "containers", dummyData.containers)}
          {renderCheckboxGroup("Storage Classes", "storageClasses", dummyData.storageClasses)}
          {renderCheckboxGroup("Hazards", "hazards", dummyData.hazards)}
          {renderCheckboxGroup("Deactivation Status", "deactivationStatus", dummyData.deactivationStatus)}
        </>
      )}
    </div>
  );
};

export default FullSidebarFilters;
