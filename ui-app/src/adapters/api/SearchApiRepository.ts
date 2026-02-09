import { SearchRepository } from "../../domain/search/SearchRepository";
import { SearchRequest, SearchResponse } from "../../domain/types/search";

const BASE = "https://compchem17.ipb-halle.de/ui/rest";

export class SearchApiRepository implements SearchRepository {
    async search(req: SearchRequest): Promise<SearchResponse> {
        const token = localStorage.getItem("token") || "";

        const res = await fetch(`${BASE}/search`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}}`,
            },
            body: JSON.stringify(req),
        });

        if (!res.ok) {
            throw new Error("Search failed");
        }
        const data: SearchResponse = await res.json();
        return data;

    }
}