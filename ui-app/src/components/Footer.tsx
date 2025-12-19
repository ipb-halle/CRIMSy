import React from "react";

interface FooterProps {
    buildNumber?: string;
}

const Footer: React.FC<FooterProps> = ({ buildNumber }) => {
    return (
        <footer style={{ padding: "1rem", backgroundColor: "#f5f5f5", marginTop: "2rem" }}>
            <div style={{ display: "flex", justifyContent: "space-around", flexWrap: "wrap" }}>
                <div>
                    <span>CRIMSy-Version: {buildNumber || "N/A"}</span><br />
                    <a href="/dsgvo" target="_blank" rel="noopener noreferrer">GDPR</a> /
                    <a href="/funding" target="_blank" rel="noopener noreferrer">Funding</a>
                </div>
                <div>
                    <span>© Leibniz-Institut f. Pflanzenbiochemie</span><br />
                    <span>Licensed under </span>
                    <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank" rel="noopener noreferrer">
                        Apache License, Version 2.0
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
