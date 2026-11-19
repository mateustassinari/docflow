export function parseLocalDate(value) {
  if (!value) return null

  if (value instanceof Date) {
    const date = new Date(value)
    date.setHours(0, 0, 0, 0)
    return Number.isNaN(date.getTime()) ? null : date
  }

  const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (match) {
    return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]))
  }

  const date = new Date(value)
  date.setHours(0, 0, 0, 0)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatDateBR(value, options) {
  const date = parseLocalDate(value)
  return date ? date.toLocaleDateString('pt-BR', options) : ''
}
