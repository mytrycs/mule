import * as https from "node:https";
import { randomBytes, createHash } from "node:crypto";

export interface Credential {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  session_id: string;
  account_id: string;
  session_expires_at: number;
  received_at: number;
}
export interface Organization {
  id: string;
  name: string;
  role: "owner" | "editor" | "viewer";
}
export interface Account {
  id: string;
  session_id: string;
  organizations: Organization[];
}
export interface Specification {
  id: string;
  name: string;
  version: string;
  format: "openapi" | "raml";
  sha256: string;
  organization_id: string;
  workspace_id: string;
  workspace_name: string;
  maven: {
    groupId: string;
    artifactId: string;
    version: string;
    extension: "yaml" | "raml";
    documentPath: string;
    coordinate: string;
  };
}
export interface MavenCredential {
  repository_id: string;
  repository_url: string;
  username: string;
  password: string;
  expires_at: number;
  permission: "read-only";
}
export interface Device {
  device_code: string;
  user_code: string;
  verification_uri: string;
  expires_in: number;
  interval: number;
}
export interface Result<T> {
  status: number;
  data: T;
}
export interface Transport {
  request<T>(
    path: string,
    body?: unknown,
    accessToken?: string,
  ): Promise<Result<T>>;
}
export function platformOrigin(value: string): string {
  const u = new URL(value);
  if (u.protocol !== "https:" || u.origin !== value || u.username || u.password)
    throw new Error(
      "Use the exact trusted HTTPS origin of your ZERNOVA Platform.",
    );
  return u.origin;
}
export class PlatformTransport implements Transport {
  readonly origin: string;
  constructor(
    origin: string,
    private readonly ca?: string,
  ) {
    this.origin = platformOrigin(origin);
  }
  request<T>(
    path: string,
    body?: unknown,
    accessToken?: string,
  ): Promise<Result<T>> {
    if (
      !/^\/(?:studio|maven)\//.test(path) ||
      path.includes("..") ||
      path.includes("://")
    )
      throw new Error("Invalid Platform API path.");
    const payload =
      body === undefined ? undefined : Buffer.from(JSON.stringify(body));
    return new Promise((resolve, reject) => {
      const req = https.request(
        this.origin + path,
        {
          method: payload ? "POST" : "GET",
          ca: this.ca,
          timeout: 8000,
          headers: {
            Accept: "application/json",
            ...(payload
              ? {
                  "Content-Type": "application/json",
                  "Content-Length": String(payload.length),
                }
              : {}),
            ...(accessToken ? { Authorization: "Bearer " + accessToken } : {}),
          },
        },
        (res) => {
          const chunks: Buffer[] = [];
          let bytes = 0;
          res.on("data", (chunk: Buffer) => {
            bytes += chunk.length;
            if (bytes > 1_000_000) {
              req.destroy(
                new Error("Platform response exceeds its size limit."),
              );
              return;
            }
            chunks.push(chunk);
          });
          res.on("end", () => {
            try {
              if (
                (res.statusCode || 500) >= 300 &&
                (res.statusCode || 500) < 400
              )
                throw new Error("Platform redirects are refused.");
              const data = JSON.parse(Buffer.concat(chunks).toString("utf8"));
              resolve({ status: res.statusCode || 500, data });
            } catch {
              reject(new Error("Platform returned an invalid response."));
            }
          });
          res.on("error", () =>
            reject(new Error("Platform response could not be read.")),
          );
        },
      );
      req.on("timeout", () =>
        req.destroy(new Error("Platform request timed out.")),
      );
      req.on("error", () =>
        reject(
          new Error(
            "Platform request failed. Check the HTTPS certificate and connection.",
          ),
        ),
      );
      if (payload) req.write(payload);
      req.end();
    });
  }
}
export function pkce() {
  const verifier = randomBytes(32).toString("base64url");
  return {
    verifier,
    challenge: createHash("sha256").update(verifier).digest("base64url"),
  };
}
export class PlatformClient {
  private refreshing: Promise<Credential> | undefined;
  constructor(
    readonly origin: string,
    private readonly transport: Transport,
    private readonly load: () => Promise<Credential | undefined>,
    private readonly save: (value: Credential | undefined) => Promise<void>,
  ) {
    platformOrigin(origin);
  }
  async begin(): Promise<Device & { verifier: string }> {
    const { verifier, challenge } = pkce();
    const result = await this.transport.request<Device>("/studio/device", {
      client_id: "zernova-studio",
      code_challenge: challenge,
    });
    if (
      result.status !== 200 ||
      !/^[A-F0-9]{10}$/.test(result.data.user_code) ||
      !/^[A-Za-z0-9_-]{43}$/.test(result.data.device_code) ||
      result.data.verification_uri !== this.origin + "/#studio-connect"
    )
      throw new Error("Platform could not start a trusted Studio connection.");
    return { ...result.data, verifier };
  }
  async poll(
    device: Device & { verifier: string },
  ): Promise<"pending" | "connected"> {
    const result = await this.transport.request<
      Credential & { error?: string }
    >("/studio/token", {
      device_code: device.device_code,
      code_verifier: device.verifier,
    });
    if (
      (result.status === 400 &&
        result.data.error === "authorization_pending") ||
      (result.status === 429 && result.data.error === "slow_down")
    )
      return "pending";
    if (result.status !== 200)
      throw new Error(
        "Studio connection expired, was denied or failed verification. Start again.",
      );
    await this.accept(result.data);
    return "connected";
  }
  private async accept(value: Credential) {
    if (
      !/^[A-Za-z0-9_-]{43}$/.test(value.access_token) ||
      !/^[A-Za-z0-9_-]{43}$/.test(value.refresh_token) ||
      !value.account_id ||
      !value.session_id ||
      value.session_expires_at <= Date.now() ||
      value.expires_in < 1 ||
      value.expires_in > 600
    )
      throw new Error("Platform returned an invalid scoped Studio credential.");
    await this.save({ ...value, received_at: Date.now() });
  }
  async credential(): Promise<Credential> {
    const current = await this.load();
    if (!current) throw new Error("Sign in to ZERNOVA Platform first.");
    if (current.session_expires_at <= Date.now()) {
      await this.save(undefined);
      throw new Error("Studio session expired. Sign in again.");
    }
    if (current.received_at + current.expires_in * 1000 > Date.now() + 30_000)
      return current;
    if (!this.refreshing)
      this.refreshing = (async () => {
        const result = await this.transport.request<Credential>(
          "/studio/refresh",
          { refresh_token: current.refresh_token },
        );
        if (result.status !== 200) {
          await this.save(undefined);
          throw new Error(
            "Studio session could not be renewed. Sign in again.",
          );
        }
        await this.accept(result.data);
        return (await this.load())!;
      })().finally(() => {
        this.refreshing = undefined;
      });
    return this.refreshing;
  }
  private async get<T>(path: string, body?: unknown): Promise<T> {
    const c = await this.credential();
    const r = await this.transport.request<T>(path, body, c.access_token);
    if (r.status === 401) {
      await this.save(undefined);
      throw new Error("Studio access expired or was revoked. Sign in again.");
    }
    if (r.status !== 200)
      throw new Error(
        `Platform denied this operation (${r.status}). Check organization access and subscription coverage.`,
      );
    return r.data;
  }
  account() {
    return this.get<Account>("/studio/api/account");
  }
  workspaces(org: string) {
    return this.get<{ id: string; name: string }[]>(
      `/studio/api/organizations/${encodeURIComponent(org)}/workspaces`,
    );
  }
  exchange(org: string) {
    return this.get<Specification[]>(
      `/studio/api/organizations/${encodeURIComponent(org)}/exchange`,
    );
  }
  async maven(org: string): Promise<MavenCredential> {
    const c = await this.get<MavenCredential>(
      `/studio/api/organizations/${encodeURIComponent(org)}/maven-session`,
      {},
    );
    if (
      c.repository_url !== this.origin + "/maven/" + org ||
      c.repository_id !== "zernova-exchange" ||
      c.username !== "zernova" ||
      !/^[A-Za-z0-9_-]{43}$/.test(c.password) ||
      c.permission !== "read-only" ||
      c.expires_at <= Date.now() ||
      c.expires_at > Date.now() + 8 * 60_000 + 1000
    )
      throw new Error("Invalid organization-scoped Maven credential.");
    return c;
  }
  async logout() {
    const c = await this.load();
    if (c) {
      try {
        const r = await this.transport.request(
          "/studio/logout",
          {},
          c.access_token,
        );
        if (r.status !== 200 && r.status !== 401)
          throw new Error("Studio revocation was not confirmed.");
      } finally {
        await this.save(undefined);
      }
    }
  }
}
