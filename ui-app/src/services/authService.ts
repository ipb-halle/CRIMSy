import { AuthResponse } from "../adapters/api";
import { RoleApi } from "../adapters/api";
import { Configuration } from "../adapters/api";
import { LoginRequest } from "../adapters/api";



const BASE = "https://compchem17.ipb-halle.de/ui/rest";
/*
export const loginAPI = async (login: string, password: string): Promise<AuthResponse> => {
  const res = await fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ login, password }),
  });
  return res.json();
};*/


export const loginAPI = async (loginRequest: LoginRequest): Promise<AuthResponse> => {
  const res = await fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(loginRequest),
  });
  return res.json();
};

export const logoutAPI = async (token: string) => {
  const res = await fetch(`${BASE}/auth/logout`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
  });

  return res.json();
};

export const checkSessionAPI = async (token: string) => {
  return fetch(`${BASE}/sessions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
  });
};

export const fetchRoleAPI = async (token: string) => {
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