import React, { useState, FormEvent } from "react";

interface LoginProps {
    customLoginInfo?: string;
}

const Login: React.FC<LoginProps> = ({ customLoginInfo }) => {
    const [login, setLogin] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState<{ login?: string; password?: string }>({});
    const [result, setResult] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const validate = (): boolean => {
        const newErrors: typeof errors = {};
        if (!login.trim()) newErrors.login = "Login is required";
        if (!password.trim()) newErrors.password = "Password is required";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        setResult(null);

        const payload = { login, password };

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const text = await response.text();
                setResult(text || "Login successful!");
            } else {
                const errorText = await response.text();
                setResult(`Login failed: ${errorText}! Please tray agaian later.`);
            }

        } catch (err) {
            console.error(err);
            setResult("Request failed! Please tray agaian later.");
        } finally {
            setLoading(false);
        }
    };

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
            {result ? (
                <div style={{ textAlign: "center", margin: "1rem 0", color: result.includes("failed") ? "red" : "green" }}>
                    {result}
                </div>
            ) : (
                <form id="logInFormId" onSubmit={handleSubmit}>
                    <fieldset style={{ border: "none" }}>
                        <legend style={{ fontSize: "1.25rem", fontWeight: "bold", marginBottom: "1rem" }}>
                            Login
                        </legend>

                        {customLoginInfo && (
                            <div style={{ margin: "0.5rem 0", textAlign: "center", fontWeight: "bold" }}>
                                {customLoginInfo}
                            </div>
                        )}

                        <div style={{ marginBottom: "1rem" }}>
                            <label htmlFor="loginLogin">Login name</label>
                            <input
                                type="text"
                                id="loginLogin"
                                value={login}
                                onChange={(e) => setLogin(e.target.value)}
                                style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                            />
                            {errors.login && <div style={{ color: "red", fontSize: "0.75rem" }}>{errors.login}</div>}
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
                            {errors.password && <div style={{ color: "red", fontSize: "0.75rem" }}>{errors.password}</div>}
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


                        <div style={{ fontSize: "0.75rem", color: "gray", textAlign: "center" }}>
                            <p>
                                By registering, you agree to the extended processing of your data in accordance with our{" "}
                                <a
                                    href="https://compchem17.ipb-halle.de/ui/dsgvo.xhtml;jsessionid=409B9E9E3286CB1F131EAC6DBBA4188E"
                                    target="_blank"
                                    style={{ textDecoration: "underline" }}
                                >
                                    Data Protection Policy!
                                </a>
                            </p>
                        </div>
                    </fieldset>
                </form>
            )}
        </div >
    );
};

export default Login;