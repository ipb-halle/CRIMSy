import React from "react";
import { useTheme } from "../../assets/css/theme/ThemeContext";
import { spacing, radius } from "../../assets/css/theme/designTokens";

interface Props {
  onLogout?: () => void;
}

const Navigation: React.FC<Props> = (
  { onLogout }
) => {
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
      <div
        style={{ fontWeight: 600, fontSize: "1.1rem" }}>
          IPB Laboratory Dashboard
      </div>

      {onLogout && (
        <button
          onClick={onLogout}
          style={{
            background: "transparent",
            border: "1px solid white",
            color: "white",
            padding: "6px 14px",
            borderRadius: radius.ssm,
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