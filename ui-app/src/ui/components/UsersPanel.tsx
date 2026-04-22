import React, { useState, useEffect } from "react";
import DataTable, { TableColumn } from "react-data-table-component";
import { PaginatedUserResponse, UserSummary } from "../../adapters/api";
import { colors, spacing, radius } from "../../assets/css/theme/designTokens";
import * as api from "../../services/authService";

interface UsersPanelProps {
  data: PaginatedUserResponse | null;
  totalCount: number;
  rowsPerPage: number;
  onPageChange?: (page: number) => void;
  onRowsPerPageChange?: (size: number) => void;
  isAdmin: boolean;
}

const UsersPanel: React.FC<UsersPanelProps> = ({
  data,
  totalCount,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
  isAdmin,
}) => {
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (data) setLoading(false);
  }, [data]);

  if (!data) return null;

  const { currentPage, totalPages, items } = data;

  const handleDeleteUser = async (id: number) => {

    const token = localStorage.getItem("token");
    if (!token) {
      alert("No auth token found");
      return;
    }

    const user = items.find(u => u.id === id);

    const confirmed = window.confirm(
      `Delete user "${user?.name || id}"?\nThis action cannot be undone.`
    );

    if (!confirmed) {
      console.log("[DELETE] cancelled by admin");
      return;
    }

    try {
      setLoading(true);

      await api.deleteUsersAPI(token, id);

      onPageChange?.(currentPage);
      alert("User deleted successfully");

    } catch (err: any) {
      console.error("[Delete] error: ", err);
      alert(err?.message || "Failed to delete user");
    } finally {
      setLoading(false);
    }
  };

  const columns: TableColumn<UserSummary>[] = [
    {
      name: "Name",
      selector: (row: any) => row.name,
      sortable: true,
      grow: 2,
    },
    {
      name: "Admin",
      selector: row => (row.admin ? "Yes" : "No"),
      sortable: true,
      center: true,
      cell: (row) => (
        <span
          style={{
            padding: "4px 8px",
            borderRadius: "12px",
            fontSize: "12px",
            background: row.admin ? "#e6f4ea" : "#f5f5f5",
            color: row.admin ? "#2e7d32" : "#666",
            fontWeight: 500,
          }}
        >
          {row.admin ? "Admin" : "User"}
        </span>
      ),
    },
    ...(isAdmin ? [
      {
        name: "Edit",
        cell: (row: UserSummary) => (
          <button
            //onClick{() => handleEditUser(row)}
            style={{
              padding: "6px 10px",
              borderRadius: "6px",
              background: "#1976d2",
              color: "#fff",
              border: "none",
              cursor: "pointer",
            }}
          >
            Edit
          </button >
        ),
      },
      {
        name: "Delete",
        cell: (row: UserSummary) => (
          <button
            onClick={() => handleDeleteUser(row.id)}
            style={{
              padding: "6px 10px",
              borderRadius: "6px",
              background: "#1976d2",
              color: "#fff",
              border: "none",
              cursor: "pointer",
            }}
          >
            Delete
          </button >
        ),
      },
    ] as TableColumn<UserSummary>[] : []),

  ];

  const handlePageChange = (page: number) => {
    setLoading(true);
    onPageChange?.(page);
  };

  const handleRowsPerPageChange = (size: number) => {
    setLoading(true);
    onRowsPerPageChange?.(size);
  };

  return (
    <div
      style={{
        padding: spacing.md,
        border: `1px solid  ${colors.border}`,
        borderRadius: radius.md,
        background: colors.surface,
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
      }}
    >
      {/* Header */}
      <div
        style={{
          marginBottom: spacing.md,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          fontWeight: 600,
        }}
      >
        <span> Users </span>

        <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
          <span style={{ color: "#666", fontSize: "14px" }}>
            (Page {currentPage} of {totalPages})
          </span>
          {isAdmin && (
            <button
              //onClick={() => handleCreateUSer()}
              style={{
                padding: "6px 12px",
                borderRadius: "6px",
                background: "#2e7d32",
                color: "#fff",
                border: "none",
                cursor: "pointer",
                fontWeight: 500,
              }}
            >
              Create User
            </button>
          )}
        </div>
      </div>

      {/* DataTable */}
      <DataTable
        key={`${currentPage}-${rowsPerPage}`}
        columns={columns}
        data={items}
        pagination
        paginationServer
        paginationTotalRows={totalCount}
        paginationPerPage={rowsPerPage}
        paginationDefaultPage={currentPage}
        onChangePage={handlePageChange}
        onChangeRowsPerPage={handleRowsPerPageChange}
        paginationRowsPerPageOptions={[5, 10, 15, 20, 25, 30, 40, 50]}
        highlightOnHover
        striped
        responsive
        progressPending={loading}
        progressComponent={
          <div style={{ display: "flex", justifyContent: "center", padding: spacing.md }}>
            <div
              style={{
                width: 24,
                height: 24,
                border: "4px solid #ddd",
                borderTop: `4px solid ${colors.primary || "#1976d2"}`,
                borderRadius: "50%",
                animation: "spin 1s linear infinite",
              }}
            />
          </div>
        }
      />

      {/* Inline styles (quick improvement) */}
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
};

export default UsersPanel;