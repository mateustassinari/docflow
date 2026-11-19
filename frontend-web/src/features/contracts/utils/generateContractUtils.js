import { formatDateBR } from '../../../utils/date'

export function formatDate(value) {
  return formatDateBR(value) || 'Data não definida'
}

export function formatMoney(value) {
  return Number(value || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

export function formatTime(value) {
  return value ? String(value).slice(0, 5) : ''
}

export function isTemplateCompatible(evento, template) {
  if (evento?.tipoServico && template?.tipoServico) {
    return evento.tipoServico === template.tipoServico
  }

  return true
}

export function serviceLabel(item) {
  const map = {
    PINTURA_AO_VIVO: 'Pintura ao vivo',
    AQUARELA_AO_VIVO: 'Aquarela ao vivo',
    ENCOMENDA_ARTISTICA: 'Encomenda artística personalizada',
    IDENTIDADE_VISUAL: 'Identidade visual',
  }
  return map[item?.tipoServico] || 'Serviço geral'
}

export function isLiveService(value) {
  return value === 'PINTURA_AO_VIVO' || value === 'AQUARELA_AO_VIVO'
}
