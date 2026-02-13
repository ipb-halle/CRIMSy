import { useState, useRef, useEffect, FormEvent } from "react";
import { AuthApiRepository } from "../api/AuthApiRepository";
import { FetchUsersUseCase } from "../../application/user/FetchUsersUseCase";
import type { AuthResponse } from "../api";
import * as api from "../../services/authService";

const SESSION_FALLBACK_TIMEOUT_MS = 600 * 1000;

export const useAuth = () => {
  const repo = new AuthApiRepository();
  const usersUC = new FetchUsersUseCase(repo);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<AuthResponse | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [usersList, setUsersList] = useState<any>(null);

  const logoutTimerRef = useRef<number | null>(null);

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
    setUsername("");
    setPassword("");
  };

  const startSessionTimer = (expiresInSeconds?: number) => {
    clearLogoutTimer();
    const timeoutMs = expiresInSeconds ? expiresInSeconds * 1000 : SESSION_FALLBACK_TIMEOUT_MS;
    logoutTimerRef.current = window.setTimeout(() => {
      expireSession("Session expired. Please log in again.");
    }, timeoutMs);
  };

  useEffect(() => {
    const checkSession = async () => {
      const token = localStorage.getItem("token");
      const storedUsername = localStorage.getItem("username");

      if (!token || !storedUsername) return;

      try {
        await api.fetchRoleAPI(token);
        setIsLoggedIn(true);
        setResult({ message: `Welcome back, ${storedUsername}!` });
        startSessionTimer();
      } catch {
        expireSession("Session expired. Please log in again.");
      }
    };

    checkSession();
    return () => clearLogoutTimer();
  }, []);

  const validate = () => {
    const newErrors: typeof errors = {};
    if (!username.trim()) newErrors.username = "Username required";
    if (!password.trim()) newErrors.password = "Password required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async (e?: FormEvent) => {
    if (e) e.preventDefault();
    if (!validate()) return;

    setResult(null);
    try {
      const data = await api.loginAPI(username, password);
      if (!data.token || !data.username) {
        setResult({
          message: data.message ?? "Login failed"
        });
        return;
      }

      localStorage.setItem("token", data.token);
      localStorage.setItem("username", data.username);
      setIsLoggedIn(true);

      setResult({ message: data.message });
      setRoleInfo(null);
      setUsersList(null);
      startSessionTimer(data.expiresInSeconds);
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
      const users = await usersUC.execute(token, page);
      setUsersList(users);
      setRoleInfo(null);
      setResult({ message: "Users List" });
    } catch {
      setUsersList(null);
    }
  };

  return {
    username,
    password,
    setUsername,
    setPassword,
    errors,
    result,
    isLoggedIn,
    roleInfo,
    usersList,
    handleLogin,
    handleLogout,
    handleCheckRole,
    handleFetchUsers,
  };
};