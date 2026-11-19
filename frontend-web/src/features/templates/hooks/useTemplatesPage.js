import { useEffect, useEffectEvent, useMemo, useState } from 'react'
import { useToast } from '../../../shared/hooks/useToast'
import { templatesService } from '../services/templatesService'

export function useTemplatesPage({
  emptyTemplate,
  buildPreviewPayload,
  parseDynamicClauses,
  publishPreviewPayload,
  stringifyDynamicClauses,
  normalizeTemplate,
  suggestedClauses,
  liveServiceTypes,
}) {
  const [templates, setTemplates] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [form, setForm] = useState(emptyTemplate)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [openEditorCards, setOpenEditorCards] = useState({})
  const { showToast, toast } = useToast()

  const loadTemplates = (preferredId = null) =>
    templatesService.list().then(data => {
      const list = Array.isArray(data) ? data : []
      setTemplates(list)

      const nextId = preferredId ?? selectedId ?? list[0]?.id ?? null
      const selected = list.find(item => item.id === nextId) || null
      setSelectedId(selected?.id ?? null)
      setForm(selected ? normalizeTemplate(selected) : emptyTemplate)
    })

  // Carga inicial: useEffectEvent usa as versões mais recentes de loadTemplates e showToast sem refazer a carga
  const loadInitialTemplates = useEffectEvent(() => {
    loadTemplates()
      .catch(() => showToast('Não foi possível carregar os templates.', 'error'))
      .finally(() => setLoading(false))
  })

  useEffect(() => {
    loadInitialTemplates()
  }, [])

  useEffect(() => {
    publishPreviewPayload(buildPreviewPayload(form))
  }, [buildPreviewPayload, form, publishPreviewPayload])

  const selectedTemplate = useMemo(
    () => templates.find(item => item.id === selectedId) || null,
    [templates, selectedId]
  )

  const dynamicClausesState = useMemo(
    () => parseDynamicClauses(form.clausulasConfig),
    [form.clausulasConfig, parseDynamicClauses]
  )

  const dynamicClauses = dynamicClausesState.clauses
  const showPaintClauseSections = liveServiceTypes.includes(form.tipoServico)
  const showDynamicClauseSections = !showPaintClauseSections
  const selectedVersion = selectedTemplate?.versao || '1'
  const isCreatingVersion = Boolean(selectedTemplate) && String(form.versao || '') !== String(selectedVersion)

  const handleChange = (field, value) => {
    setForm(current => ({ ...current, [field]: value }))
  }

  const handleDynamicClausesChange = clauses => {
    handleChange('clausulasConfig', stringifyDynamicClauses(clauses))
  }

  const handleUseSuggestedClauses = () => {
    handleDynamicClausesChange(suggestedClauses[form.tipoServico] || suggestedClauses.PINTURA_AO_VIVO)
    setOpenEditorCards(current => ({ ...current, principais: true }))
  }

  const handleAddClause = () => {
    handleDynamicClausesChange([
      ...dynamicClauses,
      { titulo: `CLÁUSULA ${dynamicClauses.length + 1}`, textoAntes: '', itens: [], textoDepois: '' },
    ])
  }

  const handleUpdateClause = (index, field, value) => {
    handleDynamicClausesChange(dynamicClauses.map((clause, itemIndex) => (
      itemIndex === index ? { ...clause, [field]: value } : clause
    )))
  }

  const handleRemoveClause = index => {
    handleDynamicClausesChange(dynamicClauses.filter((_, itemIndex) => itemIndex !== index))
  }

  const handleMoveClause = (index, direction) => {
    const targetIndex = index + direction
    if (targetIndex < 0 || targetIndex >= dynamicClauses.length) return

    const nextClauses = [...dynamicClauses]
    const [moved] = nextClauses.splice(index, 1)
    nextClauses.splice(targetIndex, 0, moved)
    handleDynamicClausesChange(nextClauses)
  }

  const handleAddClauseItem = clauseIndex => {
    handleDynamicClausesChange(dynamicClauses.map((clause, index) => (
      index === clauseIndex ? { ...clause, itens: [...(clause.itens || []), ''] } : clause
    )))
  }

  const handleUpdateClauseItem = (clauseIndex, itemIndex, value) => {
    handleDynamicClausesChange(dynamicClauses.map((clause, index) => {
      if (index !== clauseIndex) return clause
      return {
        ...clause,
        itens: clause.itens.map((item, currentItemIndex) => currentItemIndex === itemIndex ? value : item),
      }
    }))
  }

  const handleRemoveClauseItem = (clauseIndex, itemIndex) => {
    handleDynamicClausesChange(dynamicClauses.map((clause, index) => {
      if (index !== clauseIndex) return clause
      return {
        ...clause,
        itens: clause.itens.filter((_, currentItemIndex) => currentItemIndex !== itemIndex),
      }
    }))
  }

  const handleSelect = template => {
    setSelectedId(template.id)
    setForm(normalizeTemplate(template))
  }

  const handleNew = () => {
    setSelectedId(null)
    setForm(emptyTemplate)
    setOpenEditorCards({ identidade: true })
  }

  const handleOpenPreviewWindow = () => {
    publishPreviewPayload(buildPreviewPayload(form))
    window.open('/templates/preview-window', 'template-preview-window', 'width=1180,height=920')
  }

  const toggleEditorCard = key => {
    setOpenEditorCards(current => ({ ...current, [key]: !current[key] }))
  }

  const handleSubmit = async event => {
    event.preventDefault()
    setSaving(true)

    try {
      const isEditing = Boolean(selectedId) && !isCreatingVersion
      const saved = isEditing
        ? await templatesService.update(selectedId, form)
        : await templatesService.create(form)
      await loadTemplates(saved.id)
      showToast(isCreatingVersion ? 'Nova versão criada com sucesso!' : isEditing ? 'Template atualizado com sucesso!' : 'Template criado com sucesso!')
    } catch (err) {
      showToast(err.message || 'Erro ao salvar template.', 'error')
    } finally {
      setSaving(false)
    }
  }

  return {
    dynamicClauses,
    dynamicClausesState,
    form,
    handleAddClause,
    handleAddClauseItem,
    handleChange,
    handleMoveClause,
    handleNew,
    handleOpenPreviewWindow,
    handleRemoveClause,
    handleRemoveClauseItem,
    handleSelect,
    handleSubmit,
    handleUpdateClause,
    handleUpdateClauseItem,
    handleUseSuggestedClauses,
    isCreatingVersion,
    loading,
    openEditorCards,
    saving,
    selectedId,
    selectedTemplate,
    selectedVersion,
    showDynamicClauseSections,
    showPaintClauseSections,
    templates,
    toast,
    toggleEditorCard,
  }
}
