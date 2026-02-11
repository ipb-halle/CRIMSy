
import { AuthResponse } from "../../adapters/api";
import { LogoutResponse } from "../../adapters/api";
import { LoginRequest } from "../../adapters/api";

export interface UserRepository {
  login(username: string, password: string): Promise<AuthResponse>;
  logout(token: string): Promise<LogoutResponse>;
  fetchRole(token: string): Promise<any>;
  fetchUsers(token: string, page: number): Promise<any>;
}
