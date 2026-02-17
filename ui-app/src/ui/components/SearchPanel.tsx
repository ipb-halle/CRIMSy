// SearchPanel.tsx
import React, { useState } from "react";
import { SearchDropdown } from "./SearchDropdown";
import { SearchType, MaterialType, SearchRequest, Configuration, SearchApi, SearchResponse } from "../../adapters/api";
import "../../assets/css/searchTable.css";
import * as api from "../../services/searchService";
const BASE = "https://compchem17.ipb-halle.de/ui/rest";

const SearchPanel: React.FC = () => {
  const [searchTypes, setSearchTypes] = useState<SearchType[]>([]);
  const [materialTypes, setMaterialTypes] = useState<MaterialType[]>([]);

  const [searchResponse, setSearchResult] = useState<SearchResponse>();
  const [submitted, setSubmitted] = useState(false);


  const handleSearchClick = async () => {
    if (searchTypes.length === 0) {
      alert("At least one search type must be selected!")
      console.log("At least one search type must be selected!");
      return;
    }

    const searchTypesMapped = searchTypes.map((st => st.toUpperCase()));

    const materialTypesMapped = materialTypes.map((mt => mt.toUpperCase()));


    const searchRequest: SearchRequest = {
      searchTypes: searchTypesMapped as any,
      materialTypes:
        searchTypes.includes(SearchType.Materials)
          ? materialTypesMapped as any
          : [],
      query: "",
      page: 0,
      pageSize: 20,
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.log("token is empty!");
      return
    };

    try {
      const response = await api.searchAPI(searchRequest, token);
      //setSearchResult(response);
      setSubmitted(true);
    } catch (error) {
      console.error("Search failed: ", error);
      setSubmitted(false);
    }
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
            marginTop: "1rem",
            padding: "0.75rem",
            border: "1px solid #029ACF",
            borderRadius: "3px",
            textAlign: "left",
            fontWeight: "bold",
            wordBreak: "break-all"
          }}
        >
          <div>You selected:</div>

          <div style={{ marginLeft: "1rem", marginTop: "0.5rem" }}>
            <div>
              <strong>Domain:</strong>
              {searchTypes.length > 0 ? searchTypes.join(", ") : "None"}
            </div>

            <div>
              <strong>Material Types:</strong>
              {materialTypes.length > 0 ? materialTypes.join(", ") : "None"}
            </div>

            <div>
              <strong>ID:</strong>
              {"None"}

            </div>

            <div>
              <strong>Label:</strong>
              {"None"}
            </div>

          </div>
        </div>

      )}
    </div>
  );
};

export default SearchPanel;
