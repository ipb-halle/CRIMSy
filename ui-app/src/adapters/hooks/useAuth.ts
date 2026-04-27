import { useState, useRef, FormEvent, useEffect, useCallback } from "react";
import { LoginRequest, AuthUser, PaginatedUserResponse } from "../api";
import { authApi, usersApi } from "../apiClient";
import { authSession } from "./authSession";

const SESSION_TIMER_INTERVAL_MS = 1000;

export const useAuth = (onAutoLogout?: () => void) => {
  const [loginRequest, setLoginRequest] = useState<LoginRequest>({ username: "", password: "", });

  const [errors] = useState<{ username?: string; password?: string }>({});
  const [result, setResult] = useState<{ message: string } | null>(null);
  const [roleInfo, setRoleInfo] = useState<AuthUser | null>(null);
  const [usersList, setUsersList] = useState<PaginatedUserResponse | null>(null);
  const [totalUsersCount, setTotalUsersCount] = useState(0);
  const [remainingTime, setRemainingTime] = useState(0);

  const timerRef = useRef<number | null>(null);
  const lastActivityPingRef = useRef(0);
  const lockRef = useRef(false);

  const ACTIVITY_THROTTLE_MS = 1000;
  const ACTIVITY_EVENTS = ["keydown", "mousedown", "touchstart", "scroll", "pointerdown"] as const;

  // -------- Timer Helper --------//
  const clearTimer = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  const startTimer = useCallback(() => {
    clearTimer();

    timerRef.current = window.setInterval(() => {
      setRemainingTime(prev => {
        if (prev <= 1) {
          clearTimer();
          expireSession("Session expired! Please log in again.");
          return 0;
        }
        return prev - 1;
      });
    }, SESSION_TIMER_INTERVAL_MS);
  }, [clearTimer]);

  // -------- Session Core --------//
  const expireSession = useCallback(
    (message: string) => {
      clearTimer();

      setResult({ message });
      setRoleInfo(null);
      setUsersList(null);
      setRemainingTime(0);
      setLoginRequest({ username: "", password: "", });

      authSession.clear();
      onAutoLogout?.();
    },
    [clearTimer, onAutoLogout]
  );

  // -------- Activity Ping --------//
  const sendActivityPing = useCallback(async () => {
    if (lockRef.current) return;

    const now = Date.now();
    if (now - lastActivityPingRef.current < ACTIVITY_THROTTLE_MS) return;
    lastActivityPingRef.current = now;

    const token = authSession.getToken();
    if (!token) return;

    try {
      await authApi().getCurrentUser();

      const expiresInSeconds = authSession.getExpires();

      setRemainingTime(expiresInSeconds);
      startTimer();
    } catch {
      expireSession("Session expired. Please log in again.");
    }
  }, [startTimer, expireSession]);

  // -------- Session Check -------- //
  const checkSession = useCallback(async (): Promise<boolean> => {
    const token = authSession.getToken();
    if (!token)
      return false;

    try {
      const userRole = await authApi().getCurrentUser();
      setRoleInfo(userRole);

      const expiresInSeconds = authSession.getExpires();
      setRemainingTime(expiresInSeconds);
      startTimer();

      if (expiresInSeconds <= 0) {
        expireSession("Session expired! Please log in again.");
        return false;
      }
      return true;
    } catch {
      expireSession("Session expired. Please log in again.");
      return false;
    }
  }, [expireSession, startTimer]);

  // -------- Login -------- //
  const handleLogin = useCallback(
    async (e?: FormEvent, onSuccess?: () => void) => {
      if (e) e.preventDefault();

      try {
        const auth = await authApi().login({ loginRequest });

        if (!auth.token) {
          setResult({ message: "Login failed,  no token received!" });
          return;
        }

        authSession.setSession(auth.token, auth.expiresInSeconds);
        const userInfo = await authApi().getCurrentUser();
        setRoleInfo(userInfo);

        setRemainingTime(auth.expiresInSeconds);
        startTimer();

        if (userInfo?.username) {
          authSession.setUser(userInfo.username, userInfo.id);
        }
        setResult({ message: "Login successful" });
        onSuccess?.();
      } catch {
        setResult({ message: "Request failed. Please try again later." });
      }
    },
    [loginRequest, startTimer]
  );

  // -------- Logout -------- //
  const handleLogout = useCallback(async (): Promise<boolean> => {
    const token = authSession.getToken();
    if (!token) {
      alert("No auth token found");
      return false;
    }

    clearTimer();

    const confirmed = window.confirm('Are you sure you want to log out?');
    if (!confirmed) {
      console.log("[LOGOUT] cancelled by user!");
      startTimer();
      return false;
    }

    lockRef.current = true;

    try {
      await authApi().logout();
      return true;
    } catch (error) {
      console.error("[LOGOUT API ERROR", error);
      return true;
    } finally {
      expireSession("Logged out successfully!");
      lockRef.current = false;
    }
  }, [clearTimer, startTimer, expireSession]);

  const handleFetchUsers = useCallback(async (page: number = 1, pageSize: number = 5) => {
    setUsersList(null);

    const token = authSession.getToken();
    if (!token) return;

    try {
      const paginatedUserResponse = await usersApi().getUsersList({ page, pageSize });
      setUsersList(paginatedUserResponse);
      setTotalUsersCount(paginatedUserResponse.totalItems);
      setResult({ message: "Users List" });
    } catch {
      setUsersList(null);
      setResult({ message: "Users List is Empty!" });
    }
  }, []);

  // -------- Activity Listener -------- //
  useEffect(() => {
    const handleActivity = (e: Event) => {
      if (lockRef.current) return;

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
      clearTimer();
    };
  }, [sendActivityPing, clearTimer]);

  return {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    roleInfo,
    usersList,
    totalUsersCount,
    remainingTime,
    handleLogin,
    handleLogout,
    handleFetchUsers,
    checkSession,
  };
};