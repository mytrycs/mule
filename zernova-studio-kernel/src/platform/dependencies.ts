import { createHash } from "node:crypto";
import * as sax from "sax";
import type { Specification } from "./client";
export interface LockedSpecification {
  schemaVersion: 1;
  platformOrigin: string;
  organizationId: string;
  versionId: string;
  name: string;
  version: string;
  format: "openapi" | "raml";
  sha256: string;
  groupId: string;
  artifactId: string;
  type: "yaml" | "raml";
  resource: string;
}
export interface Dependency {
  groupId?: string;
  artifactId?: string;
  version?: string;
  classifier?: string;
  type?: string;
  scope?: string;
}
export function lockSpecification(
  origin: string,
  spec: Specification,
): LockedSpecification {
  if (
    !/^https:\/\//.test(origin) ||
    !/^[a-f0-9-]{36}$/.test(spec.organization_id) ||
    !/^[a-f0-9-]{36}$/.test(spec.id) ||
    !/^[a-f0-9]{64}$/.test(spec.sha256) ||
    spec.maven.groupId !==
      "com.zernovahq.exchange.o" + spec.organization_id.replaceAll("-", "") ||
    !/^api-[a-f0-9]{32}$/.test(spec.maven.artifactId) ||
    spec.maven.version !== spec.version ||
    !/^[0-9][A-Za-z0-9_.+-]{0,79}$/.test(spec.version) ||
    /snapshot/i.test(spec.version) ||
    !["yaml", "raml"].includes(spec.maven.extension)
  )
    throw new Error(
      "Exchange returned invalid immutable specification metadata.",
    );
  const type = spec.format === "raml" ? "raml" : "yaml";
  if (type !== spec.maven.extension)
    throw new Error("Specification format does not match Maven metadata.");
  return {
    schemaVersion: 1,
    platformOrigin: origin,
    organizationId: spec.organization_id,
    versionId: spec.id,
    name: spec.name,
    version: spec.version,
    format: spec.format,
    sha256: spec.sha256,
    groupId: spec.maven.groupId,
    artifactId: spec.maven.artifactId,
    type,
    resource: `src/main/resources/exchange/${spec.maven.artifactId}-${spec.version}.${type}`,
  };
}
export function verifiedBytes(lock: LockedSpecification, bytes: Buffer) {
  if (
    bytes.length > 128000 ||
    createHash("sha256").update(bytes).digest("hex") !== lock.sha256
  )
    throw new Error(
      "Downloaded specification does not match its approved Exchange SHA-256.",
    );
  return bytes;
}
export function parseDependencies(pom: string): Dependency[] {
  if (Buffer.byteLength(pom) > 1_000_000)
    throw new Error("Project POM exceeds its size limit.");
  const parser = sax.parser(true, { xmlns: true }),
    stack: string[] = [],
    out: Dependency[] = [];
  let dep: Record<string, string> | undefined;
  parser.ondoctype = () => {
    throw new Error("POM DOCTYPE is refused.");
  };
  parser.onopentag = (tag) => {
    stack.push(tag.local);
    if (stack.join("/") === "project/dependencies/dependency") dep = {};
  };
  parser.ontext = (text) => {
    if (dep && stack.length === 4) dep[stack[3]] = (dep[stack[3]] || "") + text;
  };
  parser.onclosetag = () => {
    if (stack.join("/") === "project/dependencies/dependency" && dep) {
      out.push(
        Object.fromEntries(Object.entries(dep).map(([k, v]) => [k, v.trim()])),
      );
      dep = undefined;
    }
    stack.pop();
  };
  parser.write(pom).close();
  return out;
}
export function addDependency(pom: string, lock: LockedSpecification): string {
  const existing = parseDependencies(pom);
  const matching = existing.filter(
    (d) => d.groupId === lock.groupId && d.artifactId === lock.artifactId,
  );
  if (matching.length) {
    if (
      matching.length === 1 &&
      matching[0].version === lock.version &&
      matching[0].type === lock.type &&
      !matching[0].classifier
    )
      return pom;
    throw new Error(
      "This API already has a different dependency. Review an explicit version update first.",
    );
  }
  // Preserve all original bytes; append only in an unambiguous standard Maven document.
  if (
    !/<project(?:\s|>)/.test(pom) ||
    /<(?:\w+:)?dependencies\b/.test(
      pom.replace(/<dependencies>[\s\S]*?<\/dependencies>/g, ""),
    )
  )
    throw new Error(
      "This POM layout requires a reviewed manual dependency edit.",
    );
  const entry = `\n    <dependency><groupId>${lock.groupId}</groupId><artifactId>${lock.artifactId}</artifactId><version>${lock.version}</version><type>${lock.type}</type></dependency>\n`;
  let result: string;
  const matches = [...pom.matchAll(/<dependencies>\s*/g)];
  if (matches.length === 1 && pom.includes("</dependencies>"))
    result = pom.replace("</dependencies>", entry + "  </dependencies>");
  else if (matches.length === 0 && pom.includes("</project>"))
    result = pom.replace(
      "</project>",
      "  <dependencies>" + entry + "  </dependencies>\n</project>",
    );
  else
    throw new Error(
      "This POM layout requires a reviewed manual dependency edit.",
    );
  const parsed = parseDependencies(result);
  if (
    parsed.length !== existing.length + 1 ||
    !parsed.some((d) => matchesLock(d, lock))
  )
    throw new Error("Dependency edit could not be validated.");
  return result;
}
export function matchesLock(d: Dependency, lock: LockedSpecification) {
  return (
    d.groupId === lock.groupId &&
    d.artifactId === lock.artifactId &&
    d.version === lock.version &&
    d.type === lock.type &&
    !d.classifier &&
    (!d.scope || d.scope === "compile")
  );
}
export function classifyDependencies(
  deps: Dependency[],
  locks: LockedSpecification[],
): { http: boolean; specifications: LockedSpecification[] } {
  let http = false;
  const specifications: LockedSpecification[] = [];
  for (const d of deps) {
    if (
      d.groupId === "com.zernova.connectors" &&
      d.artifactId === "zernova-http-connector" &&
      d.version === "1.12.2" &&
      d.classifier === "zernova-plugin"
    ) {
      http = true;
      continue;
    }
    const lock = locks.find((l) => matchesLock(d, l));
    if (!lock)
      throw new Error(
        "Dependency is neither the bundled HTTP connector nor an approved, locked Exchange specification.",
      );
    specifications.push(lock);
  }
  if (locks.some((l) => !specifications.includes(l)))
    throw new Error("Exchange lock metadata and POM dependencies disagree.");
  return { http, specifications };
}
