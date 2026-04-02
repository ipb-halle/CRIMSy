import React, { useState, useEffect } from "react";
import DataTable, { TableColumn } from "react-data-table-component";
import ReactPaginate from "react-paginate";
import { PaginatedUserResponse, UserSummary } from "../../adapters/api";
import { colors, spacing, radius } from "../../assets/css/theme/designTokens";

interface UsersPanelProps {
  data: PaginatedUserResponse | null;
  totalCount: number;
  onPageChange?: (page: number) => void;
  onRowsPerPageChange?: (size: number) => void;
}

const UsersPanel: React.FC<UsersPanelProps> = (
  { data,
    totalCount,
    onPageChange,
    onRowsPerPageChange,
  }
) => {
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (data) setLoading(false);
  }, [data]);

  if (!data) return null;

  const { currentPage, totalPages, items } = data;

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
      cell: row => (
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
        <span style={{ color: "#666", fontSize: "14px" }}>
          (Page {currentPage} of {totalPages})
        </span>
      </div>

      {/* Table */}
      <DataTable
        keyField="id"
        columns={columns}
        data={items}
        pagination
        paginationServer
        paginationTotalRows={totalCount}
        onChangePage={handlePageChange}
        onChangeRowsPerPage={handleRowsPerPageChange}
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
        customStyles={{
          rows: {
            style: {
              minHeight: "56px",
            },
          },
          headCells: {
            style: {
              fontWeight: 600,
              fontSize: "14px",
              backgroundColor: "#fafafa",
            },
          },
          pagination: {
            style: {
              display: "flex",
              justifyContent: "center",
              padding: spacing.sm,
              gap: "6px",
            },
          },
        }}
      />

      {/* Inline styles (quick improvement) */}
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }

          /* Customize DataTable pagination buttons */
          .rdt_Pagination button {
            padding: 6px 10px;
            border-radius: ${radius.sm};
            border: 1px solid #ddd;
            background: ${colors.surface};
            cursor: pointer;
            font-weight: 500;
            color: #333;
          }

          .rdt_Pagination button:hover {
            background: #f0f0f0;
          }

          .rdt_Pagination .rdt_Pagination-selected {
            background: ${colors.primary || "#1976d2"};
            color: white;
            border-color: ${colors.primary || "#1976d2"};
          }

          .rdt_Pagination select {
            border-radius: ${radius.sm};
            border: 1px solid #ddd;
            padding: 4px 6px;
          }
        `}
      </style>
    </div>
  );
};

export default UsersPanel;