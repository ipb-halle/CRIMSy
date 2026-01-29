// src/hooks/useAuth.ts
import { useState, useEffect, useRef, FormEvent } from "react";
import * as api from "../services/authService";

const SESSION_FALLBACK_TIMEOUT_MS = 60 * 1000;

export const useAuth = () => {
    /* ------------------ State ------------------ */
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [result, setResult] = useState<api.LoginResult | null>(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [roleInfo, setRoleInfo] = useState<{ username: string; groups: string; admin: string } | null>(null);
    const [usersList, setUsersList] = useState<any[] | null>(null);

    const logoutTimerRef = useRef<number | null>(null);

    /* ------------------ Session Management ------------------ */
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

    /* ------------------ Restore session on mount ------------------ */
    useEffect(() => {
        const checkSession = async () => {
            const token = localStorage.getItem("token");
            const storedUsername = localStorage.getItem("username");

            if (!token || !storedUsername) return;

            try {
                await api.checkSessionAPI(token);
                setIsLoggedIn(true);
                setResult({ message: `Welcome back, ${storedUsername}!` });
                startSessionTimer();
                setRoleInfo(null);
                setUsersList(null);
            } catch {
                expireSession("Session expired. Please log in again.");
            }
        };

        checkSession();
        return () => clearLogoutTimer();
    }, []);

    /* ------------------ Validation ------------------ */
    const validate = () => {
        const newErrors: typeof errors = {};
        if (!username.trim()) newErrors.username = "Username required";
        if (!password.trim()) newErrors.password = "Password required";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    /* ------------------ Login ------------------ */
    const handleLogin = async (e?: FormEvent) => {
        if (e) e.preventDefault();
        if (!validate()) return;

        setResult(null); // reset previous message

        try {
            const data = await api.loginAPI(username, password);

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
        } catch (err) {
            console.error(err);
            setResult({ message: "Request failed. Please try again later." });
        }
    };

    /* ------------------ Logout ------------------ */
    const handleLogout = async () => {
        const token = localStorage.getItem("token");

        if (token) {
            const data = await api.logoutAPI(token);
            try {
                expireSession(data.message);

            } catch (err) {
                console.error(err);
            }
        }
    };

    /* ------------------ Role Info ------------------ */
    const handleCheckRole = async () => {
        setUsersList(null);
        const token = localStorage.getItem("token");
        if (!token) return setRoleInfo(null);

        try {
            const data = await api.fetchRoleAPI(token);
            setRoleInfo({
                username: data.username,
                groups: Array.isArray(data.groups) ? data.groups.join(", ") : data.groups,
                admin: data.admin.toString(),
            });
            setResult({ message: "Your Role" });
        } catch {
            setRoleInfo(null);
        }
    };

    /* ------------------ Users List ------------------ */
    const handleFetchUsers = async () => {
        setRoleInfo(null);
        const token = localStorage.getItem("token");
        if (!token) return;

        try {
            const data = await api.fetchUsersAPI(token);
            setUsersList(data);
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