import React, { useState, CSSProperties } from "react";
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
  }

  /*// Close dropdown on outside click
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
*/

  const renderCheckboxGroup = (group: FilterGroup) => {
    const isOpen = openSections[group.key];
    const selected = (filters[group.key] as any[]) || [];

    return (
      <div key={group.key} style={{ marginBottom: spacing.md }}>
        <div
          onClick={() => toggleSection(group.key)}
          style={{
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            fontWeight: 600,
            fontSize: "0.95rem",
            color: "#003c78",
            padding: "0.4rem 0",
            borderBottom: "1px solid #e6eef5",
          }}
        >
          <span>{group.label}</span>
          <span
            style={{ fontSize: "0.8rem" }}>
            {isOpen ? "▴" : "▾"}
          </span>
        </div>

        {isOpen && (
          <div
            style={{
              paddingLeft: spacing.sm,
              marginTop: spacing.sm,
              textAlign: "left"
            }}
          >
            {group.options.map((opt) => (
              <label
                key={String(opt.value)}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "0.4rem",
                  marginBottom: "0.3rem",
                  fontSize: "0.85rem",
                  cursor: "pointer",
                }}
              >
                <input
                  type="checkbox"
                  checked={selected.includes(opt.value)}
                  onChange={() => handleToggle(group.key, opt.value)}
                  style={{
                    accentColor: "#00509e",
                  }}
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
        border: "1px solid #d8e2ec",
        borderRadius: radius.md,
        background: "#fff",
        padding: spacing.md,
        maxHeight: "75vh",
        overflowY: "auto",
        boxShadow: "0 4px 14px rgba(0,0,0,0.05)",
        ...style,
      }}
    >
      {filterConfig.map(renderCheckboxGroup)}
    </div>
  );
};

export default FullSidebarFilters;
