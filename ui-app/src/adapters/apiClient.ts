import { Configuration, AuthApi, UsersApi } from "./api";

/**
 * Single source of API configuration
 */
const createConfig = () =>
    new Configuration({
        accessToken: async () => localStorage.getItem("token") || ""
    });

export const authApi = () => new AuthApi(createConfig());
export const usersApi = () => new UsersApi(createConfig());