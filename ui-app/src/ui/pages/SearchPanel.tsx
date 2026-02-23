// src/ui/pages/SearchPanel.tsx
import React, { useState } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import { CardView } from "../components/views/CardView";
import { TableView } from "../components/views/TableView";
import { ViewModeToggle } from "../components/views/ViewModeToggle";
import { dummyData } from "../../data/dummyData";
import { filterRuls } from "../components/filters/filterRules";

const containerStyle: React.CSSProperties = {
  display: "flex",
  gap: "1rem",
  margin: "1rem",
  boxSizing: "border-box",
  flexWrap: "wrap",
};

const contentStyle: React.CSSProperties = {
  flex: 1,
  minWidth: "300px",
  maxWidth: "100%",
  overflow: "hidden",
};

export const SearchPanel: React.FC = () => {
  const [filters, setFilters] = useState<Filters>({});
  const [viewMode, setViewMode] = useState<"card" | "table">("card");

  // Generic filter function
  const applyFilters = () =>
    dummyData.materials.filter((item) =>
      filterRuls.every((rule) => {
        const selected = filters[rule.key];
        return !selected?.length || rule.matches(item, selected);
      })
    );

  const filteredMaterials = applyFilters();

  return (
    <div style={containerStyle}>
      {/* Sidebar dropdown filters */}
      <FullSidebarFilters
        onFilterChange={setFilters}
        style={{ minWidth: "250px", maxWidth: "300px" }}
        isDropdown
      />

      {/* Main content */}
      <div style={contentStyle}>
        <ViewModeToggle
          viewMode={viewMode}
          onToggle={setViewMode}
          style={{ marginBottom: "1rem" }}
        />

        {filteredMaterials.length === 0 ? (
          <div style={{ padding: "1rem", textAlign: "center", color: "#666" }}>
            No items match the selected filters.
          </div>
        ) : viewMode === "card" ? (
          <CardView items={filteredMaterials} />
        ) : (
          <div style={{ overflowX: "auto" }}>
            <TableView items={filteredMaterials} />
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchPanel;
