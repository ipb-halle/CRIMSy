import { dummyData } from "../../../data/dummyData";
import { FilterGroup } from "./Filters";


export const filterConfig: FilterGroup[] = [
    {
        key: "materialTypeIds",
        label: "Material Types",
        options: dummyData.materialTypes.map(m => ({
            value: m.id,
            label: m.name,
        })),
    },
    {
        key: "projectIds",
        label: "Projects",
        options: dummyData.projects.map(p => ({
            value: p.id,
            label: p.name,
        })),
    },
    {
        key: "ownerIds",
        label: "Owner",
        options: dummyData.usersGroups.map(u => ({
            value: u.id,
            label: u.name,
        })),
    },
    {
        key: "storageClassIds",
        label: "Storage Class",
        options: dummyData.storageClasses.map(s => ({
            value: s.id,
            label: s.name,
        })),
    },
    {
        key: "hazardIds",
        label: "Hazards",
        options: dummyData.hazards.map(h => ({
            value: h.id,
            label: h.name,
        })),
    },
    {
        key: "containerIds",
        label: "Containers",
        options: dummyData.containers.map(c => ({
            value: c.id,
            label: c.label,
        })),
    },
    {
        key: "serverIds",
        label: "Servers / Nodes",
        options: dummyData.servers.map(s => ({
            value: s.id,
            label: s.name,
        })),
    },
    {
        key: "deactivated",
        label: "Status",
        options: [
            { value: true, label: "Deactivated" },
            { value: false, label: "Active" },
        ],
    },
];
