import { useState, useRef, useEffect, FormEvent } from "react";
import { AuthApiRepository } from "../api/AuthApiRepository";
import { LoginUseCase } from "../../application/user/LoginUseCase";
import { LogoutUseCase } from "../../application/user/LogoutUseCase";
import { CheckRoleUseCase } from "../../application/user/CheckRoleUseCase";
import { FetchUsersUseCase } from "../../application/user/FetchUsersUseCase";

const SESSION_FALLBACK_TIMEOUT_MS = 60 * 1000;

export const useAuth = () => {
  const repo = new AuthApiRepository();
  const loginUC = new LoginUseCase(repo);
  const logoutUC = new LogoutUseCase(repo);
  const roleUC = new CheckRoleUseCase(repo);
  const usersUC = new FetchUsersUseCase(repo);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
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
        await repo.fetchRole(token); // simple validation
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
      const data = await loginUC.execute(username, password);
      if (!data.token || !data.username) {
        setResult(data);
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
      const data = await logoutUC.execute(token);
      expireSession(data.message);
    } catch {
      expireSession("Logout failed");
    }
  };

  const handleCheckRole = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
      const role = await roleUC.execute(token);
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