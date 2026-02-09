import { useAuth } from "../../adapters/hooks/useAuth";
import LoginForm from "../components/LoginForm";
import RolePanel from "../components/RolePanel";
import UsersPanel from "../components/UsersPanel";
import SearchPanel from "../components/SearchPanel";


const Login = () => {
  const {
    username,
    password,
    setUsername,
    setPassword,
    errors,
    result,
    isLoggedIn,
    roleInfo,
    usersList,
    handleLogin,
    handleLogout,
    handleCheckRole,
    handleFetchUsers,
  } = useAuth();

  const isError =
    result?.message.toLowerCase().includes("failed") ||
    result?.message.toLowerCase().includes("expired") ||
    result?.message.toLowerCase().includes("unauthorized");

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

        {isLoggedIn && (
          <div style={{ marginTop: "20px", textAlign: "center" }}>
            <SearchPanel />
          </div>
        )}
        {isLoggedIn ? (
          <div style={{ textAlign: "center", marginTop: "1rem" }}>

            <button
              onClick={handleCheckRole}
              style={{ padding: "0.5rem 1.5rem", marginRight: "0.5rem" }}
            >
              Check My Role
            </button>

            <button
              onClick={() => handleFetchUsers(1)}
              style={{ padding: "0.5rem 1.5rem", marginRight: "0.5rem" }}
            >
              View Users
            </button>


            <button
              onClick={handleLogout}
              style={{
                padding: "0.5rem 1.5rem",
                background: "red",
                color: "white",
                border: "none",
                borderRadius: "3px",
              }}
            >Logout</button>

            <RolePanel role={roleInfo} />
            <UsersPanel data={usersList} onPageChange={handleFetchUsers} />
          </div>
        ) : (
          <LoginForm
            username={username}
            password={password}
            setUsername={setUsername}
            setPassword={setPassword}
            errors={errors}
            result={result}
            onSubmit={handleLogin}
          />
        )}
      </fieldset>
    </div>
  );
};

export default Login;
