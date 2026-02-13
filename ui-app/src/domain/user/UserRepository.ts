
import { AuthResponse } from "../../adapters/api";
import { LogoutResponse } from "../../adapters/api";

export interface UserRepository {
  fetchUsers(token: string, page: number): Promise<any>;
}
