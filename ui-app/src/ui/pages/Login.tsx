import React from "react";
import { useAuth } from "../../adapters/hooks/useAuth";
import LoginForm from "../components/LoginForm";

interface Props {
  onLogin: () => void;
}

const Login: React.FC<Props> = ({ onLogin }) => {
  const {
    loginRequest,
    setLoginRequest,
    errors,
    result,
    isLoggedIn,
    handleLogin,
  } = useAuth();

  const isError =
    result?.message?.toString().toLowerCase().includes("failed") ||
    result?.message?.toString().toLowerCase().includes("expired") ||
    result?.message?.toString().toLowerCase().includes("unauthorized");

  return (
    <div
      style={{
        border: "1px solid #029ACF",
        borderRadius: "3px",
        padding: "1rem",
        maxWidth: "400px",
        margin: "2rem auto",
      }}
    >
      <fieldset style={{ border: "none" }}>
        <legend style={{ fontSize: "1.25rem", fontWeight: "bold" }}>
          Login
        </legend>

        {result && (
          <div
            style={{
              textAlign: "center",
              margin: "1rem 0",
              color: isError ? "red" : "green",
              border: "1px solid #029ACF",
              borderRadius: "3px",
              padding: "0.5rem",
            }}
          >
            {result.message}
          </div>
        )}
        <LoginForm
          loginrequest={loginRequest}
          setLoginRequest={setLoginRequest}
          errors={errors}
          result={result?.message ?? ""}
          onSubmit={(e) => handleLogin(e, onLogin)}
        />
      </fieldset>
    </div>
  );
};

export default Login;
