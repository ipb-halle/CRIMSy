import React, { useState } from "react";
import FullSidebarFilters from "../components/filters/FullSidebarFilters";
import { Filters } from "../components/filters/Filters";
import CardView  from "../components/views/CardView";
import TableView from "../components/views/TableView";
import { dummyData, Material } from "../../data/dummyData";
import { filterRuls } from "../components/filters/filterRules";
import { colors } from "../theme/designTokens";

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

    const results = applyFilters();

    return (
      <div style={{ background: colors.background, minHeight: "100vh" }}>
    
      {/* Header */}
      <div 
        style={{
          background: colors.primary,
          color: "white",
          padding: "1rem 1.5rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{ fontSize: "1.25rem", fontWeight: 600 }}>
          IPB Biochemistry Institute - Laboratory Material System
        </div>

        <input
          type="text"
          placeholder="Search by Material ID..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            padding: "0.5rem 0.75rem",
            borderRadius: "6px",
            border: "none",
            width: "250px",
          }}
        />
      </div>
      
      {/* Layout */}
      <div style={containerStyle}>

        {/* Sidebar Filters (dropdown system) */}
        <div style={sidebarStyle}>
          <FullSidebarFilters
            onFilterChange={setFilters}
            isDropdown
          />
        </div>

 {/* Main Content */}
        <div style={contentStyle}>
        
      {/* View Toggle */}
      <div 
        style={{
          marginBottom: "1rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
      <div style={{ fontWeight: 600 }}>
        Results: {results.length}
      </div>

      <div>
        <button
          onClick={() => setView("card")}
          style={{
            marginRight: "0.5rem",
            padding: "0.4rem 0.8rem",
            borderRadius: "6px",
            border: "1px solid #ccc",
            background: view === "card" ? colors.accent : "white",
            color: view === "card" ? "white" : "black",
            cursor: "pointer",
          }}
        >
          Card View
        </button>
          
        <button
          onClick={() => setView("table")}
          style={{
            padding: "0.4rem 0.8rem",
            borderRadius: "6px",
            border: "1px solid #ccc",
            background: view === "table" ? colors.accent : "white",
            color: view === "table" ? "white" : "black",
            cursor: "pointer",
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
      </div>
    </div>
    </div>
  );
};

export default SearchPanel;
