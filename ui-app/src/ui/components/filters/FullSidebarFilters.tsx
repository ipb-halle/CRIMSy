import React, { useState, useRef, useEffect, CSSProperties } from "react";
import { FilterGroup, Filters } from "./Filters";
import { filterConfig } from "./filterConfig";
import { radius, spacing } from "../../theme/designTokens";
import { useTheme } from "../../theme/ThemeContext";

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


  const { theme } = useTheme();

  const handleToggle = (key: keyof Filters, value: number | boolean) => {
    const current = (filters[key] as any[]) || [];

    const updated = current.includes(value)
      ? current.filter((v) => v !== value)
      : [...current, value];

    const newFilters = { ...filters, [key]: updated };
    setFilters(newFilters);
    onFilterChange(newFilters);
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

  const renderCheckboxGroup = (group: FilterGroup) => {
    const isOpen = openSections[group.key];
    const selected = (filters[group.key] as (number | boolean)[]) || [];

    return (
      <div key={group.key} style={{ marginBottom: "0.75rem" }}>
        <div
          onClick={() => toggleSection(group.key)}
          style={{
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            fontWeight: 600,
            padding: "0.25rem 0",
            color: theme.textPrimary,
          }}
        >
          <span>{group.label}</span>
          <span>{isOpen ? "▴" : "▾"}</span>
        </div>

        {isOpen && (
          <div style={{ paddingLeft: "0.5rem", marginTop: "0,25rem" }}>
            {group.options.map((opt) => (
              <div key={String(opt.value)}>
                <label style={{ fontSize: "0.9rem", color: theme.textSecondary }}>
                  <input
                    type="checkbox"
                    checked={selected.includes(opt.value)}
                    onChange={() => handleToggle(group.key, opt.value)}
                  />
                  {opt.label}
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
        borderRadius: "6px",
        background: theme.surface,
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
            border: "none",
            borderRadius: "3px",
            background: theme.primary,
            color: "white",
            fontWeight: "bold",
            cursor: "pointer",
          }}
        >
          {open ? "Hide Filters ▴" : "Show Filters ▾"}
        </button>
      )}

      {open && filterConfig.map(renderCheckboxGroup)}
    </div>
  );
};

export default FullSidebarFilters;
