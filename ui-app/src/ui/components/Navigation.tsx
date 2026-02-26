import React from "react";
import { spacing, radius } from "../theme/designTokens";
import { useTheme } from "../theme/ThemeContext"; 

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
            width: "32px",
            height: "32px",
            background: "white",
            borderRadius: radius.sm,
          }}
        />
        <span style={{ fontWeight: 600 }}>IPB Biochemistry Institute</span>
      </div>

      {onLogout && (
        <button
          onClick={onLogout}
          style={{
            background: "transparent",
            border: "1px solid white",
            color: "white",
            padding: "0.3rem 0.6rem",
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