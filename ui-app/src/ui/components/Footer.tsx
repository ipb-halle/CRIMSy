import React from "react";
import { spacing } from "../../assets/css/theme/designTokens";

interface FooterProps {
    buildNumber?: string;
}

const Footer: React.FC<FooterProps> = ({ buildNumber }) => {
    return (
        <footer
            className="Footer"
            style={{
                position: "sticky",
                bottom: 0,
                width: "100%",
                color: "#e8f4ff",
                padding: "0.5rem 1rem",
                fontSize: "0.8rem",
                borderTop: "2px solid #00a651",
                background: "linear-gradient(90deg, #003c78 0%, #007a33 100%)",
                boxShadow: "0 -2px 8px rgba(0,0,0,0.08)"
            }}>
            <div
                style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                    display: "flex",
                    justifyContent: "space-around",
                    alignItems: "center",
                    flexWrap: "wrap",
                    gap: spacing.md,
                }}
            >

                <div style={{ opacity: 0.9 }}>
                    © {new Date().getFullYear()} Leibniz-Institut für Pflanzenbiochemie
                </div>

                <div style={{ opacity: 0.85 }}>
                    CRIMSy v{buildNumber || "N/A"}
                </div>

                <div style={{ display: "flex", gap: "1rem" }}>
                    <a href="/dsgvo" style={{ color: "#cfe9ff", textDecoration: "none" }}>GDPR</a> /
                    <a href="/funding" style={{ color: "#cfe9ff", textDecoration: "none" }}>Funding</a>
                    <a
                        href="https://www.apache.org/licenses/LICENSE-2.0"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ color: "#cfe9ff", textDecoration: "none" }}>
                        Apache 2.0
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
