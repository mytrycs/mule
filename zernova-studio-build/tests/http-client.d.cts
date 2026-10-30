export function freshFetch(url: string, options?: {
  method?: string; headers?: Record<string, string>; body?: string; signal?: AbortSignal;
}): Promise<{ status: number; text(): Promise<string> }>;
