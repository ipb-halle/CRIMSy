import React, { FormEvent } from "react";
import { LoginResult } from "../services/authService";

interface Props {
  username: string;
  password: string;
  setUsername: (v: string) => void;
  setPassword: (v: string) => void;
  errors: { username?: string; password?: string };
  onSubmit: (e?: FormEvent) => void;
  result: LoginResult | null;
}

const LoginForm: React.FC<Props> = ({ username, password, setUsername, setPassword, errors, onSubmit, result }) => {
  const isError =
    result?.message.toLowerCase().includes("failed") ||
    result?.message.toLowerCase().includes("expired") ||
    result?.message.toLowerCase().includes("unauthorized");

  return (
    <form onSubmit={onSubmit}>

      <div style={{ marginBottom: "1rem" }}>
        <label>Username</label>
        <input value={username} onChange={e => setUsername(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} />
        {errors.username && <div style={{ color: "red" }}>{errors.username}</div>}
      </div>

      <div style={{ marginBottom: "1rem" }}>
        <label>Password</label>
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} />
        {errors.password && <div style={{ color: "red" }}>{errors.password}</div>}
      </div>

      <div style={{ textAlign: "center" }}>
        <button type="submit" style={{ padding: "0.5rem 1.5rem" }}>Login</button>
      </div>
    </form>
  );
};

export default LoginForm;
