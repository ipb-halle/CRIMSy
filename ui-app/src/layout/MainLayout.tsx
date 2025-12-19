import React, { ReactNode } from "react";
import Navigation from "../components/Navigation";
import Footer from "../components/Footer";
import HeadMeta from "../components/HeadMeta";

interface MainLayoutProps {
    isPublicAccount: boolean;
    userName?: string;
    permissions?: string[];
    buildNumber?: string;
    onLogout?: () => void;
    navigate: (page: "home" | "login") => void;
    children: ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({
    isPublicAccount,
    userName,
    permissions,
    buildNumber,
    onLogout,
    navigate,
    children,
}) => {
    return (
        <>
            <HeadMeta />
            <Navigation
                isPublicAccount={isPublicAccount}
                userName={userName}
                permissions={permissions}
                onLogout={onLogout}
                navigate={navigate}
            />
            <main style={{ margin: "0 auto", padding: "1rem" }}>{children}</main>
            <Footer buildNumber={buildNumber} />
        </>
    );
};

export default MainLayout;

