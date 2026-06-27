import { NavLink } from 'react-router-dom'
import type { ChangeEvent } from 'react'
import { useAuth } from '@/auth'
import { Badge, Button } from '@/components/ui'
import { cn } from '@/lib/cn'

const NAV = [
  { to: '/', label: '대시보드', end: true },
  { to: '/rbac', label: 'RBAC 데모', end: false },
  { to: '/roles', label: '역할/권한', end: false },
]

export function TopBar() {
  const { me, users, switchUser, logout } = useAuth()

  function onSelect(e: ChangeEvent<HTMLSelectElement>) {
    const username = e.target.value
    if (username) void switchUser(username)
  }

  return (
    <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 backdrop-blur dark:border-slate-700 dark:bg-slate-950/90">
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-6 gap-y-3 px-5 py-3">
        <div className="text-base font-semibold text-slate-900 dark:text-slate-100">
          인가 모델 스터디 <span className="text-slate-400">/ RBAC</span>
        </div>

        <nav className="flex gap-1">
          {NAV.map((n) => (
            <NavLink
              key={n.to}
              to={n.to}
              end={n.end}
              className={({ isActive }) =>
                cn(
                  'rounded-md px-3 py-1.5 text-sm font-medium',
                  isActive
                    ? 'bg-slate-900 text-white dark:bg-slate-100 dark:text-slate-900'
                    : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800',
                )
              }
            >
              {n.label}
            </NavLink>
          ))}
        </nav>

        <div className="ml-auto flex items-center gap-3">
          <label className="flex items-center gap-2 text-sm text-slate-500">
            유저 전환
            <select
              value={me?.username ?? ''}
              onChange={onSelect}
              className="rounded-md border border-slate-300 bg-white px-2 py-1 text-sm text-slate-800 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
            >
              <option value="" disabled>
                선택…
              </option>
              {users.map((u) => (
                <option key={u.username} value={u.username}>
                  {u.username} ({u.roles.join(', ')})
                </option>
              ))}
            </select>
          </label>

          {me ? (
            <>
              <span className="flex items-center gap-1 text-sm">
                <span className="font-medium text-slate-800 dark:text-slate-100">{me.username}</span>
                {me.roles.map((r) => (
                  <Badge key={r} tone="blue">
                    {r}
                  </Badge>
                ))}
              </span>
              <Button variant="outline" onClick={() => void logout()}>
                로그아웃
              </Button>
            </>
          ) : (
            <span className="text-sm text-slate-400">미로그인</span>
          )}
        </div>
      </div>
    </header>
  )
}
