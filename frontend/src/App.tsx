import { useState } from 'react'
import LoginPage from './pages/LoginPage.tsx'
import RegisterPage from './pages/RegisterPage.tsx'

type Screen = 'login' | 'register'

export default function App() {
  const [screen, setScreen] = useState<Screen>('login')

  if (screen === 'register') {
    return <RegisterPage onLogin={() => setScreen('login')} />
  }

  return <LoginPage onRegister={() => setScreen('register')} />
}
