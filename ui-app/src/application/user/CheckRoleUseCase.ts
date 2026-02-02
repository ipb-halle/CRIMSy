import { UserRepository } from "../../domain/user/UserRepository";

export class CheckRoleUseCase {
  constructor(private repo: UserRepository) {}
  execute(token: string) {
    return this.repo.fetchRole(token);
  }
}
