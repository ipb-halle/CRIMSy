import React, { useEffect, useState } from "react";

const RoleMessage: React.FC = () => {
    const [message, setMessage] = useState<string>("");

    useEffect(() => {
        const fetchRoleMessage = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                setMessage("You must log in first.");
                return;
            }

            try {
                const response = await fetch(
                    "https://compchem17.ipb-halle.de/ui/rest/role",
                    {
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );

                if (!response.ok) {
                    const data = await response.json();
                    setMessage(data.message || "Failed to fetch role message");
                    return;
                }

                const data = await response.json();
                setMessage(data.message);
            } catch (err) {
                console.error(err);
                setMessage("Request failed. Please try again later.");
            }
        };

        fetchRoleMessage();
    }, []);

    return (
        <div style={{ margin: "2rem", padding: "1rem", border: "1px solid #029ACF" }}>
            <h2>Role Information</h2>
            <p>{message}</p>
        </div>
    );
};

export default RoleMessage;
