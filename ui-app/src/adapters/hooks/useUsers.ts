import { useState, useCallback } from "react";
import { usersApi } from "../apiClient";
import { PaginatedUserResponse, UserSummary } from "../api";

export const useUsers = () => {
    const [loading, setLoading] = useState(false);

    const deleteUser = useCallback(async (id: number) => {
        try {
            setLoading(true);
            await usersApi().deleteUser({ id });
            return true;
        } catch (err) {
            console.error("[Delete user error]", err);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const updateUser = useCallback(
        async (user: UserSummary, role: "USER" | "ADMIN") => {
            try {
                setLoading(true);

                const isAdmin = role === "ADMIN";

                await usersApi().updateUser({
                    id: user.id,
                    userSummary: {
                        ...user,
                        admin: isAdmin,
                        groups: isAdmin ? ["Users", "Admin Group"] : ["Users"],
                    },
                });

                return true;
            } catch (err) {
                console.error("[Update user error]", err);
                throw err;
            } finally {
                setLoading(false);
            }
        },
        []
    );

    return {
        loading,
        deleteUser,
        updateUser,
    };
};