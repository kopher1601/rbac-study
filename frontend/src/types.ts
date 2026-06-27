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
