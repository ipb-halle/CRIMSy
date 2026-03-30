import { useState, useRef, FormEvent, useEffect } from "react";
import { LoginRequest } from "../api";
import * as api from "../../services/authService";

const SESSION_REFRESH_INTERVAL_MS = 5 * 60 * 1000;
const SESSION_TIMER_INTERVAL_MS = 1000;

export const useAuth = (onAutoLogout?: () => void) => {
  const [loginRequest, setLoginRequest] = useState<LoginRequest>({ username: "", password: "", });
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [usersList, setUsersList] = useState<any>(null);
  const [remainingTime, setRemainingTime] = useState<number>(0);
  const intervalRef = useRef<number | null>(null);
  const countdownRef = useRef<number | null>(null);

  const ACTIVITY_EVENTS = ["click", "mousemove", "keydown", "scroll"];

  let lastActivityPingRef = useRef<number>(0);
  const ACTIVITY_THROTTLE_MS = 1000;

  const sendActivityPing = async () => {
    const now = Date.now();

    if (now - lastActivityPingRef.current < ACTIVITY_THROTTLE_MS) return;
    lastActivityPingRef.current = now;

    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      await api.fetchRoleAPI(token);
      const expiresInSeconds = Number(localStorage.getItem("expiresInSeconds")) || 60;

      setRemainingTime(expiresInSeconds);
      startCountdownTimer();

    } catch {
      expireSession("Session expired. Please log in again.");
    }
  };

  const clearTimers = () => {
    if (countdownRef.current)
      clearInterval(countdownRef.current);
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
    if (!token)
      return false;

    try {
      const user = await api.fetchRoleAPI(token);
      setRoleInfo(user);

      const expiresInSeconds = Number(localStorage.getItem("expiresInSeconds")) || 60;
      setRemainingTime(expiresInSeconds);
      startCountdownTimer();

      if (expiresInSeconds <= 0) {
        expireSession("Session expired! Please log in again.");
        return false;
      }
      return true;
    } catch {
      expireSession("Session expired. Please log in again.");
      return false;
    }
  };

  const startCountdownTimer = () => {
    if (countdownRef.current) clearInterval(countdownRef.current);
    countdownRef.current = window.setInterval(() => {
      setRemainingTime(prev => {
        if (prev <= 1) {
          clearInterval(countdownRef.current!);
          expireSession("Session expired! Please log in again.");
          return 0;
        }
        return prev - 1;
      });
    }, SESSION_TIMER_INTERVAL_MS);
  };

  const handleLogin = async (e?: FormEvent, onSuccess?: () => void) => {
    if (e) e.preventDefault();

    try {
      const auth = await api.loginAPI(loginRequest);

      if (!auth.token) {
        setResult({ message: "Login failed,  no token received!" });
        return;
      }

      localStorage.setItem("token", auth.token);
      localStorage.setItem("expiresInSeconds", auth.expiresInSeconds.toString());

      const user = await api.fetchRoleAPI(auth.token);
      setRoleInfo(user);
      setRemainingTime(auth.expiresInSeconds);
      startCountdownTimer();

      if (!user) {
        setResult({ message: "Failed to retrieve session info!" });
        return;
      }

      if (user?.username) {
        localStorage.setItem("username", user.username);
      }

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
      setRoleInfo(null);
      setResult({ message: "Users List" });
    } catch {
      setUsersList(null);
    }
  };

  useEffect(() => {
    const handleActivity = () => {
      sendActivityPing();
    };

    ACTIVITY_EVENTS.forEach(event =>
      window.addEventListener(event, handleActivity)
    );

    document.addEventListener("visibilitychange", () => {
      if (!document.hidden) {
        sendActivityPing(); // tab switch
      }
    });

    return () => {
      ACTIVITY_EVENTS.forEach(event =>
        window.removeEventListener(event, handleActivity)
      );
    };
  }, []);

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