import React from "react";
import { colors, spacing, radius } from "../../../assets/css/theme/designTokens";

interface Props {
  onSearch: () => void;
}

const AdvancedSearchPanel: React.FC<Props> = ({ onSearch }) => {
  return (
    <div
      style={{
        background: colors.surface,
        padding: spacing.lg,
        borderRadius: radius.md,
        border: `1px solid ${colors.border}`,
        marginBottom: spacing.lg,
      }}
    >
      <h3>Advanced Material Search</h3>

      <div style={{ display: "grid", gap: spacing.md }}>
        <input placeholder="Material ID" />
        <input placeholder="Project ID" />
        <input placeholder="Owner ID (usersGroups)" />
        <input placeholder="Hazard (GHS01, GHS02...)" />
        <select>
          <option>Material Type</option>
          <option>STRUCTURE</option>
          <option>BIOMATERIAL</option>
        </select>

        <button
          onClick={onSearch}
          style={{
            background: colors.primary,
            color: "white",
            padding: "8px",
            borderRadius: radius.sm,
            border: "none",
          }}
        >
          Search
        </button>
      </div>
    </div>
  );
};

export default AdvancedSearchPanel;