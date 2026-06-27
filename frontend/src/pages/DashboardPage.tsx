import { useEffect, useState } from 'react'
import { useAuth } from '@/auth'
import { api } from '@/lib/api'
import { Badge, Card, CardTitle, Table, Td, Th } from '@/components/ui'
import type { DocumentDto, FolderDto } from '@/types'

export function DashboardPage() {
  const { me } = useAuth()
  const [documents, setDocuments] = useState<DocumentDto[]>([])
  const [folders, setFolders] = useState<FolderDto[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!me) {
      setDocuments([])
      setFolders([])
      setError(null)
      return
    }
    setError(null)
    Promise.all([api.documents(), api.folders()])
      .then(([d, f]) => {
        setDocuments(d)
        setFolders(f)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : '조회 실패'))
  }, [me])

  if (!me) {
    return (
      <Card>
        <p className="text-slate-500">
          상단의 <b>유저 전환</b>에서 시드 유저(alice/bob/carol/dave)를 선택해 로그인하세요. 모든
          유저의 공통 비밀번호는 <code>password</code> 입니다.
        </p>
      </Card>
    )
  }

  return (
    <div className="grid gap-5">
      <Card>
        <CardTitle hint={`부서 ${me.department} · clearance ${me.clearanceLevel}`}>
          현재 사용자: {me.username}
        </CardTitle>
        <div className="mb-2 text-sm text-slate-500">역할</div>
        <div className="mb-4 flex flex-wrap gap-1">
          {me.roles.map((r) => (
            <Badge key={r} tone="blue">
              {r}
            </Badge>
          ))}
        </div>
        <div className="mb-2 text-sm text-slate-500">권한(GrantedAuthority)</div>
        <div className="flex flex-wrap gap-1">
          {me.authorities.map((a) => (
            <Badge key={a} tone={a.startsWith('ROLE_') ? 'amber' : 'gray'}>
              {a}
            </Badge>
          ))}
        </div>
      </Card>

      {error ? (
        <Card>
          <p className="text-rose-600">문서/폴더 조회 실패: {error} (읽기 권한이 없을 수 있습니다)</p>
        </Card>
      ) : (
        <>
          <Card>
            <CardTitle hint={`${documents.length}건`}>문서</CardTitle>
            <Table
              head={
                <>
                  <Th>ID</Th>
                  <Th>제목</Th>
                  <Th>폴더</Th>
                  <Th>소유자</Th>
                  <Th>민감도</Th>
                </>
              }
            >
              {documents.map((d) => (
                <tr key={d.id}>
                  <Td>{d.id}</Td>
                  <Td>{d.title}</Td>
                  <Td>{d.folderName ?? '—'}</Td>
                  <Td>{d.ownerId}</Td>
                  <Td>{d.sensitivityLevel}</Td>
                </tr>
              ))}
            </Table>
          </Card>

          <Card>
            <CardTitle hint={`${folders.length}건`}>폴더</CardTitle>
            <Table
              head={
                <>
                  <Th>ID</Th>
                  <Th>이름</Th>
                  <Th>상위 폴더</Th>
                  <Th>소유자</Th>
                  <Th>민감도</Th>
                </>
              }
            >
              {folders.map((f) => (
                <tr key={f.id}>
                  <Td>{f.id}</Td>
                  <Td>{f.name}</Td>
                  <Td>{f.parentFolderId ?? '—'}</Td>
                  <Td>{f.ownerId}</Td>
                  <Td>{f.sensitivityLevel}</Td>
                </tr>
              ))}
            </Table>
          </Card>
        </>
      )}
    </div>
  )
}
