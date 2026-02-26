import React from "react";
import { RoleResponse } from "../../adapters/api";
import { colors, radius, spacing } from "../theme/designTokens";

interface Props {
  role: RoleResponse | null;
}

const RolePanel: React.FC<Props> = ({ role }) => {
  if (!role) return null;

  return (
    <div
      style={{
        marginTop: spacing.lg,
        padding: spacing.lg,
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md,
        background: colors.surface,
      }}
    >

      <div>
        <span style={{ fontWeight: 600 }}>Username: </span>{role.username}
      </div>

      <div
        style={{ marginTop: spacing.sm }}
      >
        <span style={{ fontWeight: 600 }}>Role:</span>
        <span
          style={{
            marginLeft: spacing.sm,
            padding: "0.2rem 0.6rem",
            borderRadius: radius.sm,
            background: role.admin ? colors.success : colors.accent,
            color: "white",
            fontSize: "0.75rem",
          }}
        >
          {role.admin ? "Administrator" : "User"}
        </span>
      </div>
    </div>
  );
};

export default RolePanel;
