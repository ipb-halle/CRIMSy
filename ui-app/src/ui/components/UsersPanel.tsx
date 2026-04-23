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
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);

  const [editingUser, setEditingUser] = useState<UserSummary | null>(null);
  const [selectedRole, setSelectedRole] = useState<"USER" | "ADMIN">("USER");

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
      setConfirmDeleteId(null);
    }
  };

  const handleEditUser = async () => {
    const token = localStorage.getItem("token");
    if (!token || !editingUser) {
      alert("No auth token found");
      return;
    }

    try {
      setLoading(true);

      const isAdmin = selectedRole === "ADMIN";

      await api.updateUserAPI(token, editingUser.id, {
        ...editingUser,
        admin: isAdmin,
        groups: isAdmin ? ["Users", "Admin Group"] : ["Users"],
      });

      setEditingUser(null);
      alert("User updated successfully");
      onPageChange?.(currentPage);
    } catch (err: any) {
      console.error(err);
      alert(err?.message || "Update failed");
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
    ...(isAdmin
      ? [
        {
          name: "Edit",
          cell: (row: UserSummary) => (
            <button
              onClick={() => {
                setEditingUser(row);
                setSelectedRole(row.admin ? "ADMIN" : "USER");
              }}
              style={{
                padding: "6px 10px",
                borderRadius: "6px",
                background: "#1976d2",
                color: "#fff",
                border: "none",
                cursor: "pointer",
              }}
            >
              Manage Role
            </button >
          ),
        },
        {
          name: "Delete",
          cell: (row: UserSummary) =>
            confirmDeleteId === row.id ? (
              <div style={{ display: "flex", gap: "4px", alignItems: "center" }}>
                <span style={{ fontSize: "12px", color: "#666" }}>Sure?</span>
                <button
                  onClick={() => handleDeleteUser(row.id)}
                  style={{
                    padding: "4px 8px", borderRadius: "6px",
                    background: "#d32f2f", color: "#fff",
                    border: "none", cursor: "pointer", fontSize: "12px",
                  }}
                >
                  Yes
                </button>
                <button
                  onClick={() => setConfirmDeleteId(null)}
                  style={{
                    padding: "4px 8px", borderRadius: "6px",
                    background: "#666", color: "#fff",
                    border: "none", cursor: "pointer", fontSize: "12px",
                  }}
                >
                  No
                </button>
              </div>
            ) : (
              <button
                onClick={() => setConfirmDeleteId(row.id)}
                style={{
                  padding: "6px 10px",
                  borderRadius: "6px",
                  background: "#d32f2f",
                  color: "#fff",
                  border: "none",
                  cursor: "pointer",
                }}
              >
                Delete
              </button >
            ),
        },
      ]
      : []) as TableColumn<UserSummary>[],
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
      {/* ------- Edit Modal ------- */}
      {editingUser && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0,0,0,0.4)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 9999,
          }}
        >
          <div
            style={{
              background: "#fff",
              padding: 20,
              borderRadius: 10,
              minWidth: 320,
            }}
          >
            <h3>Edit User</h3>
            <p>
              <b>{editingUser.name}</b>
            </p>
            <label style={{ display: "block", marginTop: 10 }}>
              <input
                type="radio"
                checked={selectedRole === "USER"}
                onChange={() => setSelectedRole("USER")}
              />
              Normal User
            </label>

            <label style={{ display: "block", marginTop: 5 }}>
              <input
                type="radio"
                checked={selectedRole === "ADMIN"}
                onChange={() => setSelectedRole("ADMIN")}
              />
              Admin
            </label>
            <div
              style={{
                display: "flex", marginTop: 20,
                gap: 10,
                justifyContent: "flex-end",
              }}
            >
              <button
                onClick={() => setEditingUser(null)}
                style={{
                  padding: "6px 10px",
                  background: "#777",
                  color: "#fff",
                  border: "none",
                  borderRadius: 6,
                }}
              >
                Cancel
              </button>

              <button
                onClick={handleEditUser}
                style={{
                  padding: "6px 10px",
                  background: "#1976d2",
                  color: "#fff",
                  border: "none",
                  borderRadius: 6,
                }}
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
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