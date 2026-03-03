import { LogoutApi, RoleApi } from "../adapters/api";
import { LoginApi } from "../adapters/api/apis/LoginApi";
import { Configuration } from "../adapters/api";
import { AuthResponse, LoginRequest } from "../adapters/api/models";

const BASE = "https://compchem17.ipb-halle.de/ui/rest";

export const loginAPI = async (loginRequest: LoginRequest): Promise<AuthResponse> => {

  const config = new Configuration({});

  const loginApiInstance = new LoginApi(config);

  const response = await loginApiInstance.login({ loginRequest });
  return response;

};

export const logoutAPI = async (
  token: string
) => {
  const res = await fetch(`${BASE}/auth/logout`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
  });

  return res.json();
};

export const checkSessionAPI = async (
  token: string
) => {
  return fetch(`${BASE}/sessions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
  });
};

export const fetchRoleAPI = async (
  token: string
) => {
  const config = new Configuration({
    accessToken: async () => token
  });

  const roleApi = new RoleApi(config);

  const roleResponse = await roleApi.getRoleInfo();
  return roleResponse;
}

export const fetchUsersAPI = async (
  token: string,
  page: number, pageSize: number
) => {
  const res = await fetch(
    `${BASE}/users?page=${page}&pageSize=${pageSize}`,
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
    }
  );
  return res.json();
};