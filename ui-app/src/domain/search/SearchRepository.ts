import { SearchResponse, SearchRequest } from "../../adapters/api";

export interface SearchRepository {
    search(req: SearchRequest): Promise<any>;
}