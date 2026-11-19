export default function SortableTh({ children, sortKey, sortConfig, onSort }) {
  const active = sortConfig.key === sortKey
  const indicator = active ? (sortConfig.direction === 'asc' ? '↑' : '↓') : '↕'

  return (
    <th>
      <button type="button" className={`sortable-th ${active ? 'active' : ''}`} onClick={() => onSort(sortKey)}>
        <span>{children}</span>
        <span aria-hidden="true">{indicator}</span>
      </button>
    </th>
  )
}
