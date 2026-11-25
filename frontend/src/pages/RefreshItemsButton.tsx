import { useEffect, useState } from 'react'

type ButtonState = { status: 'LOCKED' | 'UNLOCKED'; delay?: number }

function readState(value: unknown): ButtonState {
  if (typeof value !== 'object' || value === null) throw new Error('Некорректный ответ сервера.')
  const state = value as Record<string, unknown>
  if (state.status !== 'LOCKED' && state.status !== 'UNLOCKED') throw new Error('Некорректное состояние кнопки.')
  if (state.delay !== undefined && (typeof state.delay !== 'number' || !Number.isFinite(state.delay))) {
    throw new Error('Некорректная задержка опроса.')
  }
  return state as ButtonState
}

export default function RefreshItemsButton({ token, onGameSessionLost }: { token: string; onGameSessionLost: () => void }) {
  const [locked, setLocked] = useState(true)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    if (pending) return
    let disposed = false
    let timer: ReturnType<typeof setTimeout> | undefined
    let request: AbortController | undefined

    function schedule(delay?: number) {
      clearTimeout(timer)
      if (!disposed && !document.hidden && delay !== undefined && delay > 0) {
        timer = setTimeout(() => void poll(), delay * 1000)
      }
    }

    async function poll() {
      clearTimeout(timer)
      request?.abort()
      const controller = new AbortController()
      request = controller
      try {
        const response = await fetch('/api/items/refresh/status', {
          headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
          cache: 'no-store',
          signal: controller.signal,
        })
        if (controller.signal.aborted || disposed) return
        if (response.status === 428) {
          onGameSessionLost()
          return
        }
        if (!response.ok) throw new Error('Не удалось проверить состояние. Проверьте подключение и вход в приложение.')
        const state = readState(await response.json())
        if (disposed || controller.signal.aborted) return
        setLocked(state.status === 'LOCKED')
        setError(null)
        schedule(state.delay)
      } catch (cause) {
        if (disposed || controller.signal.aborted) return
        setLocked(true)
        setError(cause instanceof Error ? cause.message : 'Ошибка связи с сервером.')
        schedule(10)
      }
    }

    function visibilityChanged() {
      clearTimeout(timer)
      request?.abort()
      if (!document.hidden) {
        setLocked(true)
        void poll()
      }
    }

    setLocked(true)
    if (!document.hidden) void poll()
    document.addEventListener('visibilitychange', visibilityChanged)
    return () => {
      disposed = true
      clearTimeout(timer)
      request?.abort()
      document.removeEventListener('visibilitychange', visibilityChanged)
    }
  }, [token, pending, onGameSessionLost])

  async function refresh() {
    setPending(true)
    setLocked(true)
    setMessage(null)
    setError(null)
    try {
      const response = await fetch('/api/items/refresh', {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
      })
      if (response.status === 428) {
        onGameSessionLost()
        return
      }
      if (response.status === 409) throw new Error('Обновление БД уже запущено в другой вкладке.')
      if (!response.ok) throw new Error('Не удалось обновить базу предметов. Попробуйте ещё раз.')
      const result = await response.json() as { found: number; added: number; updated: number }
      setMessage(`База обновлена. Предметов: ${result.found}, добавлено: ${result.added}, обновлено: ${result.updated}. Загрузка недостающих иконок запущена в фоне.`)
    } catch (cause) {
      setMessage(cause instanceof Error ? cause.message : 'Ошибка обновления базы предметов.')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="items-refresh">
      <button type="button" className="register-button" disabled={locked || pending} onClick={() => void refresh()}>
        {pending ? 'Обновляем базу предметов…' : 'Refresh items DB'}
      </button>
      {error ? <p role="alert">{error}</p> : null}
      {message ? <p role="status">{message}</p> : null}
    </div>
  )
}
