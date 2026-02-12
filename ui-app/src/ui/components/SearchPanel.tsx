// SearchPanel.tsx
import React, { useState } from "react";
import { SearchDropdown } from "./SearchDropdown";
import { SearchType, MaterialType } from "../../adapters/api";
import "../../assets/css/searchTable.css";

const SearchPanel: React.FC = () => {
  const [searchTypes, setSearchTypes] = useState<SearchType[]>([]);
  const [materialTypes, setMaterialTypes] = useState<MaterialType[]>([]);
  const [submitted, setSubmitted] = useState(false);

  const handleSearchClick = () => {
    setSubmitted(true);
  };

  return (
    <div className="search-page">
      <h2>Unified Search</h2>

      <div
        className="search-form"
        style={{ display: "flex", gap: "10px", alignItems: "center" }}
      >
        <SearchDropdown
          onSearchTypesChange={setSearchTypes}
          onMaterialTypesChange={setMaterialTypes}
        />

        <button
          onClick={handleSearchClick}
          style={{
            padding: "6px 16px",
            backgroundColor: "#029ACF",
            color: "white",
            border: "none",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          Search
        </button>
      </div>

      {submitted && (
        <div
          style={{
            marginTop: "20px",
            padding: "15px",
            border: "1px solid #029ACF",
            borderRadius: "5px",
            background: "#f5fbff",
          }}
        >
          <strong>You selected:</strong>

          <div style={{ marginTop: "10px" }}>
            <div>
              <strong>Search Types:</strong>{" "}
              {searchTypes.length > 0 ? searchTypes.join(", ") : "None"}
            </div>

            <div>
              <strong>Material Types:</strong>{" "}
              {materialTypes.length > 0 ? materialTypes.join(", ") : "None"}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchPanel;
