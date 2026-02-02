import React from "react";

interface Role {
  username: string;
  groups: string;
  admin: string;
}

interface Props {
  role: Role | null;
}

const RolePanel: React.FC<Props> = ({ role }) => {
  if (!role) return null;

  return (
    <div style={{ marginTop: "1rem", padding: "0.75rem", border: "1px solid #029ACF", borderRadius: "3px", textAlign: "left", fontWeight: "bold", wordBreak: "break-all" }}>
      <div>Username: {role.username}</div>
      <div>Groups: {role.groups}</div>
      <div>Admin Access: {role.admin}</div>
    </div>
  );
};

export default RolePanel;
