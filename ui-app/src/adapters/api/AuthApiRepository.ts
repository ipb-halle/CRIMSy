import { UserRepository } from "../../domain/user/UserRepository";
import * as api from "../../services/authService";

export class AuthApiRepository implements UserRepository {
  login(username: string, password: string) {
    return api.loginAPI(username, password);
  }

  logout(token: string) {
    return api.logoutAPI(token);
  }

  fetchRole(token: string) {
    return api.fetchRoleAPI(token);
  }

  fetchUsers(token: string, page: number) {
    return api.fetchUsersAPI(token, page, 3);
  }
}
