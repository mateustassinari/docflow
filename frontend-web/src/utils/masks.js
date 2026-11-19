export const maskCPF = (value) => {
  return value
    .replace(/\D/g, '') // remove all non-digits
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})/, '$1-$2')
    .replace(/(-\d{2})\d+?$/, '$1') // keep only 11 digits
}

export const maskCNPJ = (value) => {
  return value
    .replace(/\D/g, '')
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1/$2')
    .replace(/(\d{4})(\d{1,2})/, '$1-$2')
    .replace(/(-\d{2})\d+?$/, '$1') // keep only 14 digits
}

export const maskCpfCnpj = (value) => {
  const digits = value.replace(/\D/g, '')
  if (digits.length <= 11) return maskCPF(digits)
  return maskCNPJ(digits)
}

export const maskPhone = (value) => {
  let v = value.replace(/\D/g, '')
  if (v.length > 11) v = v.substring(0, 11)
  
  if (v.length > 10) {
    v = v.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3')
  } else if (v.length > 6) {
    v = v.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3')
  } else if (v.length > 2) {
    v = v.replace(/^(\d{2})(\d{0,5})/, '($1) $2')
  } else {
    v = v.replace(/^(\d*)/, '($1')
  }
  return v
}

export const maskCurrency = (value) => {
  let v = value.replace(/\D/g, '')
  if (!v) return ''
  v = (parseInt(v, 10) / 100).toFixed(2)
  v = v.replace('.', ',')
  v = v.replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.')
  return v
}

export const parseCurrency = (value) => {
  if (value === null || value === undefined || value === '') return 0
  if (typeof value === 'number') return value
  return Number(String(value).replace(/\./g, '').replace(',', '.')) || 0
}

export const formatCurrencyInput = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) return maskCurrency(String(value))
  return numberValue.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
