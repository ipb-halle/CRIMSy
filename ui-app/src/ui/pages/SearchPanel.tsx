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
  const [sortKey, setSortKey] = useState<"id" | "project" | "server">("id");
  const [sortAsc, setSortAsc] = useState(true);

  //  Apply all filters + search term
  const applyFilters = (): Material[] =>
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
  // Sorting
  const sortMaterials = (materials: Material[]) => {
    return [...materials].sort((a, b) => {
      let valA: any = a.materialId;
      let valB: any = b.materialId;

      if (sortKey === "project") {
        valA = a.projectId;
        valB = b.projectId;
      } else if (sortKey === "server") {
        valA = a.serverId;
        valB = b.serverId;
      }

      return sortAsc ? valA - valB : valB - valA;
    });
  };
 
  const filtered = sortMaterials(applyFilters());

  // Pagination
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
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

  const results = applyFilters();

  return (
    <div style={{ background: colors.background, minHeight: "100vh" }}>

      {/* Navigation  */}
      <div
        style={{
          background: colors.primary,
          color: "white",
          padding: "0.75rem 1.25rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{
          display: "flex", alignItems: "center", gap: "0.75rem"
        }}>
          <div
            style={{
              width: "32px",
              height: "32px",
              background: "white",
              borderRadius: "4px",
            }}
          />
          <span style={{ fontWeight: 600 }}>
            IPB Biochemistry Institute - Laboratory Material System
          </span>
        </div>
        <button
          onClick={exportCSV}
          style={{
            padding: "0.4rem 0.8rem",
            borderRadius: "6px",
            border: "none",
            background: colors.accent,
            color: "white",
            cursor: "pointer",
          }}
        >
          Export CSV
        </button>
      </div>

      {/* Header Search */}
      <div
        style={{
          padding: "1rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <input
          type="text"
          placeholder="Search by Material ID..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setPage(1);
          }}
          style={{
            padding: "0.5rem 0.75rem",
            borderRadius: "6px",
            border: "1px solid #ccc",
            width: "250px",
          }}
        />

      </div>

      {/* Layout */}
      <div style={{ display: "flex", gap: "1rem", padding: "0 1rem" }}>

        {/* Sidebar Filters  */}
        <div style={{ minWidth: "250px", maxWidth: "300px" }}>
          <FullSidebarFilters
            onFilterChange={setFilters}
            isDropdown
          />
        </div>

        {/* Results  */}
        <div style={{ flex: 1 }}>

          {/* View Toggle */}
          <div
            style={{
              marginBottom: "1rem",
              display: "flex",
              justifyContent: "space-between",
            }}
          >
            <div>
              Results: {results.length}
            </div>

            <div>
              <button
                onClick={() => setView("card")}
                style={{
                  marginRight: "0.5rem",
                  background: view === "card" ? colors.accent : "white",
                  color: view === "card" ? "white" : "black",
                }}
              >
                Card View
              </button>

              <button
                onClick={() => setView("table")}
                style={{
                  background: view === "table" ? colors.accent : "white",
                  color: view === "table" ? "white" : "black",
                }}
              >
                Table View
              </button>
            </div>
          </div>

          {/* Results */}
          {view === "card" ? (
            <CardView items={results} />
          ) : (
            <TableView items={results} />
          )}

          {/* Pagination */}
          <div style={{ marginTop: "1rem", textAlign: "center" }}>
            {Array.from({ length: totalPages }).map((_, i) => (
              <button
                key={i}
                onClick={() => setPage(i + 1)}
                style={{
                  margin: "0 0.25rem",
                  padding: "0.3rem 0.6rem",
                  borderRadius: "4px",
                  border: "1px solid #ccc",
                  background: page === i + 1 ? colors.accent : "white",
                  color: page === i + 1 ? "white" : "black",
                }}
              >
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
