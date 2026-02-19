// src/ui/pages/SearchPanel.tsx
import React, { useState } from "react";
import FullSidebarFilters, { Filters } from "../components/filters/FullSidebarFilters";
import { CardView } from "../components/views/CardView";
import { TableView } from "../components/views/TableView";
import { ViewModeToggle } from "../components/views/ViewModeToggle";
import { dummyData } from "../../data/dummyData";

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
  const applyFilters = () => {
    return dummyData.materials.filter((item) => {
      if (filters.materials && filters.materials.length > 0 && !filters.materials.includes(item.name)) return false;
      if (filters.materialTypes && filters.materialTypes.length > 0 && !filters.materialTypes.includes(item.type)) return false;
      if (filters.projects && filters.projects.length > 0 && !filters.projects.includes(item.project)) return false;
      if (filters.users && filters.users.length > 0 && !filters.users.includes(item.user)) return false;
      if (filters.containers && filters.containers.length > 0 && !filters.containers.includes(item.container)) return false;
      if (filters.storageClasses && filters.storageClasses.length > 0 && !filters.storageClasses.includes(item.storageClass)) return false;
      if (filters.hazards && filters.hazards.length > 0 && !filters.hazards.includes(item.hazard)) return false;
      if (filters.deactivationStatus && filters.deactivationStatus.length > 0 && !filters.deactivationStatus.includes(item.deactivationStatus)) return false;
      return true;
    });
  };

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
