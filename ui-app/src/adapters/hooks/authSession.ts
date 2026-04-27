const STORAGE_KEYS = {
    token: "token",
    expires: "expiresInSeconds",
    username: "username",
    userId: "userId",
} as const;

export const sessionStorage = {
    getToken: () => localStorage.getItem(STORAGE_KEYS.token),

    setSession: (token: string, expiresInSeconds: number) => {
        localStorage.setItem(STORAGE_KEYS.token, token);
        localStorage.setItem(STORAGE_KEYS.expires, String(expiresInSeconds));
    },

    setUser: (username: string, userId: number) => {
        localStorage.setItem(STORAGE_KEYS.username, username);
        localStorage.setItem(STORAGE_KEYS.userId, String(userId));
    },

    getExpires: () =>
        Number(localStorage.getItem(STORAGE_KEYS.expires)) || 60,

    clear: () => {
        Object.values(STORAGE_KEYS).forEach((key) =>
            localStorage.removeItem(key)
        );
    },
};