import React from "react";
import { PagedUsers } from "../../adapters/api";
import { colors, spacing, radius } from "../theme/designTokens";

interface UsersPanelProps {
  data: PagedUsers | null;
  onPageChange?: (page: number) => void;
}

const UsersPanel: React.FC<UsersPanelProps> = ({ data, onPageChange }) => {

  if (!data) return null;

  const { currentPage, totalPages } = data;

  return (
    <div
      style={{
        marginTop: spacing.lg,
        padding: spacing.lg,
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md,
        background: colors.surface,
        overflowX: "auto",
      }}
    >
      <div>
        Users (Page {currentPage} of {totalPages})
      </div>

      <div style={{ display: "grid", gap: spacing.sm }}>
        {data.users.map((u) => (
          <div
            key={u.id}
            style={{
              padding: spacing.sm,
              border: `1px solid ${colors.border}`,
              borderRadius: radius.sm,
            }}
          >
            <div><strong>ID: </strong>{u.id}</div>
            <div><strong>Name: </strong>{u.name}</div>
            <div><strong>Type: </strong>{u.membertype}</div>
          </div>
        ))}
      </div>

      {/* Pagination controles */}
      <div style={{ marginTop: spacing.md }}>
        <button
          onClick={() => onPageChange?.(currentPage - 1)}
          disabled={currentPage <= 1}
        >
          Previous
        </button>

        <button
          onClick={() => onPageChange?.(currentPage + 1)}
          disabled={currentPage >= totalPages}
          style={{ marginLeft: spacing.sm }}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default UsersPanel;
