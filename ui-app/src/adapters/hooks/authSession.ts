const STORAGE_KEYS = {
    token: "token",
    expires: "expiresInSeconds",
    username: "username",
    userId: "userId",
} as const;

type StorageKey = typeof STORAGE_KEYS[keyof typeof STORAGE_KEYS];

const get = (key: StorageKey): string | null => localStorage.getItem(key);

const set = (key: StorageKey, value: string) => localStorage.setItem(key, value);

const remove = (key: StorageKey) => localStorage.removeItem(key);

export const authSession = {
    getToken: (): string | null => get(STORAGE_KEYS.token),

    setSession: (token: string, expiresInSeconds: number) => {
        set(STORAGE_KEYS.token, token);
        set(STORAGE_KEYS.expires, String(expiresInSeconds));
    },

    setUser: (username: string, userId: number) => {
        set(STORAGE_KEYS.username, username);
        set(STORAGE_KEYS.userId, String(userId));
    },

    getExpires: (): number => {
        const value = get(STORAGE_KEYS.expires);
        const parsed = value ? Number(value) : 0;
        return Number.isFinite(parsed) && parsed > 0 ? parsed : 60;
    },

    clear: () => {
        remove(STORAGE_KEYS.token);
        remove(STORAGE_KEYS.expires);
        remove(STORAGE_KEYS.username);
        remove(STORAGE_KEYS.userId);
    },
};