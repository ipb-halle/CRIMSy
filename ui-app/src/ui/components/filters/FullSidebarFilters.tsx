import React, { useState, CSSProperties } from "react";
import { FilterGroup, Filters } from "./Filters";
import { filterConfig } from "./filterConfig";
import { radius, spacing } from "../../../assets/css/theme/designTokens";
import { useTheme } from "../../../assets/css/theme/ThemeContext";

interface Props {
  onFilterChange: (filters: Filters) => void;
}

const FullSidebarFilters: React.FC<Props> = ({
  onFilterChange
}) => {
  const [filters, setFilters] = useState<Filters>({});
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({});

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
  };

  const renderCheckboxGroup = (
    group: FilterGroup) => {
    const isOpen = openSections[group.key];
    const selected = (filters[group.key] as any[]) || [];

    return (
      <div key={group.key} style={{ marginBottom: "0.75rem" }}>
        <div
          onClick={() => toggleSection(group.key)}
          style={{
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            fontWeight: "bold",
          }}
        >
          <span>{group.label}</span>
          <span> {isOpen ? "▴" : "▾"} </span>
        </div>

        {isOpen && (
          <div
            style={{
              marginTop: "0.25rem",
              textAlign: "left"
            }}
          >
            {group.options.map((opt) => (
              <label
                key={String(opt.value)}
                style={{ display: "block" }}
              >
                <input
                  type="checkbox"
                  checked={selected.includes(opt.value)}
                  onChange={() => handleToggle(group.key, opt.value)}
                />
                {opt.label}
              </label>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div
      style={{
        border: `1px solid ${theme.border || "#ccc"}`,
        borderRadius: radius.sm,
        background: theme.surface || "#fff",
        padding: "0.75rem",
        maxHeight: "80vh",
        overflowY: "auto",
      }}
    >
      {filterConfig.map(renderCheckboxGroup)}
    </div>
  );
};

export default FullSidebarFilters;