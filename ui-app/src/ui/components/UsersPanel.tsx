import React, { useState, useEffect } from "react";
import { PagedUsers } from "../../adapters/api";


interface UsersPanelProps {
  data: PagedUsers | null;
  onPageChange?: (page: number) => Promise<void> | void;
}


const UsersPanel: React.FC<UsersPanelProps> = ({ data, onPageChange }) => {
  const [currentPage, setCurrentPage] = useState<number>(data?.currentPage || 1);

  useEffect(() => {
    if (data?.currentPage) {
      setCurrentPage(data.currentPage);
    }
  }, [data]);

  if (!data) return null;

  const handlePrev = () => {
    if (currentPage > 1) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      onPageChange?.(newPage);
    }
  };

  const handleNext = () => {
    if (currentPage < data.totalPages) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      onPageChange?.(newPage);
    }
  };

  return (
    <div style={{ marginTop: "1rem", padding: "0.75rem", border: "1px solid #029ACF", borderRadius: "3px", textAlign: "left", fontWeight: "bold", wordBreak: "break-all" }}>
      <div>Users (Page {data.currentPage} of {data.totalPages}, Total: {data.totalUsers}):</div>
      {data.users.map((u, idx) => (
        <div key={idx} style={{ marginLeft: "1rem", marginTop: "0.5rem" }}>
          <div>ID: {u.id}</div>
          <div>Name: {u.name}</div>
          <div>Type: {u.membertype}</div>
        {/*  {u.info && <div style={{ color: "red" }}>{u.info}</div>}*/}
        </div>
      ))}

      {/* Pagination controles */}
      <div style={{ marginTop: "1rem" }}>
        <button onClick={handlePrev} disabled={data.currentPage <= 1} style={{ marginRight: "0.5rem" }}>
          Previous
        </button>
        <button onClick={handleNext} disabled={data.currentPage >= data.totalPages} >
          Next
        </button>
      </div>
    </div>
  );
};

export default UsersPanel;
