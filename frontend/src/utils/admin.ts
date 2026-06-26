export function userStatusText(status: number) {
  return status === 1 ? '启用' : '禁用'
}

export function userStatusType(status: number) {
  return status === 1 ? 'success' : 'danger'
}

export function nullableText(value?: string | number | null) {
  return value === null || value === undefined || value === '' ? '-' : value
}

export function adminRecordUser(row: { username: string | null; nickname: string | null }) {
  return row.nickname || row.username || '-'
}
