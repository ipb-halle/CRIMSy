import { User } from "../user/User";

export interface UsersListResponse {
  users: User[];
  currentPage: number;
  totalPages: number;
  totalUsers: number;
}
