import { AuthApi } from "../adapters/api";
import { Configuration } from "../adapters/api";
import { AuthToken, LoginRequest, User } from "../adapters/api/models";

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

export const logoutAPI = async (
  token: string
): Promise<void> => {
  const config = new Configuration({
    accessToken: async () => token
  });
  const api = new AuthApi(config);

  await api.logout();
};

export const checkSessionAPI = async (
  token: string
): Promise<User> => {
  const api = new AuthApi(
    new Configuration({
      accessToken: async () => token
    })
  );

  return await api.authMeGet();
};

export const fetchRoleAPI = async (
  token: string
): Promise<User> => {
  const config = new Configuration({
    accessToken: async () => token
  });

  const roleApi = new AuthApi(config);

  const roleResponse = await roleApi.authMeGet();
  return roleResponse;
};

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