import {z} from "zod";

const BOOLEAN = z.union([z.string(), z.boolean()]).optional().default(false)

export default BOOLEAN;