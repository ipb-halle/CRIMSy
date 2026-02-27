import React, { useState, useMemo } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import CardView from "../components/views/CardView";
import TableView from "../components/views/TableView";
import { dummyData } from "../../data/dummyData";
import { filterRuls } from "../components/filters/filterRules";
import { spacing } from "../theme/designTokens";

const PAGE_SIZE = 6;

const SearchPanel: React.FC = () => {
  const [filters, setFilters] = useState<Filters>({});
  const [view, setView] = useState<"card" | "table">("card");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [sort, setSort] = useState<"id" | "project">("id");


  //  Apply all filters + search term
  const results = useMemo(() => {
    return dummyData.materials
      .filter((material) =>
        filterRuls.every((rule) => {
          const selected = (filters as any)[rule.key];
          return !selected?.length || rule.matches(material, selected);
        })
      )
      .filter(material =>
        search ? material.materialId.toString().includes(search) : true
      )
      .sort((a, b) =>
        sort === "id"
          ? a.materialId - b.materialId
          : a.projectId - b.projectId
      );
  }, [filters, search, sort]);

  // Pagination
  const paged = results.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // CSV Export
  /*const exportCSV = () => {
    const rows = filtered.map(m => ({
      id: m.materialId,
      project: m.projectId,
      owner: m.ownerId,
      server: m.serverId,
      status: m.deactivated ? "Deactivated" : "Active",
    }));

    const header = "ID,Project,Owner,Server,Status\n";
    const csv = header + rows.map(r =>
      `${r.id},${r.project},${r.owner},${r.server},${r.status}`
    ).join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "materials.csv";
    a.click();
    URL.revokeObjectURL(url);
  };*/

  return (
    <div style={{
      display: "flex",
      gap: spacing.lg,
      alignItems: "flex-start"
    }}
    >
      {/* Sidebar Filters */}
      <div style={{ width: "260px", flexShrink: 0 }}>
        <FullSidebarFilters
          onFilterChange={setFilters}
        />
      </div>

      {/* Results  */}
      <div style={{
        flex: 1,
        display: "flex",
        flexDirection: "column",
        minHeight: 0
      }}
      >

        {/* View Toggle */}
        <div style={{
          marginBottom: spacing.lg,
          display: "flex",
          flexWrap: "wrap",
          gap: spacing.sm,
          alignItems: "center",
        }}
        >
          <input
            placeholder="Search Material ID..."
            value={search}
            onChange={(e) => {
              setPage(1);
              setSearch(e.target.value);
            }}
            style={{
              padding: "0.45rem 0.75rem",
              borderRadius: "6px",
              border: "1px solid #cfd9e3",
              minWidth: "180px",
            }}
          />

          <select
            value={sort}
            onChange={(e) => setSort(e.target.value as any)}
            style={{
              padding: "0.45rem 0.6rem",
              borderRadius: "6px",
              border: "1px solid #cfd9e3",
            }}
          >
            <option value="id">Sort by ID</option>
            <option value="project">Sort by Project</option>
          </select>

          <button
            onClick={() => setView(view === "card" ? "table" : "card")}
            style={{
              padding: "0.45rem 0.9rem",
              background: "#00509e",
              color: "white",
              border: "none",
              borderRadius: "6px",
              cursor: "pointer",
            }}
          >
            {view === "card" ? "Table View" : "Card View"}
          </button>
        </div>

        {/* Results */}
        <div
          style={{
            background: "#ffffff",
            padding: spacing.lg,
            borderRadius: "8px",
            boxShadow: "0 4px 16px rgba(0,0,0,0.05)",
          }}
        >
          {view === "card" ? (
            <CardView items={paged} />
          ) : (
            <TableView items={paged} />
          )}
        </div>
        {/* Pagination */}
        <div
          style={{
            marginTop: spacing.lg,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: spacing.md,
          }}
        >
          <button
            disabled={page <= 1}
            onClick={() => setPage(page - 1)}
            style={{
              padding: "0.4rem 0.9rem",
              background: "#00509e",
              color: "white",
              border: "none",
              borderRadius: "6px",
              cursor: "pointer",
              opacity: page <= 1 ? 0.5 : 1,
            }}
          >
            Prev
          </button>

          <span >
            Page {page} / {Math.ceil(results.length / PAGE_SIZE) || 1}
          </span>

          <button
            disabled={page * PAGE_SIZE >= results.length}
            onClick={() => setPage(page + 1)}
            style={{
              padding: "0.4rem 0.9rem",
              background: "#00509e",
              color: "white",
              border: "none",
              borderRadius: "6px",
              cursor: "pointer",
              opacity: page * PAGE_SIZE >= results.length ? 0.5 : 1,
            }}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default SearchPanel;