import { UserRepository } from "../../domain/user/UserRepository";

export class LogoutUseCase {
  constructor(private repo: UserRepository) {}
  execute(token: string) {
    return this.repo.logout(token);
  }
}
