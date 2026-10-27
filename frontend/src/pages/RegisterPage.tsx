import { useEffect, useState, type FormEvent } from 'react'
import { RegisterError, registerAccount, type RegisterResult } from '../api/auth.ts'
import { roleLabel } from '../roleLabel.ts'
import Sprout from './Sprout.tsx'
import './RegisterPage.css'

type RegisterPageProps = {
  onLogin: () => void
}

const USERNAME_MIN = 3
const USERNAME_MAX = 32
const PASSWORD_MIN = 8
const PASSWORD_MAX = 72

type FieldErrors = {
  username?: string
  password?: string
  confirmPassword?: string
}

export default function RegisterPage({ onLogin }: RegisterPageProps) {
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)
  const [account, setAccount] = useState<RegisterResult | null>(null)

  useEffect(() => {
    document.title = 'Farm RPG — регистрация'
  }, [])

  function validate(name: string, nextPassword: string, nextConfirm: string): FieldErrors {
    const errors: FieldErrors = {}
    const trimmedName = name.trim()

    if (trimmedName.length < USERNAME_MIN || trimmedName.length > USERNAME_MAX) {
      errors.username = `Имя — от ${USERNAME_MIN} до ${USERNAME_MAX} символов.`
    }
    if (nextPassword.trim().length === 0 || nextPassword.length < PASSWORD_MIN || nextPassword.length > PASSWORD_MAX) {
      errors.password = `Пароль — от ${PASSWORD_MIN} до ${PASSWORD_MAX} символов.`
    }
    if (nextConfirm.length === 0) {
      errors.confirmPassword = 'Повторите пароль.'
    } else if (nextPassword !== nextConfirm) {
      errors.confirmPassword = 'Пароли не совпадают.'
    }

    return errors
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const nextUsername = String(data.get('username') ?? '')
    const nextPassword = String(data.get('password') ?? '')
    const nextConfirm = String(data.get('confirmPassword') ?? '')
    const errors = validate(nextUsername, nextPassword, nextConfirm)
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) {
      setFormError(Object.values(errors).join(' '))
      const firstInvalid = (['username', 'password', 'confirmPassword'] as const).find((field) => errors[field])
      const field = firstInvalid ? event.currentTarget.elements.namedItem(firstInvalid) : null
      if (field instanceof HTMLInputElement) field.focus()
      return
    }

    setFormError(null)
    setPending(true)
    try {
      const created = await registerAccount(nextUsername.trim(), nextPassword)
      setAccount(created)
    } catch (error) {
      setFormError(error instanceof RegisterError ? error.message : 'Не удалось создать аккаунт.')
    } finally {
      setPending(false)
    }
  }

  function registerAnother() {
    setFieldErrors({})
    setFormError(null)
    setAccount(null)
  }

  return (
    <main className="register">
      <section className="register-card" aria-labelledby="register-title">
        <Sprout />
        <p className="register-kicker">Farm RPG</p>
        <h1 id="register-title">Регистрация</h1>

        {account ? (
          <div className="register-success">
            <h2>Аккаунт создан</h2>
            <dl>
              <div>
                <dt>Имя</dt>
                <dd>{account.username}</dd>
              </div>
              <div>
                <dt>Роль</dt>
                <dd>{roleLabel(account.role)}</dd>
              </div>
            </dl>
            <button type="button" className="register-button" onClick={onLogin}>
              Войти
            </button>
            <button type="button" className="register-button-secondary" onClick={registerAnother}>
              Создать ещё один
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
                maxLength={USERNAME_MAX}
                aria-invalid={fieldErrors.username ? true : undefined}
                aria-describedby={fieldErrors.username ? 'username-error' : undefined}
              />
              {fieldErrors.username ? (
                <small id="username-error">{fieldErrors.username}</small>
              ) : (
                <small className="register-hint">От {USERNAME_MIN} до {USERNAME_MAX} символов</small>
              )}
            </label>

            <label className="register-field">
              <span>Пароль</span>
              <input
                name="password"
                type="password"
                autoComplete="new-password"
                maxLength={PASSWORD_MAX}
                aria-invalid={fieldErrors.password ? true : undefined}
                aria-describedby={fieldErrors.password ? 'password-error' : undefined}
              />
              {fieldErrors.password ? (
                <small id="password-error">{fieldErrors.password}</small>
              ) : (
                <small className="register-hint">От {PASSWORD_MIN} до {PASSWORD_MAX} символов</small>
              )}
            </label>

            <label className="register-field">
              <span>Пароль ещё раз</span>
              <input
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                maxLength={PASSWORD_MAX}
                aria-invalid={fieldErrors.confirmPassword ? true : undefined}
                aria-describedby={fieldErrors.confirmPassword ? 'confirm-error' : undefined}
              />
              {fieldErrors.confirmPassword ? <small id="confirm-error">{fieldErrors.confirmPassword}</small> : null}
            </label>

            <button type="submit" className="register-button" disabled={pending}>
              {pending ? 'Создаём аккаунт…' : 'Создать аккаунт'}
            </button>
            <p className="register-switch">
              Уже есть аккаунт?{' '}
              <button type="button" onClick={onLogin}>
                Войти
              </button>
            </p>
          </form>
        )}
      </section>
    </main>
  )
}
