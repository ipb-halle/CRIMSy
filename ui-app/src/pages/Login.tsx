import React, { useState, useEffect, FormEvent } from "react";

interface LoginProps {
    customLoginInfo?: string;
}

interface LoginResult {
    message: string,
    username?: string,
    token?: string,
}

const Login: React.FC<LoginProps> = ({ customLoginInfo }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
    const [result, setResult] = useState<LoginResult | null>(null);
    const [loading, setLoading] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const storedUsername = localStorage.getItem("username");
        if (token && storedUsername) {
            setResult({
                message: "Already logged in!",
                username: storedUsername,
                token: token,
            })
            setIsLoggedIn(true);
        }
    }, []);

    const validate = (): boolean => {
        const newErrors: typeof errors = {};
        if (!username.trim()) newErrors.username = "Login is required";
        if (!password.trim()) newErrors.password = "Password is required";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        setResult(null);

        const payload = { login: username, password };

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/auth/login",
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                }
            );

            const data: LoginResult = await response.json();

            if (response.ok) {

                setResult(data);
                setIsLoggedIn(true);

                // Save token to localStorage for further API calls
                if (data.token)
                    localStorage.setItem("token", data.token);
                if (data.username)
                    localStorage.setItem("username", data.username);
            } else {
                setResult({ message: data.message || "Login failed!" });
            }

        } catch (err) {
            console.error(err);
            setResult({ message: "Request failed! Please tray agaian later." });
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        const token = localStorage.getItem("token");

        try {
            if (token) {
                await fetch("https://compchem17.ipb-halle.de/ui/rest/auth/logout",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${token}`,
                        }
                    }
                );
            }
        } catch (error) {
            console.error("Logout request failed: ", error);
        } finally {
            localStorage.removeItem("token");
            localStorage.removeItem("username");
            setIsLoggedIn(false);
            setResult(null);
            setUsername("");
            setPassword("");
        }
    }

    /*    const handleLogout = () => {
    
    
            localStorage.removeItem("token");
            localStorage.removeItem("username");
            setIsLoggedIn(false);
            setResult(null);
            setUsername("");
            setPassword("");
        };*/

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
                <legend
                    style={{
                        fontSize: "1.25rem",
                        fontWeight: "bold",
                        marginBottom: "1rem",
                    }}
                >
                    Login
                </legend>

                {customLoginInfo && (
                    <div
                        style={{
                            margin: "0.5rem 0",
                            textAlign: "center",
                            fontWeight: "bold"
                        }}
                    >
                        {customLoginInfo}
                    </div>
                )}

                {/* Login rsult message */}
                {result && (
                    <div
                        style={{
                            textAlign: "center",
                            margin: "1rem 0",
                            color: result.message?.toLowerCase().includes("failed") ? "red" : "green",
                            border: "1px solid #029ACF",
                            borderRadius: "3px",
                            padding: "0.5rem",
                            backgroundColor: "f0f8ff",
                        }}
                    >
                        {result.message}
                        {isLoggedIn && (
                            <div style={{ marginTop: "0.5rem" }}>
                                <button
                                    onClick={handleLogout}
                                    style={{
                                        padding: "0.5rem 1.5rem",
                                        backgroundColor: "#058816ff",
                                        color: "#fff",
                                        border: "none",
                                        marginTop: "1rem",
                                        borderRadius: "3px",
                                        cursor: "1rem",
                                    }}
                                >
                                    Logout
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/*Login form only if not logged in */}
                {!isLoggedIn && (
                    <form id="logInFormId" onSubmit={handleSubmit}>
                        <div style={{ marginBottom: "1rem" }}>
                            <label htmlFor="loginLogin">Username</label>
                            <input
                                type="text"
                                id="loginLogin"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                            />
                            {errors.username && (
                                <div style={{ color: "red", fontSize: "0.75rem" }}>{errors.username}</div>
                            )}
                        </div>

                        <div style={{ marginBottom: "1rem" }}>
                            <label htmlFor="loginPassword">Password</label>
                            <input
                                type="password"
                                id="loginPassword"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                            />
                            {errors.password && (
                                <div style={{ color: "red", fontSize: "0.75rem" }}>{errors.password}</div>
                            )}
                        </div>

                        <div style={{ textAlign: "center", marginBottom: "1rem" }}>
                            <button
                                type="submit"
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    backgroundColor: "#029ACF",
                                    color: "#fff",
                                    border: "none",
                                    borderRadius: "3px",
                                    cursor: "pointer",
                                }}
                                disabled={loading}
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