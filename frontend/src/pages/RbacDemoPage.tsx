import { useEffect, useState } from 'react'
import { useAuth } from '@/auth'
import { api } from '@/lib/api'
import { Badge, Button, Card, CardTitle, Table, Td, Th } from '@/components/ui'
import type { AttemptResult, Decision, DocumentDto } from '@/types'

interface ActionSpec {
  key: string
  label: string
  method: string
  needs: string
  path: (id: number) => string
  body?: () => unknown
}

const ACTIONS: ActionSpec[] = [
  { key: 'READ', label: '읽기', method: 'GET', needs: 'document:read', path: (id) => `/api/rbac/documents/${id}` },
  {
    key: 'WRITE',
    label: '수정',
    method: 'PUT',
    needs: 'document:write',
    path: (id) => `/api/rbac/documents/${id}`,
    body: () => ({ title: '수정됨', content: '데모로 수정한 내용' }),
  },
  {
    key: 'DELETE',
    label: '삭제',
    method: 'DELETE',
    needs: 'document:delete',
    path: (id) => `/api/rbac/documents/${id}`,
  },
  {
    key: 'SHARE',
    label: '공유',
    method: 'POST',
    needs: 'document:share',
    path: (id) => `/api/rbac/documents/${id}/share`,
    body: () => ({ targetUsername: 'bob' }),
  },
]

function statusTone(r: AttemptResult): 'green' | 'red' | 'amber' {
  if (r.ok) return 'green'
  if (r.status === 403) return 'red'
  return 'amber'
}

export function RbacDemoPage() {
  const { me } = useAuth()
  const [decisions, setDecisions] = useState<Decision[]>([])
  const [documents, setDocuments] = useState<DocumentDto[]>([])
  const [targetId, setTargetId] = useState<number | null>(null)
  const [results, setResults] = useState<Record<string, AttemptResult>>({})

  useEffect(() => {
    setResults({})
    if (!me) {
      setDecisions([])
      setDocuments([])
      setTargetId(null)
      return
    }
    api.decisions().then(setDecisions).catch(() => setDecisions([]))
    api
      .documents()
      .then((docs) => {
        setDocuments(docs)
        setTargetId(docs.length > 0 ? docs[0].id : null)
      })
      .catch(() => {
        setDocuments([])
        setTargetId(null)
      })
  }, [me])

  async function attempt(action: ActionSpec) {
    if (targetId == null) return
    const result = await api.attempt(action.method, action.path(targetId), action.body?.())
    setResults((prev) => ({ ...prev, [action.key]: result }))
    if (action.key === 'DELETE' && result.ok) {
      const docs = await api.documents().catch(() => [])
      setDocuments(docs)
      setTargetId(docs.length > 0 ? docs[0].id : null)
    }
  }

  if (!me) {
    return (
      <Card>
        <p className="text-slate-500">유저를 전환하면 그 역할로 가능한 액션과 사유가 표시됩니다.</p>
      </Card>
    )
  }

  return (
    <div className="grid gap-5">
      <Card>
        <CardTitle hint="@PreAuthorize 와 동일한 권한으로 계산 → 설명=실제">
          액션 결정 ({me.username})
        </CardTitle>
        <Table
          head={
            <>
              <Th>액션</Th>
              <Th>필요 권한</Th>
              <Th>결정</Th>
              <Th>사유</Th>
            </>
          }
        >
          {decisions.map((d) => (
            <tr key={d.action}>
              <Td>{d.label}</Td>
              <Td>
                <Badge>{d.requiredAuthority}</Badge>
              </Td>
              <Td>
                <Badge tone={d.allowed ? 'green' : 'red'}>{d.allowed ? '허용' : '거부'}</Badge>
              </Td>
              <Td>
                <span className="text-slate-500">{d.reason}</span>
              </Td>
            </tr>
          ))}
        </Table>
      </Card>

      <Card>
        <CardTitle hint="이론(위 결정) ↔ 실제 HTTP 응답 대조">라이브 시도</CardTitle>
        {documents.length === 0 ? (
          <p className="text-slate-500">대상 문서가 없습니다(읽기 권한이 없거나 모두 삭제됨).</p>
        ) : (
          <>
            <label className="mb-4 flex items-center gap-2 text-sm text-slate-500">
              대상 문서
              <select
                value={targetId ?? ''}
                onChange={(e) => setTargetId(Number(e.target.value))}
                className="rounded-md border border-slate-300 bg-white px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-900"
              >
                {documents.map((d) => (
                  <option key={d.id} value={d.id}>
                    #{d.id} {d.title}
                  </option>
                ))}
              </select>
            </label>
            <div className="flex flex-wrap gap-3">
              {ACTIONS.map((a) => {
                const r = results[a.key]
                return (
                  <div key={a.key} className="flex items-center gap-2">
                    <Button variant="outline" onClick={() => void attempt(a)}>
                      {a.method} {a.label}
                    </Button>
                    {r ? <Badge tone={statusTone(r)}>HTTP {r.status}</Badge> : null}
                  </div>
                )
              })}
            </div>
            <p className="mt-3 text-xs text-slate-400">
              삭제가 성공(204)하면 문서가 실제로 사라집니다. 백엔드를 재시작하면 시드가 복구됩니다.
            </p>
          </>
        )}
      </Card>
    </div>
  )
}
