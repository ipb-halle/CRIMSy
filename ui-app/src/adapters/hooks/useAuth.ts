import { useState, useRef, useEffect, FormEvent } from "react";
import { LoginRequest, type AuthResponse } from "../api";
import * as api from "../../services/authService";
import { fetchRoleAPI } from "../../services/authService";

const SESSION_FALLBACK_TIMEOUT_MS = 600 * 1000;

export const useAuth = () => {

  const [loginRequest, setLoginRequest] = useState<LoginRequest>({ login: "", password: "", });
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<AuthResponse | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [usersList, setUsersList] = useState<any>(null);

  const logoutTimerRef = useRef<number | null>(null);

  const checkSession = async (): Promise<boolean> => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

    if (!token || !username) return false;

    try {
      await fetchRoleAPI(token); // verify token is valid
      setIsLoggedIn(true);
      setResult({ message: `Welcome back, ${username}!` });
      startSessionTimer(); // restart session timer
      return true;
    } catch {
      expireSession("Session expired. Please log in again.");
      return false;
    }
  };

  const clearLogoutTimer = () => {
    if (logoutTimerRef.current !== null) {
      clearTimeout(logoutTimerRef.current);
      logoutTimerRef.current = null;
    }
  };

  const expireSession = (message: string) => {
    clearLogoutTimer();
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    setIsLoggedIn(false);
    setResult({ message });
    setRoleInfo(null);
    setUsersList(null);
    setLoginRequest({
      login: "",
      password: "",
    });

  };

  const startSessionTimer = (expiresInSeconds?: number) => {
    clearLogoutTimer();
    const timeoutMs = expiresInSeconds ? expiresInSeconds * 1000 : SESSION_FALLBACK_TIMEOUT_MS;
    logoutTimerRef.current = window.setTimeout(() => {
      expireSession("Session expired. Please log in again.");
    }, timeoutMs);
  };

  const validate = () => {
    const newErrors: typeof errors = {};
    if (!loginRequest.login.trim())
      newErrors.username = "Username required";
    if (!loginRequest.password.trim())
      newErrors.password = "Password required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async (e?: FormEvent, onSuccess?: () => void) => {
    if (e) e.preventDefault();
    if (!validate()) return;

    setResult(null);

    try {
      const data = await api.loginAPI(loginRequest);

      if (!data.token || !data.username) {
        setResult({
          message: data.message ?? "Login failed, No response from server!"
        });
        return;
      }

      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      setIsLoggedIn(true);
      setResult({ message: data.message });

      /*      setRoleInfo(null);
            setUsersList(null);*/
      startSessionTimer(data.expiresInSeconds);
      if (onSuccess) onSuccess();
    } catch {
      setResult({ message: "Request failed. Please try again later." });
    }
  };

  const handleLogout = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
      const data = await api.logoutAPI(token);
      expireSession(data.message);
    } catch {
      expireSession("Logout failed");
    }
  };

  const handleCheckRole = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
      const role = await api.fetchRoleAPI(token);
      setRoleInfo(role);
      setUsersList(null);
      setResult({ message: "Your Role" });
    } catch {
      setRoleInfo(null);
    }
  };

  const handleFetchUsers = async (page: number = 1) => {
    setUsersList(null);
    setRoleInfo(null);

    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      const users = await api.fetchUsersAPI(token, page, 3);
      setUsersList(users);
      setRoleInfo(null);
      setResult({ message: "Users List" });
    } catch {
      setUsersList(null);
    }
  };

  return {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    isLoggedIn,
    roleInfo,
    usersList,
    handleLogin,
    handleLogout,
    handleCheckRole,
    handleFetchUsers,
    checkSession,
  };
};