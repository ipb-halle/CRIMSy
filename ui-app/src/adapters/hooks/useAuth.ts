import { useState, useRef, FormEvent } from "react";
import { LoginRequest } from "../api";
import * as api from "../../services/authService";

const SESSION_FALLBACK_TIMEOUT_MS = 600 * 1000;

export const useAuth = () => {

  const [loginRequest, setLoginRequest] = useState<LoginRequest>({
    username: "",
    password: "",
  });

  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [usersList, setUsersList] = useState<any>(null);

  const logoutTimerRef = useRef<number | null>(null);

  const checkSession = async (): Promise<boolean> => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

    if (!token || !username) return false;

    try {
      await api.fetchRoleAPI(token); // verify token is valid
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

  const expireSession =
    (message: string) => {
      clearLogoutTimer();
      localStorage.removeItem("token");
      localStorage.removeItem("username");
      setIsLoggedIn(false);
      setResult({ message });
      setRoleInfo(null);
      setUsersList(null);
      setLoginRequest({ username: "", password: "", });
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
    if (!loginRequest.username.trim())
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
      const auth = await api.loginAPI(loginRequest);

      if (!auth.token) {
        setResult({ message: "Login failed,  no token received!" });
        return;
      }

      localStorage.setItem("token", auth.token);
      const user = await api.fetchRoleAPI(auth.token);
      if (user?.username) {
        localStorage.setItem("username", user.username);
      }
      setIsLoggedIn(true);
      startSessionTimer(auth.expiresInSeconds);
      if (onSuccess) onSuccess();
    } catch {
      setResult({ message: "Request failed. Please try again later." });
    }
  };

  const handleLogout = async () => {
    const token = localStorage.getItem("token");

    if (!token) return;
    try {
      await api.logoutAPI(token);
      expireSession("Logged out successfully");
    } catch (error) {
      expireSession("Logout failed");
      console.error(error);
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
      // const users = await api.fetchUsersAPI(token, page, 3);
      //setUsersList(users);
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