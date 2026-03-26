import React from "react";
import { useAuth } from "../../adapters/hooks/useAuth";
import LoginForm from "../components/LoginForm";
import { radius, spacing } from "../../assets/css/theme/designTokens";
import { useTheme } from "../../assets/css/theme/ThemeContext";

interface Props {
  auth: ReturnType<typeof useAuth>;
  onLogin: () => void;
}

const Login: React.FC<Props> = ({ auth, onLogin }) => {
  const {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    handleLogin,
  } = auth;

  const { theme } = useTheme();

  const isError =
    result?.message?.toString().toLowerCase().includes("failed") ||
    result?.message?.toString().toLowerCase().includes("expired") ||
    result?.message?.toString().toLowerCase().includes("unauthorized");

  return (
    <div
      style={{
        border: `1px solid ${theme.border}`,
        borderRadius: radius.md,
        padding: spacing.lg,
        background: theme.surface,
        maxWidth: "420px",
        margin: "3rem auto",
      }}
    >

      <h2 style={{ marginBottom: spacing.md }}>Login</h2>

      {result?.message && (
        <div
          style={{
            marginBottom: spacing.md,
            color: isError ? theme.danger : theme.success,
            border: `1px solid ${isError ? theme.danger : theme.success}`,
            borderRadius: radius.sm,
            padding: spacing.sm,
          }}
        >
          {result.message}
        </div>
      )}

      <LoginForm
        loginrequest={loginRequest}
        setLoginRequest={setLoginRequest}
        errors={errors}
        onSubmit={(e) => handleLogin(e, onLogin)}
      />
    </div>
  );
};

export default Login;
