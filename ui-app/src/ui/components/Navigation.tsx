import React from "react";

interface NavigationProps {
    isPublicAccount: boolean;
    userName?: string;
    permissions?: string[];
    onLogout?: () => void;
    navigate: (page: "home" | "login") => void;
}

const Navigation: React.FC<NavigationProps> = ({
    isPublicAccount,
    userName,
    permissions = [],
    onLogout,
    navigate,
}) => {
    const hasPermission = (perm: string) => permissions.includes(perm);

    return (
        <nav style={{ display: "flex", justifyContent: "space-evenly", margin: "1rem auto", alignItems: "center" }}>
            <button onClick={() => navigate("home")}>Home</button>

            {isPublicAccount ? (
                <button style={{ color: "#4B0082" }} onClick={() => navigate("login")}>
                    Login
                </button>
            ) : (
                <div>
                    <span>{userName} ⚙️</span>
                    <ul>
                        <li>
                            <button onClick={() => navigate("home")}>My Account</button>
                        </li>
                        {hasPermission("ADMISSION_MGR_ENABLE") && <li><button>Manage Users</button></li>}
                        <li>
                            <button onClick={onLogout}>Logout</button>
                        </li>
                    </ul>
                </div>
            )}
        </nav>
    );
};

export default Navigation;
