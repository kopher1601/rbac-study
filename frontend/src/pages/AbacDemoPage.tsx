import { Fragment, useEffect, useState } from 'react'
import { useAuth } from '@/auth'
import { api } from '@/lib/api'
import { Badge, Button, Card, CardTitle, Table, Td, Th } from '@/components/ui'
import type { AbacResourceDecisions, AttemptResult } from '@/types'

interface ActionSpec {
  key: string
  label: string
  method: string
  path: (id: number) => string
  body?: () => unknown
}

// RBAC 데모와 같은 액션 집합. 단 경로는 /api/abac/* 이고, 결과는 정적 권한이 아니라
// 그 문서의 속성(소유/민감도/부서)과 환경(시간)으로 갈린다.
const ACTIONS: ActionSpec[] = [
  { key: 'READ', label: '읽기', method: 'GET', path: (id) => `/api/abac/documents/${id}` },
  {
    key: 'WRITE',
    label: '수정',
    method: 'PUT',
    path: (id) => `/api/abac/documents/${id}`,
    body: () => ({ title: '수정됨', content: '데모로 수정한 내용' }),
  },
  { key: 'DELETE', label: '삭제', method: 'DELETE', path: (id) => `/api/abac/documents/${id}` },
  {
    key: 'SHARE',
    label: '공유',
    method: 'POST',
    path: (id) => `/api/abac/documents/${id}/share`,
    body: () => ({ targetUsername: 'bob' }),
  },
]

function statusTone(r: AttemptResult): 'green' | 'red' | 'amber' {
  if (r.ok) return 'green'
  if (r.status === 403) return 'red'
  return 'amber'
}

function effectTone(effect: string): 'green' | 'red' | 'gray' {
  if (effect === 'PERMIT') return 'green'
  if (effect === 'DENY') return 'red'
  return 'gray'
}

export function AbacDemoPage() {
  const { me } = useAuth()
  const [resources, setResources] = useState<AbacResourceDecisions[]>([])
  const [targetId, setTargetId] = useState<number | null>(null)
  const [expanded, setExpanded] = useState<string | null>(null)
  const [results, setResults] = useState<Record<string, AttemptResult>>({})

  function reload() {
    // 설명 엔드포인트는 거부 케이스도 사유와 함께 모두 반환 → 거부된 문서도 picker 에 보인다.
    api
      .abacDocumentDecisions()
      .then((rows) => {
        setResources(rows)
        setTargetId((prev) =>
          prev != null && rows.some((r) => r.resourceId === prev)
            ? prev
            : rows.length > 0
              ? rows[0].resourceId
              : null,
        )
      })
      .catch(() => {
        setResources([])
        setTargetId(null)
      })
  }

  useEffect(() => {
    setResults({})
    setExpanded(null)
    if (!me) {
      setResources([])
      setTargetId(null)
      return
    }
    reload()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [me])

  async function attempt(action: ActionSpec) {
    if (targetId == null) return
    const result = await api.attempt(action.method, action.path(targetId), action.body?.())
    setResults((prev) => ({ ...prev, [action.key]: result }))
    // 수정/삭제가 성공하면 제목·목록이 바뀌므로 결정을 다시 불러온다.
    if (result.ok && (action.key === 'DELETE' || action.key === 'WRITE')) {
      reload()
    }
  }

  if (!me) {
    return (
      <Card>
        <p className="text-slate-500">
          유저를 전환하면 그 유저의 <b>부서·등급</b>으로 각 문서의 정책 결정과 사유가 표시됩니다.
        </p>
      </Card>
    )
  }

  const selected = resources.find((r) => r.resourceId === targetId) ?? null

  return (
    <div className="grid gap-5">
      <Card>
        <CardTitle hint="정책의 입력이 되는 주체 속성">내 속성 ({me.username})</CardTitle>
        <div className="flex flex-wrap gap-x-6 gap-y-2 text-sm">
          <span>
            부서 <Badge tone="blue">{me.department}</Badge>
          </span>
          <span>
            보안등급 <Badge tone="blue">{me.clearanceLevel}</Badge>
          </span>
          <span className="text-slate-500">
            ABAC 는 역할이 아니라 이 속성들 + 문서 속성(소유/민감도) + 환경(시간)으로 결정합니다.
          </span>
        </div>
      </Card>

      <Card>
        <CardTitle hint="@abacAccess(=PDP) 와 동일 결정 → 설명=실제. 행을 클릭하면 규칙 trace 펼침">
          문서별 정책 결정
        </CardTitle>
        {resources.length === 0 ? (
          <p className="text-slate-500">문서가 없습니다.</p>
        ) : (
          <>
            <label className="mb-4 flex items-center gap-2 text-sm text-slate-500">
              대상 문서
              <select
                value={targetId ?? ''}
                onChange={(e) => {
                  setTargetId(Number(e.target.value))
                  setExpanded(null)
                }}
                className="rounded-md border border-slate-300 bg-white px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-900"
              >
                {resources.map((r) => (
                  <option key={r.resourceId} value={r.resourceId}>
                    #{r.resourceId} {r.title}
                  </option>
                ))}
              </select>
            </label>

            {selected ? (
              <Table
                head={
                  <>
                    <Th>액션</Th>
                    <Th>결정</Th>
                    <Th>결정 규칙</Th>
                    <Th>요약</Th>
                  </>
                }
              >
                {selected.decisions.map((d) => (
                  <Fragment key={d.action}>
                    <tr
                      className="cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-800/50"
                      onClick={() => setExpanded((cur) => (cur === d.action ? null : d.action))}
                    >
                      <Td>
                        <span className="mr-1 text-slate-400">
                          {expanded === d.action ? '▾' : '▸'}
                        </span>
                        {d.label}
                      </Td>
                      <Td>
                        <Badge tone={d.permitted ? 'green' : 'red'}>
                          {d.permitted ? '허용' : '거부'}
                        </Badge>
                      </Td>
                      <Td>
                        <Badge tone="blue">{d.matchedRule}</Badge>
                      </Td>
                      <Td>
                        <span className="text-slate-500">{d.summary}</span>
                      </Td>
                    </tr>
                    {expanded === d.action ? (
                      <tr>
                        <td
                          colSpan={4}
                          className="border-b border-slate-100 bg-slate-50 px-3 py-3 dark:border-slate-800 dark:bg-slate-800/30"
                        >
                          <div className="grid gap-1.5">
                            {d.trace.map((t) => (
                              <div
                                key={t.ruleId}
                                className={t.applicable ? 'flex items-center gap-2' : 'flex items-center gap-2 opacity-50'}
                              >
                                <Badge tone={effectTone(t.effect)}>{t.effect}</Badge>
                                <span className="font-mono text-xs text-slate-600 dark:text-slate-300">
                                  {t.ruleId}
                                </span>
                                <span className="text-xs text-slate-500">{t.reason}</span>
                              </div>
                            ))}
                          </div>
                        </td>
                      </tr>
                    ) : null}
                  </Fragment>
                ))}
              </Table>
            ) : null}
          </>
        )}
      </Card>

      <Card>
        <CardTitle hint="이론(위 결정) ↔ 실제 HTTP 응답 대조">라이브 시도</CardTitle>
        {targetId == null ? (
          <p className="text-slate-500">대상 문서가 없습니다.</p>
        ) : (
          <>
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
              같은 문서라도 쓰기/삭제/공유는 <b>업무시간(평일 09–18시)</b>에만 통과합니다 — 읽기는 24시간.
              RBAC 데모와 같은 문서로 결과를 비교해 보세요.
            </p>
          </>
        )}
      </Card>
    </div>
  )
}
