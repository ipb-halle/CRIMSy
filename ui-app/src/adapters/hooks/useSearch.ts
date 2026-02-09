// src/adapters/hooks/useSearch.ts
import { useState } from "react";
import { SearchUseCase } from "../../application/search/SearchUseCase";
import { SearchApiRepository } from "../api/SearchApiRepository";
import { SearchResult, SearchRequest } from "../../domain/types/search";

export const useSearch = () => {
    const repo = new SearchApiRepository();
    const searchUC = new SearchUseCase(repo);

    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (req: SearchRequest) => {
        setLoading(true);
        try {
            const data = await searchUC.execute(req);
            setResults(data.results);
        } finally {
            setLoading(false);
        }
    };

    return {
        results,
        loading,
        handleSearch,
    };
};
