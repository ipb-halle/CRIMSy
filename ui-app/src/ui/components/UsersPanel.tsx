import React from "react";
import { PagedUsers } from "../../adapters/api";
import { colors, spacing, radius } from "../../assets/css/theme/designTokens";

interface UsersPanelProps {
  data: PagedUsers | null;
  onPageChange?: (page: number) => void;
}

const UsersPanel: React.FC<UsersPanelProps> = (
  { data,
    onPageChange }
) => {

  if (!data) return null;

  const prev = data.currentPage > 1;
  const next = data.currentPage < data.totalPages;

  const { currentPage, totalPages } = data;

  return (
    <div
      style={{
        padding: spacing.md,
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md,
        background: colors.surface,
        overflowX: "auto",
      }}
    >
      <div>
        Users (Page {currentPage} of {totalPages})
      </div>
      {data.users.map(
        u => (
          <div key={u.id} style={{ marginTop: spacing.sm }}>
            {u.name} ({u.membertype})
          </div>
        ))}


      {/* Pagination controles */}
      <div
        style={{ marginTop: spacing.md }}
      >
        <button
          onClick={() => onPageChange?.(currentPage - 1)}
          disabled={!prev}
        >
          Prev
        </button>

        <button
          onClick={() => onPageChange?.(currentPage + 1)}
          disabled={!next}
          style={{ marginLeft: spacing.sm }}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default UsersPanel;