import { AuthApi } from "../adapters/api";
import { Configuration } from "../adapters/api";
import { AuthToken, LoginRequest, AuthUser, LogoutResponse } from "../adapters/api/models";


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
  //alert("Logged out Successfully!\n Click to continue!");
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


function expireSession(arg0: string) {
  throw new Error("Function not implemented.");
}
/*export const fetchUsersAPI = async (
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

};*/