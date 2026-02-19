import React, { useState } from "react";
import { SearchPanel } from "./SearchPanel";
import UsersPanel from "../components/UsersPanel";
import RolePanel from "../components/RolePanel";
import { useAuth } from "../../adapters/hooks/useAuth";

const Dashboard: React.FC<{ onLogout: () => void }> = ({ onLogout }) => {
    const [activeTab, setActiveTab] = useState<"search" | "role" | "users">("search");
    const { roleInfo, usersList, handleCheckRole, handleFetchUsers } = useAuth();

    const renderContent = () => {
        switch (activeTab) {
            case "search":
                return <SearchPanel />;
            case "role":
                return (
                    <div style={{ padding: "1rem" }}>
                        <button onClick={handleCheckRole}>Reload Role</button>
                        <RolePanel role={roleInfo} />
                    </div>
                );
            case "users":
                return (
                    <div style={{ padding: "1rem" }}>
                        <button onClick={() => handleFetchUsers(1)}>Reload Users</button>
                        <UsersPanel data={usersList} onPageChange={handleFetchUsers} />
                    </div>
                );
        }
    };

    return (
        <div style={{ maxWidth: "1200px", margin: "1rem auto", display: "flex", flexDirection: "column", gap: "1rem" }}>
            {/* Menu */}
            <div style={{ display: "flex", gap: "1rem", flexWrap: "wrap" }}>
                <button onClick={() => setActiveTab("search")}>Search</button>
                <button onClick={() => setActiveTab("role")}>Check Role</button>
                <button onClick={() => setActiveTab("users")}>Users List</button>
                <button
                    onClick={() => {
                        localStorage.removeItem("token");
                        localStorage.removeItem("username");
                        onLogout();
                    }}
                    style={{ background: "red", color: "white" }}
                >
                    Logout
                </button>
            </div>

            {/* Content */}
            <div style={{
                border: "1px solid #029ACF",
                borderRadius: "6px",
                background: "#fff",
                overflowX: "auto",
                minHeight: "400px",
                padding: "1rem"
            }}>
                {renderContent()}
            </div>
        </div>
    );
};

export default Dashboard;
