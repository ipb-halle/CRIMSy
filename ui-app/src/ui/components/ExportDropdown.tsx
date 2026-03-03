import React from "react";
import {
    exportToCSV,
    exportToJSON,
    exportToExcel,
    exportToPDF,
    ExportFormat,
} from "../utils/exportUtils";
import { Material } from "../../data/dummyData";
import { spacing, radius } from "../../assets/css/theme/designTokens";

interface Props {
    items: Material[];
}

const ExportDropdown: React.FC<Props> = ({ items }) => {
    const handle = (format: ExportFormat) => {
        const opts = {
            filename: `export-${new Date().toISOString()}`,
            includeHeader: true,
            branding: true,
        };

        switch (format) {
            case "csv":
                exportToCSV(items, opts);
                break;
            case "json":
                exportToJSON(items, opts);
                break;
            case "xlsx":
                exportToExcel(items, opts);
                break;
            case "pdf":
                exportToPDF(items, opts);
                break;
        }
    };

    return (
        <div style={{ display: "flex", gap: spacing.sm }}>
            <select
                onChange={(e) => handle(e.target.value as ExportFormat)}
                style={{
                    padding: spacing.sm,
                    borderRadius: radius.sm,
                }}
            >
                <option value="">Export...</option>
                <option value="csv">CSV</option>
                <option value="json">JSON</option>
                <option value="xlsx">Excel (.xlsx)</option>
                <option value="pdf">PDF</option>
            </select>
        </div>
    );
};

export default ExportDropdown;