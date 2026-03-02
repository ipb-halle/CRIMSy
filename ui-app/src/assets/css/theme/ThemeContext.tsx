import React, { createContext, useContext } from "react";
import { colors } from "./designTokens";

interface ThemeContextValue {
  theme: typeof colors;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ThemeContext.Provider value={{ theme: colors }}>
    {children}
  </ThemeContext.Provider>
);

export const useTheme = () => {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
};