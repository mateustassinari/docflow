import { useEffect, useMemo, useState } from 'react'
import { maskCpfCnpj, maskPhone } from '../../../utils/masks'
import { nextSort, sortItems } from '../../../utils/sort'
import { clientsService } from '../services/clientsService'

const PAGE_SIZE = 8
const EMPTY_FORM = { nome: '', email: '', telefone: '', cpfCnpj: '' }

export function useClientsPage() {
  const [clientes, setClientes] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [sortConfig, setSortConfig] = useState({ key: 'nome', direction: 'asc' })
  const [showModal, setShowModal] = useState(false)
  const [toast, setToast] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [deleteConfirmId, setDeleteConfirmId] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  const fetchClientes = () => {
    setLoading(true)
    clientsService.list()
      .then(data => {
        setClientes(Array.isArray(data) ? data : [])
      })
      .catch(() => {
        setClientes([])
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchClientes()
  }, [])

  useEffect(() => {
    setCurrentPage(1)
  }, [searchTerm])

  const setF = field => event => {
    setForm(prev => ({ ...prev, [field]: event.target.value }))
  }

  const setMaskedF = (field, maskFn) => event => {
    setForm(prev => ({ ...prev, [field]: maskFn(event.target.value) }))
  }

  const setCpfCnpj = setMaskedF('cpfCnpj', maskCpfCnpj)
  const setTelefone = setMaskedF('telefone', maskPhone)

  const handleEdit = client => {
    setForm({
      nome: client.nome || '',
      email: client.email || '',
      telefone: client.telefone || '',
      cpfCnpj: client.cpfCnpj || '',
    })
    setEditingId(client.id)
    setShowModal(true)
  }

  const handleDelete = async id => {
    try {
      await clientsService.remove(id)
      showToast('Cliente excluído com sucesso!', 'success')
      setDeleteConfirmId(null)
      fetchClientes()
    } catch (err) {
      showToast(err.message, 'error')
    }
  }

  const openNewClientModal = () => {
    setForm(EMPTY_FORM)
    setEditingId(null)
    setShowModal(true)
  }

  const handleSubmit = async event => {
    event.preventDefault()
    setSaving(true)
    try {
      if (editingId) {
        await clientsService.update(editingId, form)
      } else {
        await clientsService.create(form)
      }

      showToast(editingId ? 'Cliente atualizado com sucesso!' : 'Cliente cadastrado com sucesso!', 'success')
      setShowModal(false)
      fetchClientes()
    } catch (err) {
      showToast(err.message || 'Erro ao conectar ao servidor', 'error')
    } finally {
      setSaving(false)
    }
  }

  const filtered = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) return clientes

    return clientes.filter(client => (
      [
        client.nome,
        client.email,
        client.telefone,
        client.cpfCnpj,
        client.id ? `CLI-${client.id}` : '',
      ].some(value => String(value || '').toLowerCase().includes(term))
    ))
  }, [clientes, searchTerm])

  const sorted = useMemo(() => sortItems(filtered, sortConfig, {
    nome: { value: item => item.nome, type: 'text' },
    contato: { value: item => item.email || item.telefone, type: 'text' },
    documento: { value: item => item.cpfCnpj, type: 'text' },
    id: { value: item => item.id, type: 'number' },
  }), [filtered, sortConfig])

  const totalPages = Math.max(Math.ceil(sorted.length / PAGE_SIZE), 1)
  const safePage = Math.min(currentPage, totalPages)
  const pageStart = (safePage - 1) * PAGE_SIZE
  const paginated = sorted.slice(pageStart, pageStart + PAGE_SIZE)

  const handleSort = key => {
    setSortConfig(current => nextSort(current, key))
  }

  const previousPage = () => {
    setCurrentPage(page => Math.max(page - 1, 1))
  }

  const nextPage = () => {
    setCurrentPage(page => Math.min(page + 1, totalPages))
  }

  return {
    currentPage,
    deleteConfirmId,
    editingId,
    filtered,
    form,
    handleDelete,
    handleEdit,
    handleSort,
    handleSubmit,
    loading,
    nextPage,
    openNewClientModal,
    pageStart,
    paginated,
    previousPage,
    safePage,
    saving,
    searchTerm,
    setCpfCnpj,
    setDeleteConfirmId,
    setEmail: setF('email'),
    setNome: setF('nome'),
    setSearchTerm,
    setShowModal,
    setTelefone,
    setCurrentPage,
    showModal,
    sortConfig,
    toast,
    totalPages,
  }
}
