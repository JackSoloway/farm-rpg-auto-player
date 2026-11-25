import { useCallback, useEffect, useState } from 'react'
import type { Session } from '../session.ts'
import RefreshItemsButton from './RefreshItemsButton.tsx'

export default function GameAccess({ session, onLogin }: { session: Session; onLogin: () => void }) {
  const [state, setState] = useState<'checking' | 'connected' | 'disconnected' | 'error'>('checking')
  const [username, setUsername] = useState('')
  const invalidate = useCallback(() => setState('disconnected'), [])

  useEffect(() => {
    let disposed = false
    let timer: ReturnType<typeof setTimeout> | undefined
    let request: AbortController | undefined
    async function check() {
      clearTimeout(timer)
      request?.abort()
      const controller = new AbortController()
      request = controller
      try {
        const response = await fetch('/gameAuth/status', {
          headers: { Authorization: `Bearer ${session.token}` }, cache: 'no-store', signal: controller.signal,
        })
        if (!response.ok) throw new Error('Session check failed')
        const body: unknown = await response.json()
        if (typeof body !== 'object' || body === null || !('authenticated' in body)
            || typeof body.authenticated !== 'boolean') throw new Error('Invalid session status')
        if (disposed || controller.signal.aborted) return
        setState(body.authenticated ? 'connected' : 'disconnected')
        setUsername('gameUsername' in body && typeof body.gameUsername === 'string' ? body.gameUsername : '')
      } catch {
        if (disposed || controller.signal.aborted) return
        setState('error')
      } finally {
        if (!disposed && !controller.signal.aborted && !document.hidden) timer = setTimeout(() => void check(), 30000)
      }
    }
    function visibility() {
      clearTimeout(timer)
      request?.abort()
      if (!document.hidden) {
        setState('checking')
        void check()
      }
    }
    if (!document.hidden) void check()
    document.addEventListener('visibilitychange', visibility)
    return () => {
      disposed = true
      clearTimeout(timer)
      request?.abort()
      document.removeEventListener('visibilitychange', visibility)
    }
  }, [session.token])

  const admin = session.role === 'ADMIN' || session.role === 'ROLE_ADMIN'
  return <div aria-live="polite">
    {state === 'checking' ? <p>Проверяем вход в игру…</p> : null}
    {state === 'error' ? <p role="alert">Не удалось проверить вход в игру. Повторим проверку автоматически.</p> : null}
    {state === 'connected' ? <>
      <p>Вход в игру подтверждён: {username}</p>
      {admin ? <RefreshItemsButton token={session.token} onGameSessionLost={invalidate} /> : null}
    </> : null}
    {state === 'disconnected' || state === 'error' ? <p className="register-switch">
      <button type="button" onClick={onLogin}>Войти в игру</button>
    </p> : null}
  </div>
}
