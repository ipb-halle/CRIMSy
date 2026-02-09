import { useState } from "react";
import { SearchDropdown } from "../../ui/components/SearchDropdown"



interface SearchResult {
    domain: string;
    subtype?: string | null // only when "Material" is selected
    id: string;
    label: string;
}

const BASE = "https://compchem17.ipb-halle.de/ui/rest";

export const SearchPage = () => {
    const [query, setQuery] = useState("");
    const [searchTypes, setSearchTypes] = useState<string[]>([]);
    const [materialTypes, setMaterialTypes] = useState<string[]>([]);
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);


    const handleSearch = async () => {
        if (!query || searchTypes.length === 0) {
            alert("Please enter a queary and selectat least one search type.")
            return;
        }

        setLoading(true);

        const payload = {
            query,
            searchTypes,
            materialTypes: searchTypes.includes("Materials")
                ? materialTypes
                : undefined,
            page: 1,
            pageSize: 10,
        };
        try {
            const res = await fetch(`${BASE}/search`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getITem("token")}`,
                },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                throw new Error("Search failed");
            }

            const data = await res.json();
            setResults(data.results || []);
        } catch (err) {
            console.error(err);
            alert("Error while searching!");
        } finally {
            setLoading(false);
        }
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
                    onClick={handleSearch}
                    disabled={loading}>
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
