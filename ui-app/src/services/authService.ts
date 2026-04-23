import { AuthApi, UsersApi } from "../adapters/api";
import { Configuration } from "../adapters/api";
import {
  AuthToken,
  LoginRequest,
  AuthUser,
  LogoutResponse,
  PaginatedUserResponse,
  DeleteUser200Response,
  UserSummary
} from "../adapters/api/models";


let BASE = "https://compchem17.ipb-halle.de/ui/rest/auth/logout";

export const loginAPI = async (
  loginRequest: LoginRequest
): Promise<AuthToken> => {

  const config = new Configuration({});
  const loginApiInstance = new AuthApi(config);

  const response = await loginApiInstance.login({
    loginRequest
  });
  return response;
};

export const logoutAPII = async (token: string): Promise<LogoutResponse> => {
  if (!token) throw new Error("No token found");

  const config = new Configuration({
    accessToken: async (_name?: string, _scopes?: string[]) => token
  });

  const api = new AuthApi(config);

  try {
    const res = await api.logout();
    if (!res.message) {
      throw new Error(`Logout failed: ${res.message}`);
    }
    return res;

  } catch (error) {
    throw new Error(`Logout failed: ${error}`);
  }
};


export const logoutAPI = async (token: string): Promise<LogoutResponse> => {
  if (!token) throw new Error("No token found");

  const res = await fetch(`${BASE}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
  });

  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(`Logout failed: ${res.status} ${errorText}`);
  }

  const responseBody = await res.text();
  if (!responseBody) {
    throw new Error("Logout response is empty");
  }

  const data = JSON.parse(responseBody);
  return data;
};

export const checkSessionAPI = async (
  token: string
): Promise<AuthUser> => {
  const api = new AuthApi(
    new Configuration({
      accessToken: async () => token
    })
  );

  return await api.getCurrentUser();
};

export const fetchRoleAPI = async (
  token: string
): Promise<AuthUser> => {
  const config = new Configuration({
    accessToken: async () => token
  });

  const roleApi = new AuthApi(config);

  const authUser = await roleApi.getCurrentUser();
  return authUser;
};

export const fetchUsersAPI = async (
  token: string,
  page: number, pageSize: number
): Promise<PaginatedUserResponse> => {
  const config = new Configuration({
    accessToken: async () => token
  });

  const userResponse = new UsersApi(config);

  const paginatedUserResponse = await userResponse.getUsersList({ page, pageSize });
  return paginatedUserResponse;
};

export const deleteUsersAPI = async (
  token: string,
  id: number
): Promise<DeleteUser200Response> => {

  if (!token) {
    throw new Error("Missing token");
  }

  const config = new Configuration({
    accessToken: async () => {
      return token;
    }
  });

  const api = new UsersApi(config);

  try {
    const res = await api.deleteUser({ id });
    console.log("API delete success; ", res);
    return res;
  } catch (err) {
    console.error("API delete failed; ", err);
    throw err;
  }
};

export const updateUserAPI = async (
  token: string,
  id: number,
  userSummary: UserSummary
): Promise<UserSummary> => {
  if (!token) {
    throw new Error("Missing token");
  }

  const config = new Configuration({
    accessToken: async () => token
  });

  const api = new UsersApi(config);

  try {
    const res = await api.updateUser({
      id,
      userSummary
    });

    console.log("API update success; ", res);
    return res;
  } catch (err) {
    console.error("API update failed; ", err);
    throw err;
  }
};
