import React from "react";
import { colors, spacing } from "../../theme/designTokens";

interface Props {
  active: string;
  onChange: (view: string) => void;
}

const TopNavigation: React.FC<Props> = ({ active, onChange }) => {
  const tabs = ["search", "rolecheck", "userlist"];

  return (
    <header
      style={{
        background: colors.primary,
        color: "white",
        padding: spacing.md,
        display: "flex",
        justifyContent: "center",
        gap: spacing.lg,
      }}
    >
      {tabs.map(tab => (
        <button
          key={tab}
          onClick={() => onChange(tab)}
          style={{
            background: active === tab ? colors.accent : "transparent",
            border: "none",
            color: "white",
            padding: "8px 16px",
            borderRadius: "6px",
            cursor: "pointer",
            transition: "all 0.25s ease",
          }}
        >
          {tab.toUpperCase()}
        </button>
      ))}
    </header>
  );
};

export default TopNavigation;