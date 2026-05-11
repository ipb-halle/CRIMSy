import React, { useMemo, useState } from "react";
import DataTable, { TableColumn } from "react-data-table-component";
import { Material, dummyData } from "../../../data/dummyData";
import { useTheme } from "../../../assets/css/theme/ThemeContext";

interface TableViewProps {
  items: Material[];
}

type Row = Material & {
  projectName?: string;
  ownerName?: string;
  serverName?: string;
};

const TableView: React.FC<TableViewProps> = ({ items }) => {
  const { theme } = useTheme();

  // ------------------------------
  // UI state (search + filter)
  // ------------------------------
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | "active" | "deactivated">("all");

  // ------------------------------
  // O(1) lookups maps
  // ------------------------------
  const lookups = useMemo(() => {
    const projectMap = new Map(dummyData.projects.map(p => [p.id, p.name]));
    const userMap = new Map(dummyData.usersGroups.map(u => [u.id, u.name]));
    const serverMap = new Map(dummyData.servers.map(s => [s.id, s.name]));

    return { projectMap, userMap, serverMap };
  }, []);

  // ------------------------------
  // Enrich data once (No .find())
  // ------------------------------
  const enrichedItems: Row[] = useMemo(() => {
    return items.map(m => ({
      ...m,
      projectName: lookups.projectMap.get(m.projectId),
      ownerName: lookups.userMap.get(m.ownerId),
      serverName: lookups.serverMap.get(m.serverId),
    }))
  }, [items, lookups]);


  // ------------------------------
  // Filtering + search (table-level)
  // ------------------------------
  const filteredItems = useMemo(() => {
    const q = search.trim().toLowerCase();

    return enrichedItems.filter(item => {
      const matchesSearch =
        !q ||
        item.materialId.toString().includes(q) ||
        item.materialName?.toLowerCase().includes(q) ||
        item.projectName?.toLowerCase().includes(q) ||
        item.ownerName?.toLowerCase().includes(q) ||
        item.serverName?.toLowerCase().includes(q);

      const matchesStatus =
        statusFilter === "all" ||
        (statusFilter === "active" && !item.deactivated) ||
        (statusFilter === "deactivated" && item.deactivated);

      return matchesSearch && matchesStatus;
    });
  }, [enrichedItems, search, statusFilter]);


  // -----------------------------
  // Columns
  // -----------------------------
  const columns: TableColumn<Row>[] = useMemo(
    () => [
      {
        name: "ID",
        selector: (row) => row.materialId,
        sortable: true,
        width: "80px",
      },
      {
        name: "Name",
        selector: (row) => row.materialName ?? "",
        sortable: true,
      },
      {
        name: "Project",
        selector: (row) => row.projectName ?? "-",
        sortable: true,
      },
      {
        name: "Owner",
        selector: (row) => row.ownerName ?? "-",
      },
      {
        name: "Server",
        selector: (row) => row.serverName ?? "-",
      },
      {
        name: "Status",
        selector: (row) => (row.deactivated ? "Deactivated" : "Active"),
        sortable: true,
      },
    ],
    []
  );

  // -----------------------------
  // Custom header (search + filter)
  // -----------------------------
  const subHeaderComponent = useMemo(() => {
    return (
      <div style={{ display: "flex", gap: "1rem", padding: "0.5rem" }}>
        <input
          type="text"
          placeholder="Search materials..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            padding: "0.5rem",
            borderRadius: "6px",
            border: "1px solid #ccc",
          }}
        />

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as any)}
        >
          <option value="all">All</option>
          <option value="active">Active</option>
          <option value="deactivated">Deactivated</option>
        </select>
      </div>
    );
  }, [search, statusFilter]);

  // -----------------------------
  // Render
  // -----------------------------
  return (
    <DataTable
      columns={columns}
      data={filteredItems}
      pagination
      highlightOnHover
      dense
      subHeader
      subHeaderComponent={subHeaderComponent}
      persistTableHead
      customStyles={{
        headRow: {
          style: {
            backgroundColor: theme.primary,
            color: "white",
          },
        },
        rows: {
          style: {
            backgroundColor: theme.surface,
          },
        },
      }}
    />
  );
};

export default TableView;