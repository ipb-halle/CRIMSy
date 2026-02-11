import { SearchRepository } from "../../domain/search/SearchRepository";
import { SearchResponse, SearchRequest } from "../../adapters/api";

export class SearchUseCase {
    constructor(private repo: SearchRepository) { }

    execute(req: SearchRequest): Promise<SearchResponse> {
        return this.repo.search(req);
    }
}




