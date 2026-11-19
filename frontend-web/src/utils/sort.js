import { parseLocalDate } from './date'

export function compareValues(a, b, type = 'text') {
  if (type === 'number') {
    return Number(a || 0) - Number(b || 0)
  }

  if (type === 'date') {
    const dateA = parseLocalDate(a)
    const dateB = parseLocalDate(b)
    return (dateA?.getTime() || 0) - (dateB?.getTime() || 0)
  }

  return String(a || '').localeCompare(String(b || ''), 'pt-BR', {
    sensitivity: 'base',
    numeric: true,
  })
}

export function sortItems(items, sortConfig, accessors) {
  const accessor = accessors[sortConfig.key]
  if (!accessor) return items

  return [...items].sort((a, b) => {
    const direction = sortConfig.direction === 'asc' ? 1 : -1
    return compareValues(accessor.value(a), accessor.value(b), accessor.type) * direction
  })
}

export function nextSort(current, key) {
  if (current.key !== key) {
    return { key, direction: 'asc' }
  }

  return { key, direction: current.direction === 'asc' ? 'desc' : 'asc' }
}
