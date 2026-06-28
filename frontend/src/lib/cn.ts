/** Tailwind 클래스 조건부 결합(truthy 만 join — 가벼운 헬퍼). */
export function cn(...parts: Array<string | false | null | undefined>): string {
  return parts.filter(Boolean).join(' ')
}
