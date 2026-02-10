import { SearchRepository } from "../../domain/search/SearchRepository";
//import { SearchRequest, SearchResponse } from "../../domain/types/search";
import { SearchResponse, SearchRequest } from "../../adapters/api";

export class SearchUseCase {
    constructor(private repo: SearchRepository) { }

    execute(req: SearchRequest): Promise<SearchResponse> {
        return this.repo.search(req);
    }
}




