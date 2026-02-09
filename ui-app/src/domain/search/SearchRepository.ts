import { SearchResponse, SearchRequest } from "../types/search";

export interface SearchRepository {
    search(req: SearchRequest): Promise<SearchResponse>;
}