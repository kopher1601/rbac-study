import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import type { Me, UserSummary } from '@/types'
import { api } from '@/lib/api'

interface AuthState {
  me: Me | null
  users: UserSummary[]
  loading: boolean
  switchUser: (username: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [me, setMe] = useState<Me | null>(null)
  const [users, setUsers] = useState<UserSummary[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([api.me(), api.users()])
      .then(([m, u]) => {
        setMe(m)
        setUsers(u)
      })
      .finally(() => setLoading(false))
  }, [])

  const switchUser = useCallback(async (username: string) => {
    await api.login(username)
    setMe(await api.me())
  }, [])

  const logout = useCallback(async () => {
    await api.logout()
    setMe(null)
  }, [])

  return (
    <AuthContext.Provider value={{ me, users, loading, switchUser, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
