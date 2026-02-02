import React, { FormEvent } from "react";

interface Props {
  username: string;
  password: string;
  setUsername: (v: string) => void;
  setPassword: (v: string) => void;
  errors: { username?: string; password?: string };
  result: { message: string } | null;
  onSubmit: (e?: FormEvent) => void;
}

const LoginForm: React.FC<Props> = ({
  username,
  password,
  setUsername,
  setPassword,
  errors,
  onSubmit,
}) => {
  return (
    <form onSubmit={onSubmit}>
      <div style={{ marginBottom: "0.75rem" }}>
        <input
          style={{ width: "100%", padding: "0.5rem" }}
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        {errors.username && <div style={{ color: "red" }}>{errors.username}</div>}
      </div>

      <div style={{ marginBottom: "0.75rem" }}>
        <input
          type="password"
          style={{ width: "100%", padding: "0.5rem" }}
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {errors.password && <div style={{ color: "red" }}>{errors.password}</div>}
      </div>

      <button type="submit"
        style={{
          width: "100%",
          padding: "0.5rem",
          background: "#029ACF",
          color: "white",
          border: "none",
          borderRadius: "3px",
          fontWeight: "bold",
          cursor: "pointer",
        }}>
        Login
      </button>
    </form>
  );
};

export default LoginForm;
