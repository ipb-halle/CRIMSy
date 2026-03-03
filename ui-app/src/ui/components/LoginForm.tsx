import React, { FormEvent } from "react";
import { LoginRequest } from "../../adapters/api";
import { useTheme } from "../../assets/css/theme/ThemeContext";
import { radius, spacing } from "../../assets/css/theme/designTokens";

interface Props {
  loginrequest: LoginRequest;
  setLoginRequest: (v: LoginRequest) => void;
  errors: { username?: string; password?: string };
  onSubmit: (e?: FormEvent) => void;
}

const LoginForm: React.FC<Props> = ({
  loginrequest,
  setLoginRequest,
  errors,
  onSubmit,
}) => {

  const { theme } = useTheme();

  return (
    <form onSubmit={onSubmit}>
      <div style={{ marginBottom: spacing.md }} >
        <input
          style={{
            width: "100%",
            padding: spacing.sm,
            borderRadius: radius.sm,
            border: `1px solid ${theme.border}`,
          }}
          placeholder="Username"
          value={loginrequest.login}
          onChange={(e) => setLoginRequest({ ...loginrequest, login: e.target.value })
          }
        />
        {errors.username && (
          <div style={{ color: theme.danger }}>{errors.username}</div>
        )}
      </div>

      <div
        style={{ marginBottom: spacing.md }}>
        <input
          type="password"
          style={{
            width: "100%",
            padding: spacing.sm,
            borderRadius: radius.sm,
            border: `1px solid ${theme.border}`,
          }}
          placeholder="Password"
          value={loginrequest.password}
          onChange={(e) => setLoginRequest({ ...loginrequest, password: e.target.value })}
        />
        {errors.password && (
          <div style={{ color: theme.danger }}>{errors.password}</div>
        )}
      </div>

      <button
        type="submit"
        style={{
          width: "100%",
          padding: spacing.sm,
          background: theme.primary,
          color: "white",
          border: "none",
          borderRadius: radius.sm,
          fontWeight: 600,
          cursor: "pointer",
        }}
      >
        Login
      </button>
    </form>
  );
};

export default LoginForm;