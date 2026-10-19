// Local development only: build the schema of an EMPTY database straight from the entities and mark
// every migration as applied. Needed because the migration chain cannot run on a fresh database yet
// (AddGreenScore1775865413942 drops a constraint that earlier migrations never create).
// Usage: npx tsx scripts/dev-db-bootstrap.ts
import fs from "fs";
import path from "path";
import AppDataSource from "../src/infrastructure/database";

async function main() {
    await AppDataSource.initialize();
    await AppDataSource.query(`CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`);
    await AppDataSource.synchronize();

    await AppDataSource.query(
        `CREATE TABLE IF NOT EXISTS "migrations" ("id" SERIAL PRIMARY KEY, "timestamp" bigint NOT NULL, "name" varchar NOT NULL)`
    );
    const dir = path.resolve(__dirname, "../src/migrations");
    let marked = 0;
    for (const file of fs.readdirSync(dir).filter((f) => f.endsWith(".ts"))) {
        const match = fs.readFileSync(path.join(dir, file), "utf8").match(/class\s+(\w+?)(\d{13})\s+implements/);
        if (!match) continue;
        const name = match[1] + match[2];
        const exists = await AppDataSource.query(`SELECT 1 FROM "migrations" WHERE "name" = $1`, [name]);
        if (!exists.length) {
            await AppDataSource.query(`INSERT INTO "migrations" ("timestamp", "name") VALUES ($1, $2)`, [match[2], name]);
            marked++;
        }
    }
    console.log(`Schema synchronized; ${marked} migrations marked as applied.`);
    await AppDataSource.destroy();
}

main().catch((err) => {
    console.error(err);
    process.exit(1);
});
