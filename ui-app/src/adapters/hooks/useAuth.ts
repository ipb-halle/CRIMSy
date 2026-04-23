import { useState, useRef, FormEvent, useEffect } from "react";
import { LoginRequest } from "../api";
import * as api from "../../services/authService";
import { findAncestor } from "typescript";

const SESSION_TIMER_INTERVAL_MS = 1000;

export const useAuth = (onAutoLogout?: () => void) => {
  const [loginRequest, setLoginRequest] = useState<LoginRequest>({ username: "", password: "", });
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
  const [roleInfo, setRoleInfo] = useState<any>(null);
  const [totalUsersCount, setTotalUsersCount] = useState<number>(0);
  const [usersList, setUsersList] = useState<any>(null);
  const [remainingTime, setRemainingTime] = useState<number>(0);

  const countdownRef = useRef<number | null>(null);

  let lastActivityPingRef = useRef<number>(0);
  const ACTIVITY_THROTTLE_MS = 1000;

  const sessionLockRef = useRef(false);


  const ACTIVITY_EVENTS = ["keydown", "mousedown", "touchstart", "scroll", "pointerdown"] as const;

  // -------- Timer Helper --------//
  const clearTimers = () => {
    if (countdownRef.current) {
      clearInterval(countdownRef.current);
      countdownRef.current = null;
    }
  };

  const startCountdownTimer = () => {
    if (countdownRef.current) clearInterval(countdownRef.current);

    countdownRef.current = window.setInterval(() => {
      setRemainingTime(prev => {
        if (prev <= 1) {
          clearTimers();
          expireSession("Session expired! Please log in again.");
          return 0;
        }
        return prev - 1;
      });
    }, SESSION_TIMER_INTERVAL_MS);
  };

  // -------- Session Core --------//
  const expireSession = (message: string) => {
    clearTimers();

    setResult({ message });
    setRoleInfo(null);
    setUsersList(null);
    setRemainingTime(0);
    setLoginRequest({ username: "", password: "", });

    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("userId");
    localStorage.removeItem("tokenExpiry");
    localStorage.removeItem("expiresInSeconds");

    onAutoLogout?.();
  };

  // -------- Activity Ping --------//
  const sendActivityPing = async () => {
    if (sessionLockRef.current) return;

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

  // -------- Session Check -------- //
  const checkSession = async (): Promise<boolean> => {
    const token = localStorage.getItem("token");
    if (!token)
      return false;

    try {
      const userRole = await api.fetchRoleAPI(token);
      setRoleInfo(userRole);

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

  // -------- Login -------- //
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

      const userInfo = await api.fetchRoleAPI(auth.token);
      setRoleInfo(userInfo);

      setRemainingTime(auth.expiresInSeconds);
      startCountdownTimer();

      if (userInfo?.username) {
        localStorage.setItem("username", userInfo.username);
        localStorage.setItem("userId", userInfo.id.toString());
      }

      onSuccess?.();
    } catch {
      setResult({ message: "Request failed. Please try again later." });
    }
  };

  // -------- Logout -------- //
  const handleLogout = async (): Promise<boolean> => {
    const token = localStorage.getItem("token");
    if (!token) {
      alert("No auth token found");
      return false;
    }

    clearTimers();
    const confirmed = window.confirm('Are you sure you want to log out?');
    if (!confirmed) {
      console.log("[LOGOUT] cancelled by user!");
      startCountdownTimer();
      return false;
    }

    sessionLockRef.current = true;
    try {
      await api.logoutAPI(token);
      return true;
    } catch (error) {
      console.error("[LOGOUT API ERROR", error);
      return true;
    } finally {
      expireSession("Logged out successfully!");
      sessionLockRef.current = false;
    }
  };

  // -------- Role /  Users -------- //
  const handleCheckRole = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;
    try {
      const userInfo = await api.fetchRoleAPI(token);
      setRoleInfo(userInfo);
      setUsersList(null);
      setResult({ message: "Your Role" });
    } catch {
      setRoleInfo(null);
    }
  };

  const handleFetchUsers = async (page: number = 1, pageSize: number = 5) => {
    setUsersList(null);

    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      const paginatedUserResponse = await api.fetchUsersAPI(token, page, pageSize);
      setUsersList(paginatedUserResponse);
      setTotalUsersCount(paginatedUserResponse.totalItems);
      setResult({ message: "Users List" });
    } catch {
      setUsersList(null);
      setResult({ message: "Users List is Empty!" });
    }
  };

  // -------- Activity Listener -------- //
  useEffect(() => {
    const handleActivity = (e: Event) => {
      if (sessionLockRef.current) return;

      if (e.type === "mousedown") {
        const target = e.target as HTMLElement | null;
        if (target?.closest("button")) return;
        return;
      }
      sendActivityPing();
    };

    ACTIVITY_EVENTS.forEach(event =>
      window.addEventListener(event, handleActivity, { passive: true })
    );

    const onVisibilityChange = () => {
      if (!document.hidden) sendActivityPing();
    };
    const onFocuse = () => sendActivityPing();


    document.addEventListener("visibilitychange", onVisibilityChange);
    window.addEventListener("focus", onFocuse);

    return () => {
      ACTIVITY_EVENTS.forEach(event =>
        window.removeEventListener(event, handleActivity)
      );
      document.removeEventListener("visibilitychange", onVisibilityChange);
      window.removeEventListener("focus", onFocuse);
      clearTimers();
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
    totalUsersCount,
    handleLogout,
    handleCheckRole,
    handleFetchUsers,
    checkSession,
  };
};