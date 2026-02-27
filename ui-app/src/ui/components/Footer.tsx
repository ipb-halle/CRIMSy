import React from "react";
import { radius, spacing } from "../theme/designTokens";

interface FooterProps {
    buildNumber?: string;
}

const Footer: React.FC<FooterProps> = ({ buildNumber }) => {
    return (
        <footer
            className="Footer"
            style={{
                color: "white",
                padding: spacing.lg,
                background: "linear-gradient(90deg, #00509e, #00843d)"
            }}>
            <div
                style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                    display: "flex",
                    justifyContent: "space-around",
                    flexWrap: "wrap",
                    gap: spacing.md,
                }}>
                <div>
                    <span>CRIMSy-Version: {buildNumber || "N/A"}</span><br />
                    <a href="/dsgvo" target="_blank" rel="noopener noreferrer">GDPR</a> /
                    <a href="/funding" target="_blank" rel="noopener noreferrer">Funding</a>
                </div>
                <div style={{ textAlign: "right" }}>
                    <span>© Leibniz-Institut f. Pflanzenbiochemie</span>
                    <br />
                    <span>Licensed under </span>
                    <a
                        href="https://www.apache.org/licenses/LICENSE-2.0"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ color: "#dff" }}>
                        Apache License, Version 2.0
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
