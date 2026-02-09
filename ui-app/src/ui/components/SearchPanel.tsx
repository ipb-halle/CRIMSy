import React, { useState } from "react";
import { SearchDropdown } from "./SearchDropdown";
import { useSearch } from "../../adapters/hooks/useSearch";
import { SearchRequest } from "../../domain/types/search";
import "../../assets/css/searchTable.css"

interface Props {
    initialQuery?: string;
}

const SearchPanel: React.FC = () => {
    const { results, loading, handleSearch } = useSearch();

    const [query, setQuery] = useState("");
    const [searchTypes, setSearchTypes] = useState<string[]>([]);
    const [materialTypes, setMaterialTypes] = useState<string[]>([]);

    const onSubmit = () => {
        if (!query || searchTypes.length === 0) {
            alert("Please enter a queary and selectat least one search type.")
            return;
        }

        const req: SearchRequest = {
            query,
            searchTypes,
            materialTypes: searchTypes.includes("Materials")
                ? materialTypes
                : undefined,
            page: 1,
            pageSize: 10,
        };

        handleSearch(req);
    };

    return (
        <div className="search-page">
            <h2>Unified Search</h2>

            {/* Search form */}
            <div className="search-form">
                <input
                    type="text"
                    placeholder="Search..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                />
                <SearchDropdown
                    onSearchTypesChange={setSearchTypes}
                    onMaterialTypesChange={setMaterialTypes}
                />

                <button
                    onClick={onSubmit}>
                    {loading ? "Searching..." : "Search"}
                </button>
            </div>

            {/* Results table */}
            <table className="search-table">
                <thead className="search-table__head">
                    <tr className="search-table__row search-table__row--header">
                        <th className="search-table__cell search-table__cell--header">Domain</th>
                        <th className="search-table__cell search-table__cell--header">SubType</th>
                        <th className="search-table__cell search-table__cell--header">ID</th>
                        <th className="search-table__cell search-table__cell--header">Label</th>
                    </tr>
                </thead>

                <tbody className="search-table__body">
                    {results.length === 0 && !loading && (
                        <tr>
                            <td
                                colSpan={4}
                                className="search-table__cell search-table__cell--empty"
                            >
                                No results
                            </td>
                        </tr>
                    )}

                    {results.map((r) => (
                        <tr key={r.id} className="search-table__row">
                            <td className="search-table__cell">
                                <span className={`domain-pill domain-pill--${r.domain.toLowerCase()}`}>
                                    {r.domain}
                                </span>
                            </td>

                            <td className="search-table__cell">
                                {r.subtype ? (
                                    <span className="subtyoe-badge">{r.subtype}</span>
                                ) : (
                                    <span className="subtyoe-badge subtyoe-badge--empty">-</span>
                                )}
                            </td>

                            <td className="search-table__cell search.table__cell--mono">
                                {r.id}
                            </td>

                            <td className="search-table__cell search.table__cell--label">
                                {r.label}
                            </td>

                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default SearchPanel;
