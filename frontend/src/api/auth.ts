import type { Session } from '../session.ts'

export type RegisterResult = {
  username: string
  role: string
}

export class RegisterError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'RegisterError'
    this.status = status
  }
}

export async function registerAccount(username: string, password: string): Promise<RegisterResult> {
  let response: Response
  try {
    response = await fetch('/auth/register', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    })
  } catch {
    throw new RegisterError(0, 'Сервер недоступен. Запустите бэкенд на порту 8080.')
  }

  if (response.ok) {
    return response.json() as Promise<RegisterResult>
  }

  throw new RegisterError(response.status, await readError(response, 'Не удалось создать аккаунт.'))
}

export class LoginError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'LoginError'
    this.status = status
  }
}

export async function loginAccount(username: string, password: string): Promise<Session> {
  let response: Response
  try {
    response = await fetch('/auth/login', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    })
  } catch {
    throw new LoginError(0, 'Сервер недоступен. Запустите бэкенд на порту 8080.')
  }

  if (response.status === 401) {
    throw new LoginError(401, 'Неверное имя или пароль.')
  }

  if (!response.ok) {
    throw new LoginError(response.status, await readError(response, 'Не удалось войти.'))
  }

  return readSession(await response.json())
}

export async function loginGameAccount(username: string, password: string, token: string): Promise<void> {
  let response: Response
  try {
    response = await fetch('/gameAuth/login', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    })
  } catch {
    throw new LoginError(0, 'Сервер недоступен. Запустите бэкенд на порту 8080.')
  }

  const fallback = 'Не удалось войти в игру.'
  if (response.status === 403) {
    throw new LoginError(403, 'Войдите в приложение снова.')
  }
  if (!response.ok) {
    const message = response.status >= 500 ? fallback : await readError(response, fallback)
    throw new LoginError(response.status, message)
  }
  if (!response.headers.get('Content-Type')?.includes('application/json')) {
    throw new LoginError(response.status, fallback)
  }
}

function readSession(body: unknown): Session {
  if (typeof body !== 'object' || body === null) {
    throw new LoginError(0, 'Не удалось прочитать ответ сервера.')
  }

  const record = body as Record<string, unknown>
  const token = record.token
  const username = record.username
  const role = record.Role ?? record.role
  if (typeof token !== 'string' || token.length === 0 || typeof username !== 'string' || typeof role !== 'string') {
    throw new LoginError(0, 'Не удалось прочитать ответ сервера.')
  }

  return { token, username, role }
}

async function readError(response: Response, fallback: string): Promise<string> {
  if (response.status === 409) {
    return 'Это имя уже занято.'
  }

  try {
    const body: unknown = await response.json()
    if (isApiError(body) && body.error.length > 0) {
      return body.error
    }
  } catch {
    // The body is not the JSON shape from AuthExceptionHandler.
  }

  if (response.status >= 500) {
    return 'Сервер недоступен. Запустите бэкенд на порту 8080.'
  }

  return fallback
}

function isApiError(value: unknown): value is { error: string } {
  return (
    typeof value === 'object' &&
    value !== null &&
    'error' in value &&
    typeof value.error === 'string'
  )
}
