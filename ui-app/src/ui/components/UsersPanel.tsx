import React, { useState, useEffect } from "react";
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
        Users (Page {data.currentPage} of {data.totalPages})
      </div>

      {data.users.map((u) => (
        <div key={u.id} style={{ marginTop:  spacing.sm}}>
          <div>ID: {u.id}</div>
          <div>Name: {u.name}</div>
          <div>Type: {u.membertype}</div>
        </div>
      ))}

      {/* Pagination controles */}
      <div style={{ marginTop: spacing.md }}>
        <button onClick={() => onPageChange?.(data.currentPage - 1)}>
          Previous
        </button>
        <button onClick={() => onPageChange?.(data.currentPage + 1)} >
          Next
        </button>
      </div>
    </div>
  );
};

export default UsersPanel;
