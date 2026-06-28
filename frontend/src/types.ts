export interface UserSummary {
  username: string
  department: string
  roles: string[]
}

export interface Me {
  username: string
  department: string
  clearanceLevel: number
  roles: string[]
  authorities: string[]
}

export interface DocumentDto {
  id: number
  title: string
  content: string | null
  folderId: number | null
  folderName: string | null
  ownerId: number
  sensitivityLevel: number
}

export interface FolderDto {
  id: number
  name: string
  parentFolderId: number | null
  ownerId: number
  sensitivityLevel: number
}

export interface Decision {
  action: string
  label: string
  allowed: boolean
  requiredAuthority: string
  reason: string
}

export interface RoleDto {
  name: string
  scoped: boolean
  permissions: string[]
}

export interface RoleExplosion {
  departments: number
  actionTiers: number
  rolesNeededForFullScoping: number
  scopedRolesSeeded: number
  note: string
}

export interface RolesView {
  roleCount: number
  permissionCount: number
  roles: RoleDto[]
  explosion: RoleExplosion
}

export interface AttemptResult {
  status: number
  ok: boolean
}

// ── ABAC (Stage 2) ────────────────────────────────────────────────

/** 규칙 한 건의 평가(why 패널의 한 행). effect = PERMIT | DENY | NOT_APPLICABLE. */
export interface RuleTrace {
  ruleId: string
  description: string
  effect: string
  applicable: boolean
  reason: string
}

/** 한 리소스에 대한 한 액션의 ABAC 결정 + 규칙 trace. */
export interface AbacDecision {
  action: string
  label: string
  permitted: boolean
  matchedRule: string
  summary: string
  trace: RuleTrace[]
}

/** 한 리소스(문서/폴더)의 액션별 결정 묶음(설명 엔드포인트 응답 단위). */
export interface AbacResourceDecisions {
  resourceId: number
  title: string
  decisions: AbacDecision[]
}
