import React, { useState, useEffect, useRef, FormEvent } from "react";

interface LoginProps {
    customLoginInfo?: string;
}

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
    /* ------------------ State ------------------ */
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [result, setResult] = useState<LoginResult | null>(null);
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
                setUsersList(null); // users list hidden until user clicks button
            } catch (err) {
                console.error(err);
                expireSession("Session expired. Please log in again");
            }
        };

        checkSessions();

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
                setResult({ message: data.message || "Login failed" });
                return;
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

    /* ------------------ Role Info ------------------ */
    const handleCheckRole = async () => {
        setUsersList(null);
        const token = localStorage.getItem("token");
        if (!token) return setRoleInfo(null);

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/role", {
                headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
            });

            if (!response.ok) {
                console.error("Failed to fetch role info: ", response.status);
                return setRoleInfo(null);
            }

            const data = await response.json();
            setRoleInfo({
                username: data.username,
                groups: Array.isArray(data.groups) ? data.groups.join(", ") : data.groups,
                admin: data.admin.toString(),
            });
        } catch (err) {
            console.error(err);
            setRoleInfo(null);
        }
    };

    const handleFetchUsers = async () => {
        setRoleInfo(null)
        const token = localStorage.getItem("token");
        if (!token) return;

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/usersList", {
                headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
            });

            if (!response.ok) {
                console.error("Failed to fetch users:", response.status);
                return;
            }

            const data = await response.json();
            setUsersList(data); // store in state
        } catch (err) {
            console.error(err);
            setUsersList(null);
        }
    };

    /* ------------------ Rendering ------------------ */
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

                {isLoggedIn ? (
                    <div style={{ textAlign: "center", marginTop: "1rem" }}>
                        <button onClick={handleCheckRole} style={{ padding: "0.5rem 1.5rem", marginRight: "0.5rem" }}>
                            Check My Role
                        </button>
                        <button onClick={handleFetchUsers} style={{ padding: "0.5rem 1.5rem" }}>
                            View Users
                        </button>

                        <button onClick={handleLogout} style={{ padding: "0.5rem 1.5rem" }}>
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
                                    wordBreak: "break-all"
                                }}
                            >
                                <div>Username: {roleInfo.username}</div>
                                <div>Groups: {roleInfo.groups}</div>
                                <div>Admin Access: {roleInfo.admin}</div>
                            </div>
                        )}
                        {usersList && (
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
                                <div>Users:</div>
                                {usersList.map((u, idx) => (
                                    <div key={idx} style={{ marginLeft: "1rem", marginTop: "0.5rem" }}>
                                        <div>ID: {u.id}</div>
                                        <div>Login: {u.login}</div>
                                        <div>Name: {u.name}</div>
                                        <div>Type: {u.membertype}</div>
                                        {u.info && <div style={{ color: "red" }}>{u.info}</div>}
                                    </div>
                                ))}
                            </div>
                        )}


                    </div>
                ) : (
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
