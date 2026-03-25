import { useState, useRef, FormEvent, useEffect, use } from "react";
import { LoginRequest } from "../api";
import * as api from "../../services/authService";

const SESSION_CHECK_INTERVAL_MS = 30 * 1000;

export const useAuth = (onAutoLogout?: () => void) => {
  const [loginRequest, setLoginRequest] = useState<LoginRequest>({ username: "", password: "", });
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [usersList, setUsersList] = useState<any>(null);
  const [remainingTime, setRemainingTime] = useState<number>(0);
  const intervalRef = useRef<number | null>(null);


  const clearTimers = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
  };

  const expireSession = (message: string) => {
    clearTimers();
    setResult({ message });
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("tokenExpiry");

    setRoleInfo(null);
    setUsersList(null);
    setLoginRequest({ username: "", password: "", });
    if (onAutoLogout) onAutoLogout();
  };

  const checkSession = async (): Promise<boolean> => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

    if (!token || !username) {
      return false;
    }

    try {
      const user = await api.fetchRoleAPI(token);
      setRoleInfo(user);
      const expiresAt = user.expiresAt as string | Date;


      const expireInSeconds = new Date(expiresAt).getTime();
      // localStorage.setItem("tokenExpiry", expireInSeconds.toString());
      const remainingSeconds = Math.max((expireInSeconds - Date.now()) / 1000, 0);
      setRemainingTime(remainingSeconds);

      if (remainingSeconds <= 0) {
        expireSession("Session expired! Please log in again.");
        return false;
      }

      return true;
    } catch {
      expireSession("Session expired. Please log in again.");
      return false;
    }
  };

  const startSessionTimer = (expiresInSeconds: number) => {
    clearTimers();
    setRemainingTime(expiresInSeconds);

    intervalRef.current = window.setInterval(async () => {
      const valid = await checkSession();
      if (!valid) clearTimers();
    }, SESSION_CHECK_INTERVAL_MS);
  };

  const validate = () => {
    const newErrors: typeof errors = {};
    if (!loginRequest.username.trim()) newErrors.username = "Username required";
    if (!loginRequest.password.trim()) newErrors.password = "Password required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async (e?: FormEvent, onSuccess?: () => void) => {
    if (e) e.preventDefault();
    if (!validate()) return;

    try {
      const auth = await api.loginAPI(loginRequest);

      if (!auth.token) {
        setResult({ message: "Login failed,  no token received!" });
        return;
      }

      localStorage.setItem("token", auth.token);

      const user = await api.fetchRoleAPI(auth.token);
      if (!user || !user.expiresAt) {
        setResult({ message: "Failed to retrieve session info!" });
        return;
      }

      if (user?.username) {
        localStorage.setItem("username", user.username);
      }

      const expiryTimestamp = new Date(user.expiresAt).getTime();
      localStorage.setItem("tokenExpiry", expiryTimestamp.toString());
      const remainingSeconds = Math.max((expiryTimestamp - Date.now()) / 1000, 0);
      setRoleInfo(user);
      //      setRemainingTime(remainingSeconds);
      startSessionTimer(remainingSeconds);

      if (onSuccess) {
        onSuccess();
      }
    } catch {
      setResult({ message: "Request failed. Please try again later." });
    }
  };

  const handleLogout = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    const confirmed = window.confirm("Are you sure you want to log out?");
    if (!confirmed) return;

    try {
      await api.logoutAPI(token);
    } catch (error) {
      //  expireSession("Logout failed!");
      console.error(error);
    } finally {
      expireSession("Logged out successfully!");
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
  /*
    useEffect(() => {
      checkSession().then((valid) => {
        if (valid) startSessionTimer(remainingTime);
      });
      return () => clearTimers();
    }, []);*/

  return {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    roleInfo,
    usersList,
    remainingTime,
    handleLogin,
    handleLogout,
    handleCheckRole,
    handleFetchUsers,
    checkSession,
  };
};