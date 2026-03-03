import React from "react";
import { RoleResponse } from "../../adapters/api";
import { colors, radius, spacing } from "../../assets/css/theme/designTokens";

interface Props {
  role: RoleResponse | null;
}

const RolePanel: React.FC<Props> = (
  { role }
) => {
  if (!role) return null;

  return (
    <div
      style={{
        padding: spacing.md,
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md,
        background: colors.surface,
      }}
    >

      <div>
        <span style={{ fontWeight: 600 }}>Username: </span>{role.username}
      </div>

      <div
        style={{
          marginTop: spacing.sm,
          padding: "0.2rem 0.6rem",
          borderRadius: radius.sm,
          background: role.admin ? colors.success : colors.secondary,
          color: "white",
          display: "inline-block",
        }}
      >
        {role.admin ? "Administrator" : "User"}
      </div>
    </div>
  );
};

export default RolePanel;