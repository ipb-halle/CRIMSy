import { blob } from "stream/consumers";
import { Material } from "../../data/dummyData";
import { isTemplateExpression } from "typescript";

export type ExportFormat = "csv" | "json" | "xlsx" | "pdf";

export interface ExportOptions {
    filename?: string;
    includeHeader?: boolean;
    branding?: boolean;
}

export const exportToJSON = (items: Material[], options: ExportOptions = {}) => {
    const blob = new Blob([JSON.stringify(items, null, 2)], { type: "application/json" });
    triggerDownload(blob, options.filename || "export.json");
}

export const exportToCSV = (items: Material[], options: ExportOptions = {}) => {
    const rows = items.map(m => ({
        id: m.materialId,
        project: m.projectId,
        onwer: m.ownerId,
        server: m.serverId,
        status: m.deactivated ? "DEactivated" : "Active",
    }));

    const header = options.includeHeader
        ? "ID,Project,Owner,Server,Status\n"
        : "";

    const csv = header + rows.map(r =>
        `${r.id},${r.project},${r.onwer},${r.server},${r.status}`
    ).join("\n");

    const blob = new Blob([csv], { type: "text/csv" });
    triggerDownload(blob, options.filename || "export.csv");
};

export const exportToExcel = async (items: Material[],
    options: ExportOptions = {}) => {
    const { utils, writeFile } = await import("xlsx");
    const rows = items.map(m => ({
        ID: m.materialId,
        Project: m.projectId,
        Onwer: m.ownerId,
        Server: m.serverId,
        Status: m.deactivated ? "DEactivated" : "Active",
    }));

    const ws = utils.json_to_sheet(rows);
    const wb = utils.book_new();
    utils.book_append_sheet(wb, ws, "Results");
    writeFile(wb, options.filename || "export.xlsx");
};

export const exportToPDF = async (items: Material[], options: ExportOptions = {}) => {
    const { jsPDF } = await import("jspdf");
    const doc = new jsPDF();

    if (options.branding) {
        doc.setFontSize(16);
        doc.text("IPB Laboratory Results", 14, 20);
        doc.setFontSize(10);
        doc.text(`Records: ${items.length}`, 14, 28);
    }

    let y = options.branding ? 40 : 20;
    items.forEach(m => {
        doc.text(
            `ID: ${m.materialId} | Project: ${m.projectId} | Owner: ${m.ownerId}`,
            14,
            y
        );
        y += 8;
    });
    doc.save(options.filename || "export.pdf");
};

const triggerDownload = (blob: Blob, filename: string) => {
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
};