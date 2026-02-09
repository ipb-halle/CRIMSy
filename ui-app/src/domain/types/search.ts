export interface SearchRequest {
    query: string;
    searchTypes: string[];
    materialTypes?: string[];
    page?: number;
    pageSize?: number;
}

export interface SearchResult {
    domain: string;
    subtype?: string | null; // only when "Material" is selected
    id: string;
    label: string;
}

export interface SearchResponse {
    total: number;
    results: SearchResult[];
}