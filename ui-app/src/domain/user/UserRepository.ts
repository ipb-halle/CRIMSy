export interface LoginResult {
  token?: string;
  username?: string;
  message: string;
  expiresInSeconds?: number;
}

export interface UserRepository {
  login(username: string, password: string): Promise<LoginResult>;
  logout(token: string): Promise<{ message: string }>;
  fetchRole(token: string): Promise<any>;
  fetchUsers(token: string, page: number): Promise<any>;
}
