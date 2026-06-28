import type {
  AbacResourceDecisions,
  AttemptResult,
  Decision,
  DocumentDto,
  FolderDto,
  Me,
  RolesView,
  UserSummary,
} from '@/types'

/** 모든 시드 유저 공통 비밀번호(백엔드 DataSeeder.COMMON_PASSWORD 와 동일). */
export const COMMON_PASSWORD = 'password'

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`)
  }
  return res.json() as Promise<T>
}

function get(path: string): Promise<Response> {
  return fetch(path, { credentials: 'include' })
}

export const api = {
  /** 현재 로그인 사용자. 미인증(401)이면 null. */
  async me(): Promise<Me | null> {
    const res = await get('/api/me')
    if (res.status === 401) return null
    return json<Me>(res)
  },

  users(): Promise<UserSummary[]> {
    return get('/api/users').then(json<UserSummary[]>)
  },

  /** formLogin: username + 공통 비밀번호. 성공 시 세션 쿠키가 설정된다. */
  async login(username: string): Promise<boolean> {
    const res = await fetch('/api/login', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ username, password: COMMON_PASSWORD }),
    })
    return res.ok
  },

  async logout(): Promise<void> {
    await fetch('/api/logout', { method: 'POST', credentials: 'include' })
  },

  documents(): Promise<DocumentDto[]> {
    return get('/api/rbac/documents').then(json<DocumentDto[]>)
  },

  folders(): Promise<FolderDto[]> {
    return get('/api/rbac/folders').then(json<FolderDto[]>)
  },

  decisions(): Promise<Decision[]> {
    return get('/api/rbac/documents/decisions').then(json<Decision[]>)
  },

  roles(): Promise<RolesView> {
    return get('/api/rbac/roles').then(json<RolesView>)
  },

  /** ABAC 목록: PDP READ 결정으로 행 단위 필터링된 문서만(RBAC 와 달리 전부가 아님). */
  abacDocuments(): Promise<DocumentDto[]> {
    return get('/api/abac/documents').then(json<DocumentDto[]>)
  },

  /** 모든 문서 × 액션의 결정 + 규칙 trace(거부 케이스도 사유 포함). 인가 우회 없이 설명만. */
  abacDocumentDecisions(): Promise<AbacResourceDecisions[]> {
    return get('/api/abac/documents/decisions').then(json<AbacResourceDecisions[]>)
  },

  /** 모든 폴더 × 액션의 결정 + 규칙 trace. */
  abacFolderDecisions(): Promise<AbacResourceDecisions[]> {
    return get('/api/abac/folders/decisions').then(json<AbacResourceDecisions[]>)
  },

  /**
   * 실제 액션을 시도하고 HTTP 상태만 돌려준다(이론↔실제 인가 결과 대조용).
   * 응답 본문은 데모에 불필요해 버린다.
   */
  async attempt(method: string, path: string, body?: unknown): Promise<AttemptResult> {
    const res = await fetch(path, {
      method,
      credentials: 'include',
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
    return { status: res.status, ok: res.ok }
  },
}
