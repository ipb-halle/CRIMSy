import React, { useState, useMemo } from "react";
import DataTable, { TableColumn } from "react-data-table-component";
import { PaginatedUserResponse, UserSummary } from "../../../adapters/api";
import { colors, spacing, radius } from "../../../assets/css/theme/designTokens";
import { useUsers } from "../../../adapters/hooks/useUsers";
import { authSession } from "../../../adapters/hooks/authSession";
import EditUserModal from "./EditUserModal";


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
  const { loading, deleteUser, updateUser } = useUsers();

  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
  const [editingUser, setEditingUser] = useState<UserSummary | null>(null);
  const [selectedRole, setSelectedRole] = useState<"USER" | "ADMIN">("USER");

  const currentUserId = Number(authSession.getToken() ? localStorage.getItem("userId") : null);

  const isSelf = (row: UserSummary) => row.id === currentUserId;

  const columns: TableColumn<UserSummary>[] = useMemo(() => {
    return [
      {
        name: "Name",
        selector: (row) => row.name || "",
        sortable: true,
        grow: 2,
      },
      {
        name: "Role",
        selector: (row) => (row.admin ? "Admin" : "User"),
        sortable: true,
        center: true,
      },
      ...(isAdmin
        ? [
          {
            name: "Actions",
            cell: (row: UserSummary) =>
              isSelf(row) ? null : (
                <button

                  style={{
                    padding: "6px 12px",
                    borderRadius: "6px",
                    background: "#2e7d32",
                    color: "#fff",
                    border: "none",
                    cursor: "pointer",
                    fontWeight: 500,
                  }}
                  onClick={() => {
                    setEditingUser(row);
                    setSelectedRole(row.admin ? "ADMIN" : "USER");
                  }}
                >
                  Manage
                </button>
              ),
          },
          {
            name: "Delete",
            cell: (row: UserSummary) =>
              isSelf(row) ? null : confirmDeleteId === row.id ? (
                <>
                  <button onClick={() => handleDeleteUser(row.id)}>
                    Yes
                  </button>
                  <button onClick={() => setConfirmDeleteId(null)}    >
                    No
                  </button>
                </>
              ) : (
                <button onClick={() => setConfirmDeleteId(row.id)} >
                  Delete
                </button >
              ),
          },
        ]
        : []),
    ];
  }, [isAdmin, confirmDeleteId]);

  if (!data) return null;

  const { currentPage, totalPages, items } = data;

  const handleDeleteUser = async (id: number) => {
    try {
      await deleteUser(id);
      onPageChange?.(currentPage);
      alert("User deleted successfully");
      setConfirmDeleteId(null);
    } catch (err: any) {
      alert(err?.message || "Failed to delete user");
    }
  };

  const handleEditUser = async () => {
    if (!editingUser) {
      return;
    }

    try {
      await updateUser(editingUser, selectedRole);
      setEditingUser(null);
      onPageChange?.(currentPage);
      alert("User updated successfully");
    } catch (err: any) {
      alert(err?.message || "Update failed");
    }
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
          border: `1pc solid ${colors.border}`,
          borderRadius: radius.md,
          background: colors.surface,
          display: "flex",
          justifyContent: "space-between",
        }}
      >
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
      {/*    </div> */}

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
        onChangePage={onPageChange}
        onChangeRowsPerPage={onRowsPerPageChange}
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
      {
        editingUser && (
          <EditUserModal
            user={editingUser}
            role={selectedRole}
            onChangeRole={setSelectedRole}
            onClose={() => setEditingUser(null)}
            onSave={handleEditUser}
          />
        )
      }
    </div >
  );
};

export default UsersPanel;