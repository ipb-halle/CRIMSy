import React, { useState, FormEvent } from "react";

const LoginForm: React.FC = () => {
    const [login, setLogin] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [result, setResult] = useState<string>("");

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

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
                setResult(text || "login successful!");
            } else {
                const errorText = await response.text();
                setResult(`Login failed: ${errorText}`);
            }

        } catch (err) {
            setResult("Request failed!");
            console.error(err);
        }
    };

    return (
        <>
            <form onSubmit={handleSubmit}>
                <h2>Login Test API</h2>
                <input
                    type="text"
                    placeholder="Login"
                    value={login}
                    onChange={(e) => setLogin(e.target.value)}
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit">Login</button>
                <pre>{result}</pre> {/* Display the result here */}
            </form>
        </>
    );
};

export default LoginForm;