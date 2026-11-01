const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    credentials: "include",
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
  });

  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(body.detail ?? body.message ?? "Request failed. Please try again.");
  }
  return body;
}

async function post(path, payload) {
  const { token } = await request("/api/auth/csrf");
  return request(path, {
    method: "POST",
    headers: { "X-XSRF-TOKEN": token },
    ...(payload ? { body: JSON.stringify(payload) } : {}),
  });
}

export const authApi = {
  currentUser: () => request("/api/auth/me"),
  register: (payload) => post("/api/auth/register", payload),
  login: (payload) => post("/api/auth/login", payload),
  logout: () => post("/api/auth/logout"),
};

export const stockApi = {
  list: () => request("/api/stock"),
};
