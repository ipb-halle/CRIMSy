import { UserRepository } from "../../domain/user/UserRepository";
import * as api from "../../services/authService";

export class AuthApiRepository implements UserRepository {
  fetchUsers(token: string, page: number) {
    return api.fetchUsersAPI(token, page, 3);
  }
}
