
import { AuthResponse } from "../../adapters/api";

export interface UserRepository {
  login(username: string, password: string): Promise<AuthResponse>;
  logout(token: string): Promise<{ message: string }>;
  fetchRole(token: string): Promise<any>;
  fetchUsers(token: string, page: number): Promise<any>;
}
