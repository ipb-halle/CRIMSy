
import { AuthResponse } from "../../adapters/api";
import { LogoutResponse } from "../../adapters/api";

export interface UserRepository {
  login(username: string, password: string): Promise<AuthResponse>;
  logout(token: string): Promise<LogoutResponse>;
  fetchUsers(token: string, page: number): Promise<any>;
}
