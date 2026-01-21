import { error } from "console";
import React, { useState, useEffect, useRef, FormEvent } from "react";

interface LoginProps {
    customLoginInfo?: string;
}
/*
interface LoginResult {
    message: string;
    username?: string;
    token?: string;
    expiresInSeconds?: number;
}*/


interface LoginResult {
    message: string;
    username?: string;
    token?: string;
    expiresInSeconds?: number;
    roles?: string[];
    groups?: string[];
}


const SESSION_FALLBACK_TIMEOUT_MS = 60 * 1000;

const Login: React.FC<LoginProps> = ({ customLoginInfo }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [result, setResult] = useState<LoginResult | null>(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [roleInfo, setRoleInfo] = useState<{ username: string; token: string; message: string } | null>(null);
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

    /* ------------------ Restore session on reload ------------------ */
    useEffect(() => {

        const checkSessions = async () => {
            const token = localStorage.getItem("token");
            const storedUsername = localStorage.getItem("username");

            if (!token || !storedUsername) return;

            try {
                //console.log("token: ", token);
                const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/sessions", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!response.ok) {
                    expireSession("Session expired. Please log in again");
                    return;
                }

                setIsLoggedIn(true);
                setResult({ message: `Welcome back, ${storedUsername}!`, username: storedUsername, token });
                startSessionTimer();
                setRoleInfo(null); // role info hidden until user clicks button
            } catch (err) {
                console.error(err);
                expireSession("Session expired. Please log in again");
            }
        };

        checkSessions();

        return () => clearLogoutTimer();
    }, []);

    /*
        useEffect(() => {
            const token = localStorage.getItem("token");
            const storedUsername = localStorage.getItem("username");
    
            if (token && storedUsername) {
                setIsLoggedIn(true);
                setResult({ message: `Welcome back, ${storedUsername}!`, username: storedUsername, token });
                startSessionTimer();
            }
            return () => clearLogoutTimer();
        }, []);
        
    */


    /* ------------------ Validation ------------------ */
    const validate = (): boolean => {
        const newErrors: typeof errors = {};
        if (!username.trim()) newErrors.username = "Username required";
        if (!password.trim()) newErrors.password = "Password required";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    /* ------------------ Login ------------------ */
    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setResult(null);

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ login: username, password }),
            });

            let data: LoginResult;
            try {
                data = await response.json();
            } catch {
                setResult({ message: "Server returned invalid response" });
                return;
            }

            if (!response.ok || !data.token || !data.username) {
                return setResult({ message: data.message || "Login failed" });
            }

            localStorage.setItem("token", data.token);
            localStorage.setItem("username", data.username);

            setIsLoggedIn(true);
            setResult(data);
            setRoleInfo(null);
            startSessionTimer(data.expiresInSeconds);

        } catch (err) {
            console.error(err);
            setResult({ message: "Request failed. Please try again later." });
        }

    };

    /* ------------------ Logout ------------------ */
    const handleLogout = async () => {
        const token = localStorage.getItem("token");
        try {
            if (token) {
                await fetch("https://compchem17.ipb-halle.de/ui/rest/auth/logout", {
                    method: "POST",
                    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
                });
            }
        } catch (err) {
            console.error(err);
        } finally {
            expireSession("Logged out successfully.");
        }
    };

    /* ------------------ Check Role ------------------ */
    const handleCheckRole = async () => {
        const token = localStorage.getItem("token");
        if (!token) return setRoleInfo(null);

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/role", {
                headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
            });

            if (!response.ok) return setRoleInfo(null);

            const data = await response.json();
            setRoleInfo({ username: data.username, token: data.token, message: data.message });
        } catch (err) {
            console.error(err);
            setRoleInfo(null);
        }
    };

    /* ------------------ Render ------------------ */
    const isError =
        result?.message.toLowerCase().includes("failed") ||
        result?.message.toLowerCase().includes("expired") ||
        result?.message.toLowerCase().includes("unauthorized");

    return (
        <div style={{ border: "1px solid #029ACF", borderRadius: "3px", padding: "1rem", maxWidth: "400px", margin: "2rem auto" }}>
            <fieldset style={{ border: "none" }}>
                <legend style={{ fontSize: "1.25rem", fontWeight: "bold" }}>Login</legend>
                {customLoginInfo && <div style={{ textAlign: "center", fontWeight: "bold" }}>{customLoginInfo}</div>}

                {result && (
                    <div style={{ textAlign: "center", margin: "1rem 0", color: isError ? "red" : "green", border: "1px solid #029ACF", borderRadius: "3px", padding: "0.5rem" }}>
                        {result.message}
                    </div>
                )}

                {isLoggedIn && (
                    <div style={{ textAlign: "center", marginTop: "1rem" }}>
                        <button onClick={handleCheckRole} style={{ padding: "0.5rem 1.5rem", marginRight: "0.5rem" }}>Users List</button>
                        <button onClick={handleLogout} style={{ padding: "0.5rem 1.5rem" }}>Logout</button>

                        {roleInfo && (
                            <div style={{ marginTop: "1rem", padding: "0.75rem", border: "1px solid #029ACF", borderRadius: "3px", textAlign: "left", fontWeight: "bold", wordBreak: "break-all" }}>
                                <div>User: {roleInfo.username}</div>
                                <div>Token: {roleInfo.token}</div>
                                <div style={{ marginTop: "0.5rem" }}>{roleInfo.message}</div>
                            </div>
                        )}
                    </div>
                )}

                {!isLoggedIn && (
                    <form onSubmit={handleSubmit}>
                        <div style={{ marginBottom: "1rem" }}>
                            <label>Username</label>
                            <input value={username} onChange={(e) => setUsername(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} />
                            {errors.username && <div style={{ color: "red" }}>{errors.username}</div>}
                        </div>

                        <div style={{ marginBottom: "1rem" }}>
                            <label>Password</label>
                            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} />
                            {errors.password && <div style={{ color: "red" }}>{errors.password}</div>}
                        </div>

                        <div style={{ textAlign: "center" }}>
                            <button type="submit" style={{ padding: "0.5rem 1.5rem" }}>Login</button>
                        </div>
                    </form>
                )}
            </fieldset>
        </div>
    );
};

export default Login;
