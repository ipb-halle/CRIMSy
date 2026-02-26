import React, { useState } from "react";
import SearchPanel from "./SearchPanel";
import UsersPanel from "../components/UsersPanel";
import RolePanel from "../components/RolePanel";
import { useAuth } from "../../adapters/hooks/useAuth";
import Navigation from "../components/Navigation";
import { useTheme } from "../theme/ThemeContext";
import { spacing, radius } from "../theme/designTokens";

type View = "home" | "search" | "role" | "users";


interface Props {
    onLogout: () => void;
}
const Dashboard: React.FC<Props> = ({ onLogout }) => {
    const {
        roleInfo,
        usersList,
        handleLogout,
        handleCheckRole,
        handleFetchUsers,
    } = useAuth();

    const { theme, mode, toggle } = useTheme();
    const [view, setView] = useState<View>("home");

    const isAdmin = roleInfo?.admin;

    const handleTab = (next: View) => {
        setView(next);

        // trigger backend actions for role/users when selected
        if (next === "role") handleCheckRole();
        if (next === "users") handleFetchUsers(1);
    };

    const renderView = () => {
        switch (view) {
            case "search":
                return <SearchPanel />;
            case "role":
                return <RolePanel role={roleInfo} />;
            case "users":
                return (
                    <UsersPanel
                        data={usersList}
                        onPageChange={(page) => handleFetchUsers(page)}
                    />
                );

            default:
                return (
                    <div
                        style={{
                            padding: spacing.lg,
                            background: theme.surface,
                            borderRadius: radius.md,
                            border: `1px solid ${theme.border}`,
                        }}
                    >
                        <h2>Welcome to IPB Laboratory System</h2>
                        <p>Select a menu item to continue.</p>
                    </div>
                );
        }
    };

    return (
        <div style={{ background: theme.background, minHeight: "100vh" }}>

            <Navigation
                onLogout={() => {
                    handleLogout();
                    onLogout();
                }}
            />

            {/* TAB MENU */}
            <div
                style={{
                    display: "flex",
                    gap: spacing.md,
                    padding: spacing.sm,
                    borderBottom: `1px solid ${theme.border}`,
                }}
            >
                <Tab
                    label="Home"
                    active={view === "home"}
                    onClick={() => setView("home")} />
                <Tab
                    label="Search"
                    active={view === "search"}
                    onClick={() => handleTab("search")}
                />

                <Tab
                    label="Role Check"
                    active={view === "role"}
                    onClick={() => handleTab("role")}
                />
                <Tab
                    label="User List"
                    active={view === "users"}
                    onClick={() => handleTab("users")}
                />

            </div>

            {/* BREADCRUMB */}
            <div
                style={{
                    padding: spacing.sm,
                    fontSize: "0.85rem",
                    color: theme.textSecondary,
                }}
            >
                Home {view !== "home" && ` / ${view}`}
            </div>

            {/* NOTIFICATION */}
            {roleInfo && (
                <div
                    style={{
                        margin: spacing.sm,
                        padding: spacing.sm,
                        background: theme.surface,
                        borderRadius: radius.sm,
                        border: `1px solid ${theme.border}`,
                    }}
                >
                    Welcome, {roleInfo.username}
                </div>
            )}

            {/* CONTENT AREA */}
            <div style={{ padding: spacing.lg }}>{renderView()}</div>

        </div>
    );
};

interface TabProps {
    label: string;
    active: boolean;
    onClick: () => void;
}
const Tab: React.FC<TabProps> = ({ label, active, onClick }) => (
    <button
        onClick={onClick}
        style={{
            padding: "0.4rem 0.9rem",
            background: active ? "#00509e" : "white",
            color: active ? "white" : "black",
            border: active ? "none" : `1px solid #ccc`,
            fontWeight: active ? 600 : 500,
            borderRadius: "999px",
            transition: "all 0.2s ease",
            cursor: "pointer",
            boxShadow: active ? "0 2px 4px rgba(0,0,0,0.15)" : "none",
        }}
    >
        {label}
    </button>
);

export default Dashboard;
