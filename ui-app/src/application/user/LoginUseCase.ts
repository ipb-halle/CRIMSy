import { UserRepository } from "../../domain/user/UserRepository";

export class LoginUseCase {
  constructor(private repo: UserRepository) {}

  execute(username: string, password: string) {
    return this.repo.login(username, password);
  }
}
