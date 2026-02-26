import React, { useState, useEffect } from "react";
import { PagedUsers } from "../../adapters/api";
import { colors, radius } from "../theme/designTokens";


interface UsersPanelProps {
  data: PagedUsers | null;
  onPageChange?: (page: number) => void;
}


const UsersPanel: React.FC<UsersPanelProps> = ({ data, onPageChange }) => {
  if (!data) return null;

  return (
    <div
      style={{
        marginTop: "1rem",
        padding: "1rem",
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md
      }}
    >
      <div>
        Users (Page {data.currentPage} of {data.totalPages})
      </div>

      {data.users.map((u) => (
        <div key={u.id} style={{ marginTop: "0.5rem" }}>
          <div>ID: {u.id}</div>
          <div>Name: {u.name}</div>
          <div>Type: {u.membertype}</div>
        </div>
      ))}

      {/* Pagination controles */}
      <div style={{ marginTop: "1rem" }}>
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
