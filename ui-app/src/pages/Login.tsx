import React, { useState, useEffect, useRef, FormEvent } from "react";

interface LoginProps {
    customLoginInfo?: string;
}

interface LoginResult {
    message: string;
    username?: string;
    token?: string;
    expiresInSeconds?: number;
}

const SESSION_FALLBACK_TIMEOUT_MS = 60 * 1000; // safety fallback

const Login: React.FC<LoginProps> = ({ customLoginInfo }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [result, setResult] = useState<LoginResult | null>(null);
    const [loading, setLoading] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const logoutTimerRef = useRef<number | null>(null);
    const [roleInfo, setRoleInfo] = useState<{
        username: string;
        token: string;
        message: string;
    } | null>(null);


    /* -------------------- helpers -------------------- */

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
        setUsername("");
        setPassword("");
        setRoleInfo(null);
    };

    const startSessionTimer = (expiresInSeconds?: number) => {
        clearLogoutTimer();

        const timeoutMs = expiresInSeconds
            ? expiresInSeconds * 1000
            : SESSION_FALLBACK_TIMEOUT_MS;

        logoutTimerRef.current = window.setTimeout(() => {
            expireSession("Session expired due to inactivity. Please log in again.");
        }, timeoutMs);
    };

    /* -------------------- session restore -------------------- */

    useEffect(() => {
        const token = localStorage.getItem("token");
        const storedUsername = localStorage.getItem("username");

        if (token && storedUsername) {
            setIsLoggedIn(true);
            setResult({
                message: "Session restored",
                username: storedUsername,
                token,
            });

            // enforce max session length even after reload
            startSessionTimer();
        }

        return () => clearLogoutTimer();
    }, []);

    /* -------------------- validation -------------------- */

    const validate = (): boolean => {
        const newErrors: typeof errors = {};
        if (!username.trim()) newErrors.username = "Login is required";
        if (!password.trim()) newErrors.password = "Password is required";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    /* -------------------- login -------------------- */

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        setResult(null);

        try {
            const response = await fetch(
                "https://compchem17.ipb-halle.de/ui/rest/auth/login",
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ login: username, password }),
                }
            );

            const data: LoginResult = await response.json();

            if (!response.ok) {
                setResult({ message: data.message || "Login failed" });
                return;
            }

            if (!data.token || !data.username) {
                setResult({ message: "Invalid login response" });
                return;
            }

            localStorage.setItem("token", data.token);
            localStorage.setItem("username", data.username);

            setIsLoggedIn(true);
            setResult(data);

            startSessionTimer(data.expiresInSeconds);
        } catch (err) {
            console.error(err);
            setResult({ message: "Request failed. Please try again later." });
        } finally {
            setLoading(false);
        }
    };

    /* -------------------- logout -------------------- */

    const handleLogout = async () => {
        const token = localStorage.getItem("token");

        try {
            if (token) {
                await fetch(
                    "https://compchem17.ipb-halle.de/ui/rest/auth/logout",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
            }
        } catch (error) {
            console.error("Logout request failed:", error);
        } finally {
            expireSession("Logged out successfully");
        }
    };


    const handleCheckRole = async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            setRoleInfo(null);
            return;
        }

        try {
            const response = await fetch(
                "https://compchem17.ipb-halle.de/ui/rest/role",
                {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            setRoleInfo({
                username: data.username,
                token: data.token,
                message: data.message,
            });
        } catch (err) {
            console.error(err);
            setRoleInfo(null);
        }
    };


    /* -------------------- rendering -------------------- */

    const isError =
        result?.message.toLowerCase().includes("failed") ||
        result?.message.toLowerCase().includes("expired") ||
        result?.message.toLowerCase().includes("unauthorized");

    return (
        <div
            className="centralPanel"
            style={{
                border: "1px solid #029ACF",
                borderRadius: "3px",
                padding: "1rem",
                maxWidth: "400px",
                margin: "2rem auto",
            }}
        >
            <fieldset style={{ border: "none" }}>
                <legend style={{ fontSize: "1.25rem", fontWeight: "bold" }}>
                    Login
                </legend>

                {customLoginInfo && (
                    <div style={{ textAlign: "center", fontWeight: "bold" }}>
                        {customLoginInfo}
                    </div>
                )}

                {result && (
                    <div
                        style={{
                            textAlign: "center",
                            margin: "1rem 0",
                            color: isError ? "red" : "green",
                            border: "1px solid #029ACF",
                            borderRadius: "3px",
                            padding: "0.5rem",
                        }}
                    >
                        {result.message}

                        {isLoggedIn && (
                            <div style={{ marginTop: "1rem" }}>
                                <button
                                    onClick={handleCheckRole}
                                    style={{
                                        padding: "0.5rem 1.5rem",
                                        backgroundColor: "#029ACF",
                                        color: "#fff",
                                        border: "none",
                                        borderRadius: "3px",
                                        cursor: "pointer",
                                        marginRight: "0.5rem",
                                    }}
                                >
                                    Check My Role
                                </button>

                                <button
                                    onClick={handleLogout}
                                    style={{
                                        padding: "0.5rem 1.5rem",
                                        backgroundColor: "#058816",
                                        color: "#fff",
                                        border: "none",
                                        borderRadius: "3px",
                                        cursor: "pointer",
                                    }}
                                >
                                    Logout
                                </button>

                                {roleInfo && (
                                    <div
                                        style={{
                                            marginTop: "1rem",
                                            padding: "0.75rem",
                                            border: "1px solid #029ACF",
                                            borderRadius: "3px",
                                            textAlign: "left",
                                            fontWeight: "bold",
                                            wordBreak: "break-all",
                                        }}
                                    >
                                        <div>User: {roleInfo.username}</div>
                                        <div>Token: {roleInfo.token}</div>
                                        <div style={{ marginTop: "0.5rem" }}>{roleInfo.message}</div>
                                    </div>
                                )}

                            </div>
                        )}


                    </div>
                )}

                {!isLoggedIn && (
                    <form onSubmit={handleSubmit}>
                        <div style={{ marginBottom: "1rem" }}>
                            <label>Username</label>
                            <input
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                style={{ width: "100%", padding: "0.5rem" }}
                            />
                            {errors.username && (
                                <div style={{ color: "red" }}>{errors.username}</div>
                            )}
                        </div>

                        <div style={{ marginBottom: "1rem" }}>
                            <label>Password</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                style={{ width: "100%", padding: "0.5rem" }}
                            />
                            {errors.password && (
                                <div style={{ color: "red" }}>{errors.password}</div>
                            )}
                        </div>

                        <div style={{ textAlign: "center" }}>
                            <button
                                type="submit"
                                disabled={loading}
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    backgroundColor: "#029ACF",
                                    color: "#fff",
                                    border: "none",
                                    borderRadius: "3px",
                                    cursor: "pointer",
                                }}
                            >
                                {loading ? "Logging in..." : "Login"}
                            </button>
                        </div>
                    </form>
                )}
            </fieldset>
        </div>
    );
};

export default Login;
