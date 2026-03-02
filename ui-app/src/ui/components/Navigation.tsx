import React from "react";
import { useTheme } from "../../assets/css/theme/ThemeContext";
import { spacing, radius } from "../../assets/css/theme/designTokens";

interface Props {
  onLogout?: () => void;
}

const Navigation: React.FC<Props> = ({ onLogout }) => {
  const { theme } = useTheme();

  return (
    <nav
      style={{
        background: theme.primary,
        color: "white",
        padding: `${spacing.sm} ${spacing.md}`,
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
        <div
          style={{
            width: "34px",
            height: "34px",
            background: theme.accent,
            borderRadius: radius.sm,
          }}
        />
        <span style={{ fontWeight: 600 }}>
          IPB Laboratory Dashboard
        </span>
      </div>

      {onLogout && (
        <button
          onClick={onLogout}
          style={{
            background: "transparent",
            border: "1px solid white",
            color: "white",
            padding: "0.3rem 0.7rem",
            borderRadius: radius.sm,
            cursor: "pointer",
          }}
        >
          Logout
        </button>
      )}
    </nav>
  );
};

export default Navigation;