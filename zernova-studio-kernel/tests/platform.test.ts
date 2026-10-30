import test from "node:test";
import assert from "node:assert/strict";
import { randomBytes, createHash } from "node:crypto";
import {
  PlatformClient,
  platformOrigin,
  type Transport,
  type Credential,
  type Specification,
} from "../src/platform/client";
import {
  lockSpecification,
  addDependency,
  parseDependencies,
  classifyDependencies,
  verifiedBytes,
} from "../src/platform/dependencies";
import { mavenSettings } from "../src/platform/maven";
const token = () => randomBytes(32).toString("base64url");
const org = "11111111-1111-4111-8111-111111111111";
const id = "22222222-2222-4222-8222-222222222222";
const design = "33333333333343338333333333333333";
const origin = "https://localhost:4640";
const source = Buffer.from(
  "openapi: 3.1.0\ninfo:\n  title: Orders\n  version: 1.0.0\npaths: {}\n",
);
const spec: Specification = {
  id,
  name: "Orders",
  version: "1.0.0",
  format: "openapi",
  sha256: createHash("sha256").update(source).digest("hex"),
  organization_id: org,
  workspace_id: id,
  workspace_name: "APIs",
  maven: {
    groupId: "com.zernovahq.exchange.o" + org.replaceAll("-", ""),
    artifactId: "api-" + design,
    version: "1.0.0",
    extension: "yaml",
    documentPath: "unused",
    coordinate: "unused",
  },
};
function fixture() {
  let stored: Credential | undefined;
  let refreshes = 0;
  let unauthorized = false;
  let pending = true;
  let verifier = "";
  const requests: { path: string; body: unknown; bearer?: string }[] = [];
  const credential = () => ({
    access_token: token(),
    refresh_token: token(),
    expires_in: 600,
    session_id: id,
    account_id: org,
    session_expires_at: Date.now() + 3600_000,
    received_at: Date.now(),
  });
  const transport: Transport = {
    request: async <T>(path: string, body?: unknown, bearer?: string) => {
      requests.push({ path, body, bearer });
      let status = 200,
        data: unknown;
      if (path === "/studio/device") {
        verifier = (body as { code_challenge: string }).code_challenge;
        data = {
          device_code: token(),
          user_code: "A1B2C3D4E5",
          verification_uri: origin + "/#studio-connect",
          expires_in: 600,
          interval: 3,
        };
      } else if (path === "/studio/token") {
        assert.equal(
          createHash("sha256")
            .update((body as { code_verifier: string }).code_verifier)
            .digest("base64url"),
          verifier,
        );
        if (pending) {
          status = 400;
          data = { error: "authorization_pending" };
        } else data = credential();
      } else if (path === "/studio/refresh") {
        refreshes++;
        await new Promise((r) => setTimeout(r, 5));
        data = credential();
      } else if (path === "/studio/api/account") {
        if (unauthorized) {
          status = 401;
          data = { error: "revoked" };
        } else data = { id: org, session_id: id, organizations: [] };
      } else if (path.endsWith("/maven-session"))
        data = {
          repository_id: "zernova-exchange",
          repository_url: origin + "/maven/" + org,
          username: "zernova",
          password: token(),
          expires_at: Date.now() + 480000,
          permission: "read-only",
        };
      else if (path === "/studio/logout") data = { revoked: true };
      else throw new Error("unexpected path");
      return { status, data: data as T };
    },
  };
  const client = new PlatformClient(
    origin,
    transport,
    async () => stored,
    async (v) => {
      stored = v;
    },
  );
  return {
    client,
    requests,
    connect: async () => {
      const d = await client.begin();
      assert.equal(await client.poll(d), "pending");
      pending = false;
      assert.equal(await client.poll(d), "connected");
    },
    stored: () => stored,
    expire: () => {
      stored!.received_at = Date.now() - 600000;
    },
    refreshes: () => refreshes,
    revoke: () => {
      unauthorized = true;
    },
  };
}
test("Platform connection accepts exact HTTPS origins and rejects credentials, paths and HTTP", () => {
  assert.equal(platformOrigin(origin), origin);
  for (const value of [
    "http://localhost:4640",
    origin + "/",
    origin + "/path",
    "https://user:secret@example.invalid",
    "https://example.invalid?return=bad",
  ])
    assert.throws(() => platformOrigin(value));
});
test("Studio device connection supplies PKCE and stores only verified scoped credentials", async () => {
  const f = fixture();
  await f.connect();
  assert.ok(f.stored());
  assert.equal(f.stored()!.account_id, org);
  assert.equal((await f.client.account()).id, org);
  const created = f.requests.find((r) => r.path === "/studio/device")!;
  assert.equal(created.bearer, undefined);
  assert.equal(
    (created.body as { client_id: string }).client_id,
    "zernova-studio",
  );
});
test("concurrent calls rotate refresh credentials once and revoked access clears stored credentials", async () => {
  const f = fixture();
  await f.connect();
  f.expire();
  await Promise.all([
    f.client.account(),
    f.client.account(),
    f.client.account(),
  ]);
  assert.equal(f.refreshes(), 1);
  f.revoke();
  await assert.rejects(f.client.account(), /revoked/);
  assert.equal(f.stored(), undefined);
});
test("organization-scoped Maven grant is distinct from Studio access and logout clears the account", async () => {
  const f = fixture();
  await f.connect();
  const grant = await f.client.maven(org);
  assert.equal(grant.repository_url, origin + "/maven/" + org);
  assert.notEqual(grant.password, f.stored()!.access_token);
  await f.client.logout();
  assert.equal(f.stored(), undefined);
});
test("dependency insertion preserves original POM comments and unrelated configuration", () => {
  const lock = lockSpecification(origin, spec);
  const before =
    '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><!-- preserve me --><groupId>example.app</groupId><artifactId>app</artifactId><version>1.0.0</version><properties><custom>original</custom></properties><dependencies></dependencies></project>';
  const after = addDependency(before, lock);
  assert.ok(after.includes("<!-- preserve me -->"));
  assert.ok(
    after.includes("<properties><custom>original</custom></properties>"),
  );
  assert.equal(parseDependencies(after).length, 1);
  assert.equal(addDependency(after, lock), after);
  assert.equal(
    after.replace(/\n    <dependency>[\s\S]*?<\/dependency>\n  /, ""),
    before,
  );
});
test("unreviewed version changes, mutable versions, cross-organization coordinates and DTDs are refused", () => {
  const lock = lockSpecification(origin, spec),
    pom = addDependency(
      "<project><dependencies></dependencies></project>",
      lock,
    );
  assert.throws(
    () => addDependency(pom, { ...lock, version: "2.0.0" }),
    /different/,
  );
  assert.throws(
    () =>
      parseDependencies(
        '<!DOCTYPE project [<!ENTITY x SYSTEM "file:///etc/passwd">]><project/>',
      ),
    /DOCTYPE/,
  );
  assert.throws(() =>
    lockSpecification(origin, {
      ...spec,
      maven: { ...spec.maven, groupId: "another.organization" },
    }),
  );
  assert.throws(() =>
    lockSpecification(origin, {
      ...spec,
      version: "1.0.0-SNAPSHOT",
      maven: { ...spec.maven, version: "1.0.0-SNAPSHOT" },
    }),
  );
});
test("locked specification bytes are verified and never become executable connector metadata", () => {
  const lock = lockSpecification(origin, spec);
  assert.deepEqual(verifiedBytes(lock, source), source);
  assert.throws(() => verifiedBytes(lock, Buffer.from("changed")), /SHA-256/);
  const deps = parseDependencies(
    addDependency(
      "<project><dependencies><dependency><groupId>com.zernova.connectors</groupId><artifactId>zernova-http-connector</artifactId><version>1.12.2</version><classifier>zernova-plugin</classifier></dependency></dependencies></project>",
      lock,
    ),
  );
  const c = classifyDependencies(deps, [lock]);
  assert.equal(c.http, true);
  assert.equal(c.specifications.length, 1);
  assert.throws(() =>
    classifyDependencies(
      [
        ...deps,
        { groupId: "evil", artifactId: "code", version: "1.0.0", type: "jar" },
      ],
      [lock],
    ),
  );
  assert.throws(() => classifyDependencies(deps, []));
});
test("private Maven settings mirror every repository and contain only an environment credential reference", () => {
  const settings = mavenSettings(origin + "/maven/" + org, true);
  assert.ok(settings.includes("<mirrorOf>*</mirrorOf>"));
  assert.ok(settings.includes("${env.ZERNOVA_MAVEN_TOKEN}"));
  assert.ok(!settings.includes("publisher"));
  assert.ok(
    !mavenSettings("https://repo.maven.apache.org/maven2", false).includes(
      "<servers>",
    ),
  );
  assert.throws(() =>
    mavenSettings("https://user:secret@example.invalid/repo", true),
  );
});
