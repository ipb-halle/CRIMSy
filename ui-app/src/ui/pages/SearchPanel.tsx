import React, { useState, useMemo } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import CardView from "../components/views/CardView";
import TableView from "../components/views/TableView";
import { dummyData } from "../../data/dummyData";
import { filterRuls } from "../components/filters/filterRules";
import { spacing } from "../theme/designTokens";
import MaterialDetailModal from "../components/views/MaterialDetailModal";

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
        sort === "id" ? a.materialId - b.materialId : a.projectId - b.projectId
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
    <div style={{ display: "flex", gap: spacing.md }}>
      {/* Sidebar Filters */}
      <div style={{ width: "260px", flexShrink: 0 }}>
        <FullSidebarFilters
          onFilterChange={setFilters}
        />
      </div>

      {/* Results  */}
      <div style={{ flex: 1 }}>

        {/* View Toggle */}
        <div style={{ marginBottom: spacing.md }}>
          <input
            placeholder="Search ID..."
            value={search}
            onChange={(e) => {
              setPage(1);
              setSearch(e.target.value);
            }}
            style={{ padding: spacing.sm }}
          />

          <select
            value={sort}
            onChange={(e) => setSort(e.target.value as any)}
            style={{ marginLeft: spacing.sm }}
          >
            <option value="id">Sort by ID</option>
            <option value="project">Sort by Project</option>
          </select>
        </div>

        {/* Results */}
        {view === "card" ? (
          <CardView items={paged} />
        ) : (
          <TableView items={paged} />
        )}

        {/* Pagination */}
        <div style={{ marginTop: spacing.md }}>
          <button
            disabled={page <= 1}
            onClick={() => setPage(page - 1)}
          >
            Prev
          </button>

          <span style={{ margin: "0 1rem" }}>
            Page {page} / {Math.ceil(results.length / PAGE_SIZE) || 1}
          </span>

          <button
            disabled={page * PAGE_SIZE >= results.length}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default SearchPanel;