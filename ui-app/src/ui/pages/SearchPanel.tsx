import React, { useState, useMemo } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import CardView from "../components/views/CardView";
import TableView from "../components/views/TableView";
import { materialRepository } from "../../domain/repositories/InMemoryMaterialRepository";
import { MaterialService } from "../../domain/material/MaterialService";
import { filterRuls } from "../components/filters/filterRules";
import styles from "../../assets/css/components/SearchPanel.module.css";
import ExportDropdown from "../components/ExportDropdown";

const PAGE_SIZE = 6;
const materialService = new MaterialService(materialRepository);

const SearchPanel: React.FC = () => {
  const [filters, setFilters] = useState<Filters>({});
  const [view, setView] = useState<"card" | "table">("card");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [sort, setSort] = useState<"id" | "project">("id");
  const [sidebarOpen, setSidebarOpen] = useState(false);


  // Apply all filters + search term
  const results = useMemo(() => {
    return materialService
      .getAllMaterials()
      .filter((material) =>
        filterRuls.every((rule) => {
          const selected = (filters as any)[rule.key];
          return !selected?.length || rule.matches(material, selected);
        })
      )
      .filter(material => {
        if (!search) return true;
        const query = search.toLocaleLowerCase();
        return (
          material.materialId.toString().includes(query) ||
          material.materialName?.toLowerCase().includes(query)
        );
      })
      .sort((a, b) =>
        sort === "id"
          ? a.materialId - b.materialId
          : a.projectId - b.projectId
      );
  }, [filters, search, sort]);

  // Pagination
  const paged = results.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const filterSummary = Object.entries(filters)
    .filter(([_, v]) => (v as any[])?.length)
    .map(([k, v]) => `${k}: ${(v as any[]).length}`)
    .join(" • ");

  return (
    <div className={styles.layout}>

      {/* Desktop Sidebar */}
      <aside
        className={styles.sidebar}>
        <div className={styles.card}>
          <FullSidebarFilters onFilterChange={setFilters} />
        </div>
      </aside>

      {/* MOBILE DRAWER OVERLAY */}
      {sidebarOpen && (
        <div
          onClick={() => setSidebarOpen(false)}
          className={styles.drawerOverlay}
        />
      )}

      {/* Mobile Drawer */}
      <div
        className={`${styles.mobileDrawer} ${sidebarOpen ? styles.open : ""}`}>
        <button
          className={styles.closeDrawer}
          onClick={() => setSidebarOpen(false)}
        >
          Close
        </button>
        <FullSidebarFilters onFilterChange={setFilters} />
      </div>

      {/* Results Area */}
      <main className={styles.results}>

        {/* Mobile Filter Button */}
        <button
          onClick={() => setSidebarOpen(true)}
          className={styles.mobileOnly}
        >
          Show Filters
        </button>

        {/* Filter summary */}
        {filterSummary && (
          <div className={styles.filterSummary}>
            Filters: {filterSummary}
          </div>
        )}

        {/* View & Search Controls */}
        <div className={styles.controls}>
          <input
            className={styles.input}
            placeholder="Search Material ..."
            value={search}
            onChange={(e) => {
              setPage(1);
              setSearch(e.target.value);
            }}
          />

          <select
            className={styles.select}
            value={sort}
            onChange={(e) => setSort(e.target.value as any)}
          >
            <option value="id">Sort by ID</option>
            <option value="project">Sort by Project</option>
          </select>

          {/* Result Badge */}
          <div className={styles.resultBadge}>
            {results.length} results
          </div>

          {/* Export */}
          <ExportDropdown items={results} />
        </div>

        {/* View Section */}
        <div className={styles.viewSwitch}>
          <button
            type="button"
            onClick={() => setView("card")}
            className={view === "card" ? styles.activeView : ""}
          >
            Card View
          </button>

          <button
            type="button"
            onClick={() => setView("table")}
            className={view === "table" ? styles.activeView : ""}
          >
            Table View
          </button>
        </div>

        {/* Results */}
        <div className={styles.resultsGrid}>
          {results.length === 0 ? (
            <div>No results found! </div>
          ) : view === "card" ? (
            <CardView items={paged} />
          ) : (
            <TableView items={paged} />
          )}
        </div>

        {/* Pagination */}
        <div className={styles.pagination}>
          <button
            disabled={page <= 1}
            onClick={() => setPage(page - 1)}
          >
            Prev
          </button>

          <span>
            Page {page} /{""}
            {Math.ceil(results.length / PAGE_SIZE) || 1}
          </span>

          <button
            disabled={page * PAGE_SIZE >= results.length}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
        </div>
      </main>
    </div >
  );
};

export default SearchPanel;