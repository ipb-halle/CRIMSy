import React, { useState } from "react";
import SearchPanel from "./SearchPanel";
import UsersPanel from "../components/UsersPanel";
import RolePanel from "../components/RolePanel";
import { useAuth } from "../../adapters/hooks/useAuth";
import Navigation from "../components/Navigation";
import { colors, spacing, radius } from "../theme/designTokens";

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

    const [view, setView] = useState<View>("home");

    const handleTab = (next: View) => {
        setView(next);
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
                            background: colors.surface,
                            borderRadius: radius.md,
                            border: `1px solid ${colors.border}`,
                        }}
                    >
                        <h2>Welcome to IPB Laboratory System</h2>
                        <p>Select a menu item to continue.</p>
                    </div>
                );
        }
    };

    return (
        <div style={{ background: colors.background, minHeight: "100vh" }}>

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
                    borderBottom: `1px solid ${colors.border}`,
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
                    color: colors.textSecondary,
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
                        background: colors.surface,
                        borderRadius: radius.sm,
                        border: `1px solid ${colors.border}`,
                        animation: "fadeIn 0.3s ease",
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
            background: active ? colors.primary : colors.surface,
            color: active ? "white" : colors.textPrimary,
            border: active ? "none" : `1px solid ${colors.border}`,
            fontWeight: active ? 600 : 500,
            borderRadius: "999px",
            transition: "all 0.2s ease",
            cursor: "pointer",
        }}
    >
        {label}
    </button>
);

export default Dashboard;
