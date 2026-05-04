import React from "react";
import { UserSummary } from "../../../adapters/api";

interface Props {
    user: UserSummary;
    role: "USER" | "ADMIN";
    onChangeRole: (role: "USER" | "ADMIN") => void;
    onClose: () => void;
    onSave: () => void;
}

const EditUserModal: React.FC<Props> = ({
    user,
    role,
    onChangeRole,
    onClose,
    onSave,
}) => {
    return (
        <div
            style={{
                position: "fixed",
                inset: 0,
                background: "rgba(0,0,0,0.4)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                zIndex: 9999,
            }}
        >
            <div
                style={{
                    background: "#fff",
                    padding: 20,
                    borderRadius: 10,
                    minWidth: 320,
                }}
            >
                <h3>Change Role</h3>
                <p><b>{user.name}</b></p>

                <label>
                    <input
                        type="radio"
                        checked={role === "USER"}
                        onChange={() => onChangeRole("USER")}
                    />
                    User
                </label>

                <label style={{ marginLeft: 10 }}>
                    <input
                        type="radio"
                        checked={role === "ADMIN"}
                        onChange={() => onChangeRole("ADMIN")}
                    />
                    Admin
                </label>

                <div style={{ marginTop: 20, display: "flex", justifyContent: "flex-end", gap: 10 }}>
                    <button onClick={onClose}>Cancel</button>
                    <button onClick={onSave}>Save</button>
                </div>
            </div>
        </div>
    );
};

export default EditUserModal;