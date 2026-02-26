import React from "react";
import { PagedUsers } from "../../adapters/api";
import { useTheme } from "../theme/ThemeContext";
import { spacing, radius } from "../theme/designTokens";

interface UsersPanelProps {
  data: PagedUsers | null;
  onPageChange?: (page: number) => void;
}

const UsersPanel: React.FC<UsersPanelProps> = ({ data, onPageChange }) => {
  const { theme } = useTheme();

  if (!data) return null;

  const { currentPage, totalPages } = data;

  const handlePrev = () => {
    if (currentPage > 1) {
      onPageChange?.(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages) {
      onPageChange?.(currentPage + 1);
    }
  };

  return (
    <div
      style={{
        marginTop: spacing.lg,
        padding: spacing.lg,
        border: `1px solid  ${theme.border}`,
        borderRadius: radius.md,
        background: theme.surface,
      }}
    >
      <div>
        Users (Page {currentPage} of {totalPages})
      </div>

      {data.users.map((u) => (
        <div key={u.id} style={{ marginTop: spacing.sm }}>
          <div>ID: {u.id}</div>
          <div>Name: {u.name}</div>
          <div>Type: {u.membertype}</div>
        </div>
      ))}

      {/* Pagination controles */}
      <div style={{ marginTop: spacing.md }}>
        <button onClick={handlePrev} disabled={currentPage <= 1}>
          Previous
        </button>
        <button onClick={handleNext} disabled={currentPage >= totalPages}
          style={{ marginLeft: spacing.sm }}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default UsersPanel;
