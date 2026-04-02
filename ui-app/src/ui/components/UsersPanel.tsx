import React from "react";
import DataTable, { TableColumn } from "react-data-table-component";
import ReactPaginate from "react-paginate";
import { PaginatedUserResponse, UserSummary } from "../../adapters/api";
import { colors, spacing, radius } from "../../assets/css/theme/designTokens";

interface UsersPanelProps {
  data: PaginatedUserResponse | null;
  onPageChange?: (page: number) => void;
}

const UsersPanel: React.FC<UsersPanelProps> = (
  { data,
    onPageChange }
) => {
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
      selector: (row: any) => (row.admin ? "Yes" : "No"),
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

  const handlePageClick = (selectedItem: { selected: number }) => {
    const page = selectedItem.selected + 1; // react-paginate is 0-based
    onPageChange?.(page);
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
        <span>  Users </span>
        <span style={{ color: "#666", fontSize: "14px" }}>
          (Page {currentPage} of {totalPages})
        </span>
      </div>

      {/* Table */}
      <DataTable
        keyField="id"
        columns={columns}
        data={items}
        pagination={false} // we use external pagination
        highlightOnHover
        striped
        responsive
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
        }}
      />

      {/* Pagination */}
      <div style={{ marginTop: spacing.md }}>
        <ReactPaginate
          previousLabel="← Prev"
          nextLabel="Next →"
          breakLabel="..."
          pageCount={totalPages}
          marginPagesDisplayed={1}
          pageRangeDisplayed={2}
          onPageChange={handlePageClick}
          forcePage={currentPage - 1}
          containerClassName="pagination"
          activeClassName="active"
        />
      </div>

      {/* Inline styles (quick improvement) */}
      <style>
        {`
          .pagination {
            display: flex;
            gap: 6px;
            list-style: none;
            padding: 0;
          }

          .pagination li {
            padding: 6px 10px;
            border-radius: 6px;
            cursor: pointer;
            border: 1px solid #ddd;
          }

          .pagination li.active {
            background: #1976d2;
            color: white;
            border-color: #1976d2;
          }

          .pagination li:hover {
            background: #f0f0f0;
          }
        `}
      </style>
    </div>
  );
};

export default UsersPanel;