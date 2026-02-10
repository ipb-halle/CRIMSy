import { User } from "../../adapters/api/models/User";

export interface UsersListResponse {
  users: User[];
  currentPage: number;
  totalPages: number;
  totalUsers: number;
}
