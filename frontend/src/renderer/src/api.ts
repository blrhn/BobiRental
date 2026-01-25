// src/api.ts
export const API_BASE = "http://localhost:8080";

export const authFetch = async (
  url: string,
  options: RequestInit = {},
  token?: string
) => {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Basic ${token}` } : {}),
      ...options.headers,
    },
  });
  return res;
};
