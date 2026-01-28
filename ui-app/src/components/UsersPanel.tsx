import React from "react";

interface User {
  id: string;
  login: string;
  name: string;
  membertype: string;
  info?: string;
}

interface Props {
  users: User[] | null;
}

const UsersPanel: React.FC<Props> = ({ users }) => {
  if (!users) return null;

  return (
    <div style={{ marginTop: "1rem", padding: "0.75rem", border: "1px solid #029ACF", borderRadius: "3px", textAlign: "left", fontWeight: "bold", wordBreak: "break-all" }}>
      <div>Users:</div>
      {users.map((u, idx) => (
        <div key={idx} style={{ marginLeft: "1rem", marginTop: "0.5rem" }}>
          <div>ID: {u.id}</div>
          <div>Login: {u.login}</div>
          <div>Name: {u.name}</div>
          <div>Type: {u.membertype}</div>
          {u.info && <div style={{ color: "red" }}>{u.info}</div>}
        </div>
      ))}
    </div>
  );
};

export default UsersPanel;
