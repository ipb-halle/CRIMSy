import React, { useState } from "react";

const TestApi: React.FC = () => {
    const [login, setLogin] = useState("");
    const [password, setPassword] = useState("");
    const [result, setResult] = useState("");

    const handleSubmit = async () => {
        const payload = { login, password };

        try {
            const response = await fetch("https://compchem17.ipb-halle.de/ui/rest/loginTest", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                },
                body: JSON.stringify(payload)
            });
            const data = await response.json();
            setResult(JSON.stringify(data, null, 2));
        } catch (err) {
            setResult('Error: ${err}');
        }
    };

    return (
        <div>
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
            <button onClick={handleSubmit}>Login</button>
            <pre>{result}</pre>
        </div>
    );
};

export default TestApi;