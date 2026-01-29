// src/services/authService.ts
export interface LoginResult {
  message: string;
  username?: string;
  token?: string;
  expiresInSeconds?: number;
}

const BASE = "https://compchem17.ipb-halle.de/ui/rest";

export const loginAPI = async (login: string, password: string): Promise<LoginResult> => {
  const res = await fetch(`${BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ login, password }),
  });
  return res.json();
};

export const logoutAPI = async (token: string) => {
   const res = await fetch(`${BASE}/auth/logout`, {
    method: "POST",
    headers: { 
      "Content-Type": "application/json",
       Authorization: `Bearer ${token}` },
  });

  return res.json();
};

export const checkSessionAPI = async (token: string) => {
  return fetch(`${BASE}/sessions`, {
    method: "POST",
    headers: { 
      "Content-Type": "application/json",
       Authorization: `Bearer ${token}` },
  });
};

export const fetchRoleAPI = async (token: string) => {
  const res = await fetch(`${BASE}/role`, {
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
  });
  return res.json();
};

export const fetchUsersAPI = async (token: string) => {
  const res = await fetch(`${BASE}/usersList`, {
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
  });
  return res.json();
};
