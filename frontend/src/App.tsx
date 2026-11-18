import { useState } from 'react'
import LoginPage from './pages/LoginPage.tsx'
import RegisterPage from './pages/RegisterPage.tsx'
import { loadSession } from './session.ts'

type Screen = 'login' | 'register' | 'gameAuth'

export default function App() {
  const [screen, setScreen] = useState<Screen>('login')

  if (screen === 'register') {
    return <RegisterPage onLogin={() => setScreen('login')} />
  }

  const session = screen === 'gameAuth' ? loadSession() : null
  if (session) {
    return <LoginPage key="gameAuth" gameToken={session.token} onBack={() => setScreen('login')} />
  }

  return (
    <LoginPage
      key="login"
      onRegister={() => setScreen('register')}
      onGameAuth={() => setScreen('gameAuth')}
    />
  )
}
