let token: string | null = localStorage.getItem('token')

export function setToken(t: string | null) {
  token = t
  if (t) localStorage.setItem('token', t)
  else localStorage.removeItem('token')
}

export async function api<T = any>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const text = await res.text()
  if (!res.ok) throw new Error(text)
  return (text ? JSON.parse(text) : undefined) as T
}
