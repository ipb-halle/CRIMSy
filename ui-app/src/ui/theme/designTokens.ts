export type ThemeMode = "light" | "dark";

export interface ThemeColors {
  primary: string;
  primaryDark: string;
  accent: string;
  background: string;
  surface: string;
  border: string;
  textPrimary: string;
  textSecondary: string;
  success: string;
  danger: string;
  warning: string;
}

export const lightTheme: ThemeColors = {
  primary: "#002855",
  primaryDark: "#001a3d",
  accent: "#00509e",
  background: "#f4f7f9",
  surface: "#ffffff",
  border: "#d9e2ec",
  textPrimary: "#102a43",
  textSecondary: "#486581",
  success: "#2e7d32",
  danger: "#c62828",
  warning: "#f9a825",
};

export const darkTheme: ThemeColors = {
  primary: "#001a3d",
  primaryDark: "#000d1f",
  accent: "#003f7d",
  background: "#0f172a",
  surface: "#1e293b",
  border: "#334155",
  textPrimary: "#f8fafc",
  textSecondary: "#cbd5e1",
  success: "#4caf50",
  danger: "#ef5350",
  warning: "#ffb74d",
};

export const spacing = {
  xs: "0.25rem",
  sm: "0.5rem",
  md: "1rem",
  lg: "1.5rem",
};

export const radius = {
  sm: "4px",
  md: "10px",
  lg: "14px",
};