import React, { useState } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import CardView from "../components/views/CardView";
import TableView from "../components/views/TableView";
import { dummyData, Material } from "../../data/dummyData";
import { filterRuls } from "../components/filters/filterRules";
import { colors } from "../theme/designTokens";


const PAGE_SIZE = 12;

const containerStyle: React.CSSProperties = {
  display: "flex",
  gap: "1rem",
  margin: "1rem",
  boxSizing: "border-box",
  flexWrap: "wrap",
};

const sidebarStyle: React.CSSProperties = {
  minWidth: "250px",
  maxWidth: "300px",
  flexShrink: 0,
};

const contentStyle: React.CSSProperties = {
  flex: 1,
  minWidth: "300px",
  maxWidth: "100%",
  overflow: "hidden",
};

const SearchPanel: React.FC = () => {
  const [filters, setFilters] = useState<Filters>({});
  const [view, setView] = useState<"card" | "table">("card");
  const [searchTerm, setSearchTerm] = useState("");
  const [page, setPage] = useState(1);

  //  Apply all filters + search term
  const applyFilters = () =>
    dummyData.materials
      .filter(material =>
        filterRuls.every(rule => {
          const selected = (filters as any)[rule.key];
          return !selected?.length || rule.matches(material, selected);
        })
      )
      .filter(material =>
        material.materialId.toString().includes(searchTerm)
      );


  const filtered = applyFilters();

  // Pagination
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // CSV Export
  const exportCSV = () => {
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
  };

  return (
    <div>
      {/* Sidebar Filters */}
      <div style={{ display: "flex", gap: "1rem" }}>
        <FullSidebarFilters
          onFilterChange={setFilters}
          isDropdown
        />

        {/* Results  */}
        <div style={{ flex: 1 }}>

          {/* View Toggle */}
          <div style={{ marginBottom: "1rem" }}>
            <input
              placeholder="Search ID..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setPage(1);
              }}
            />
          </div>

          <div style={{ marginBottom: "1rem" }}>
            <button onClick={() => setView("card")}>
              Card View
            </button>

            <button
              onClick={() => setView("table")} >
              Table View
            </button>
          </div>

          {/* Results */}
          {view === "card" ? (
            <CardView items={paged} />
          ) : (
            <TableView items={paged} />
          )}

          {/* Pagination */}
          <div style={{ marginTop: "1rem" }}>
            {Array.from({ length: Math.ceil(filtered.length / PAGE_SIZE) }).map(
              (_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i + 1)}>
                  {i + 1}
                </button>
              ))}
          </div>
        </div>
      </div>
    </div >
  );
};

export default SearchPanel;
