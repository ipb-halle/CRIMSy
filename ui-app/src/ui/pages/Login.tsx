import React from "react";
import { useAuth } from "../../adapters/hooks/useAuth";
import LoginForm from "../components/LoginForm";
import { colors, radius, spacing } from "../theme/designTokens";

interface Props {
  onLogin: () => void;
}

const Login: React.FC<Props> = ({ onLogin }) => {
  const {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    handleLogin,
  } = useAuth();

  const isError =
    result?.message?.toString().toLowerCase().includes("failed") ||
    result?.message?.toString().toLowerCase().includes("expired") ||
    result?.message?.toString().toLowerCase().includes("unauthorized");

  return (
    <div
      style={{
        border: `1px solid ${colors.border}`,
        borderRadius: radius.md,
        padding: spacing.lg,
        background: colors.surface,
        maxWidth: "420px",
        margin: "3rem auto",
      }}
    >

      <h2 style={{ marginBottom: spacing.md }}>Login</h2>

      {result && (
        <div
          style={{
            marginBottom: spacing.md,
            color: isError ? colors.danger : colors.success,
            border: `1px solid ${isError ? colors.danger : colors.success}`,
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
