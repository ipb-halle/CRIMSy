export interface User {
    id: string;
    name: string;
    membertype: string;
    info?: string;
}

export interface UsersListResponse {
    totalUsers: number;
    totalPages: number;
    currentPage: number;
    users: User[];
}