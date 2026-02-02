import { UserRepository } from "../../domain/user/UserRepository";

export class FetchUsersUseCase {
  constructor(private repo: UserRepository) {}
  execute(token: string, page: number) {
    return this.repo.fetchUsers(token, page);
  }
}