import { useEffect, useState } from 'react'
import { useAuth } from '@/auth'
import { api } from '@/lib/api'
import { Badge, Card, CardTitle, Table, Td, Th } from '@/components/ui'
import type { RolesView } from '@/types'

export function RolesPage() {
  const { me } = useAuth()
  const [view, setView] = useState<RolesView | null>(null)
  const [folderCount, setFolderCount] = useState(3)

  useEffect(() => {
    if (!me) {
      setView(null)
      return
    }
    api.roles().then(setView).catch(() => setView(null))
  }, [me])

  if (!me) {
    return (
      <Card>
        <p className="text-slate-500">로그인하면 역할-권한 매트릭스와 역할 폭발 지표가 표시됩니다.</p>
      </Card>
    )
  }
  if (!view) {
    return (
      <Card>
        <p className="text-slate-500">불러오는 중…</p>
      </Card>
    )
  }

  const { explosion } = view
  const projected = folderCount * explosion.actionTiers

  return (
    <div className="grid gap-5">
      <Card>
        <CardTitle hint={`역할 ${view.roleCount} · 권한 ${view.permissionCount}`}>
          역할 → 권한 매트릭스
        </CardTitle>
        <Table
          head={
            <>
              <Th>역할</Th>
              <Th>유형</Th>
              <Th>권한</Th>
            </>
          }
        >
          {view.roles.map((r) => (
            <tr key={r.name}>
              <Td>
                <span className="font-medium text-slate-800 dark:text-slate-100">{r.name}</span>
              </Td>
              <Td>{r.scoped ? <Badge tone="amber">스코프</Badge> : <Badge tone="blue">전역</Badge>}</Td>
              <Td>
                <div className="flex flex-wrap gap-1">
                  {r.permissions.map((p) => (
                    <Badge key={p}>{p}</Badge>
                  ))}
                </div>
              </Td>
            </tr>
          ))}
        </Table>
      </Card>

      <Card>
        <CardTitle hint="RBAC 의 구조적 한계">역할 폭발(Role Explosion) 시뮬레이터</CardTitle>
        <p className="mb-4 text-sm text-slate-500">{explosion.note}</p>
        <div className="flex items-center gap-4">
          <label className="whitespace-nowrap text-sm">
            부서/폴더 수: <b>{folderCount}</b>
          </label>
          <input
            type="range"
            min={1}
            max={30}
            value={folderCount}
            onChange={(e) => setFolderCount(Number(e.target.value))}
            className="flex-1"
          />
        </div>
        <div className="mt-4 flex flex-wrap items-center gap-3 text-sm">
          <span>필요한 스코프 역할 수 =</span>
          <Badge tone="amber">
            {folderCount} 부서 × {explosion.actionTiers} 등급 = {projected}개
          </Badge>
        </div>
        <p className="mt-3 text-xs text-slate-400">
          현재 시드: 부서 {explosion.departments} × 등급 {explosion.actionTiers} ={' '}
          {explosion.rolesNeededForFullScoping} (스코프 역할 {explosion.scopedRolesSeeded}개 생성).
          Stage 2(ABAC)에서는 "같은 부서면 편집" 같은 속성 규칙 1개로 대체됩니다.
        </p>
      </Card>
    </div>
  )
}
