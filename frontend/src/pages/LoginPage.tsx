import { useEffect, useState, type FormEvent } from 'react'
import { LoginError, loginAccount, loginGameAccount } from '../api/auth.ts'
import { roleLabel } from '../roleLabel.ts'
import { clearSession, loadSession, saveSession, type Session } from '../session.ts'
import Sprout from './Sprout.tsx'
import GameAccess from './GameAccess.tsx'
import './RegisterPage.css'

type FieldErrors = {
  username?: string
  password?: string
}

type LoginPageProps =
  | { onRegister: () => void; onGameAuth: () => void }
  | { gameToken: string; onBack: () => void }

export default function LoginPage(props: LoginPageProps) {
  const game = 'gameToken' in props
  const title = game ? 'Вход в игру' : 'Вход'
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)
  const [session, setSession] = useState<Session | null>(() => game ? null : loadSession())
  const [gameUsername, setGameUsername] = useState<string | null>(null)

  useEffect(() => {
    document.title = `Farm RPG — ${title.toLowerCase()}`
  }, [title])

  function validate(name: string, password: string): FieldErrors {
    const errors: FieldErrors = {}
    if (name.trim().length === 0) errors.username = 'Введите имя.'
    if (password.trim().length === 0) errors.password = 'Введите пароль.'
    return errors
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const username = String(data.get('username') ?? '').trim()
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
      if (game) {
        await loginGameAccount(username, password, props.gameToken)
        setGameUsername(username)
      } else {
        const nextSession = await loginAccount(username, password)
        saveSession(nextSession)
        setSession(nextSession)
      }
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
        <h1 id="login-title">{title}</h1>

        {session || gameUsername ? (
          <div className="register-success">
            <h2>Вы вошли</h2>
            <dl>
              <div>
                <dt>Имя</dt>
                <dd>{session?.username ?? gameUsername}</dd>
              </div>
              {session ? (
                <div>
                  <dt>Роль</dt>
                  <dd>{roleLabel(session.role)}</dd>
                </div>
              ) : null}
            </dl>
            {session && !game ? (
              <GameAccess session={session} onLogin={props.onGameAuth} />
            ) : null}
            <button type="button" className="register-button" onClick={game ? props.onBack : logout}>
              {game ? 'Назад' : 'Выйти'}
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
              {game ? null : 'Нет аккаунта? '}
              <button type="button" onClick={game ? props.onBack : props.onRegister}>
                {game ? 'Назад' : 'Зарегистрироваться'}
              </button>
            </p>
          </form>
        )}
      </section>
    </main>
  )
}
