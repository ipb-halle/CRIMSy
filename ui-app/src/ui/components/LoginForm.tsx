import React, { FormEvent } from "react";
import { LoginRequest } from "../../adapters/api";

interface Props {
  loginrequest: LoginRequest;
  setLoginRequest: (v: LoginRequest) => void;
  errors: { username?: string; password?: string };
  result: string;
  onSubmit: (e?: FormEvent) => void;
}

const LoginForm: React.FC<Props> = ({
  loginrequest,
  setLoginRequest,
  errors,
  onSubmit,
}) => {
  return (
    <form onSubmit={onSubmit}>
      <div style={{ marginBottom: "0.75rem" }}>
        <input
          style={{ width: "100%", padding: "0.5rem" }}
          placeholder="Username"
          value={loginrequest.login}
          onChange={(e) => setLoginRequest({ ...loginrequest, login: e.target.value })
          }
        />
        {errors.username && <div style={{ color: "red" }}>{errors.username}</div>}
      </div>

      <div style={{ marginBottom: "0.75rem" }}>
        <input
          type="password"
          style={{ width: "100%", padding: "0.5rem" }}
          placeholder="Password"
          value={loginrequest.password}
          onChange={(e) =>
            setLoginRequest({ ...loginrequest, password: e.target.value })
          }
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
