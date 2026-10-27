import { useEffect, useState, type FormEvent } from 'react'
import { LoginError, loginAccount } from '../api/auth.ts'
import { roleLabel } from '../roleLabel.ts'
import { clearSession, loadSession, saveSession, type Session } from '../session.ts'
import Sprout from './Sprout.tsx'
import './RegisterPage.css'

type FieldErrors = {
  username?: string
  password?: string
}

type LoginPageProps = {
  onRegister: () => void
}

export default function LoginPage({ onRegister }: LoginPageProps) {
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)
  const [session, setSession] = useState<Session | null>(() => loadSession())

  useEffect(() => {
    document.title = 'Farm RPG — вход'
  }, [])

  function validate(name: string, password: string): FieldErrors {
    const errors: FieldErrors = {}
    if (name.trim().length === 0) errors.username = 'Введите имя.'
    if (password.trim().length === 0) errors.password = 'Введите пароль.'
    return errors
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const username = String(data.get('username') ?? '')
    const password = String(data.get('password') ?? '')
    const errors = validate(username, password)
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) {
      setFormError(Object.values(errors).join(' '))
      const firstInvalid = (['username', 'password'] as const).find((field) => errors[field])
      const field = firstInvalid ? event.currentTarget.elements.namedItem(firstInvalid) : null
      if (field instanceof HTMLInputElement) field.focus()
      return
    }

    setFormError(null)
    setPending(true)
    try {
      const nextSession = await loginAccount(username.trim(), password)
      saveSession(nextSession)
      setSession(nextSession)
    } catch (error) {
      setFormError(error instanceof LoginError ? error.message : 'Не удалось войти.')
    } finally {
      setPending(false)
    }
  }

  function logout() {
    clearSession()
    setSession(null)
    setFieldErrors({})
    setFormError(null)
  }

  return (
    <main className="register">
      <section className="register-card" aria-labelledby="login-title">
        <Sprout />
        <p className="register-kicker">Farm RPG</p>
        <h1 id="login-title">Вход</h1>

        {session ? (
          <div className="register-success">
            <h2>Вы вошли</h2>
            <dl>
              <div>
                <dt>Имя</dt>
                <dd>{session.username}</dd>
              </div>
              <div>
                <dt>Роль</dt>
                <dd>{roleLabel(session.role)}</dd>
              </div>
            </dl>
            <button type="button" className="register-button" onClick={logout}>
              Выйти
            </button>
          </div>
        ) : (
          <form className="register-form" onSubmit={onSubmit} noValidate>
            {formError ? (
              <p className="register-banner" role="alert">
                {formError}
              </p>
            ) : null}

            <label className="register-field">
              <span>Имя</span>
              <input
                name="username"
                autoComplete="username"
                spellCheck={false}
                aria-invalid={fieldErrors.username ? true : undefined}
                aria-describedby={fieldErrors.username ? 'login-username-error' : undefined}
              />
              {fieldErrors.username ? <small id="login-username-error">{fieldErrors.username}</small> : null}
            </label>

            <label className="register-field">
              <span>Пароль</span>
              <input
                name="password"
                type="password"
                autoComplete="current-password"
                aria-invalid={fieldErrors.password ? true : undefined}
                aria-describedby={fieldErrors.password ? 'login-password-error' : undefined}
              />
              {fieldErrors.password ? <small id="login-password-error">{fieldErrors.password}</small> : null}
            </label>

            <button type="submit" className="register-button" disabled={pending}>
              {pending ? 'Входим…' : 'Войти'}
            </button>
            <p className="register-switch">
              Нет аккаунта?{' '}
              <button type="button" onClick={onRegister}>
                Зарегистрироваться
              </button>
            </p>
          </form>
        )}
      </section>
    </main>
  )
}
