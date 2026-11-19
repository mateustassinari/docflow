// Nome exibido na interface. Definido no build (VITE_APP_NAME) para o código não carregar a marca de um negócio real.
export const APP_NAME = import.meta.env.VITE_APP_NAME || 'DocFlow'

// Iniciais do avatar: primeira letra das duas primeiras palavras, ignorando "da", "de"... ("Ateliê Exemplo" -> "AE"),
// ou as maiúsculas de um nome só ("DocFlow" -> "DF").
export function appInitials(name = APP_NAME) {
  const words = name.trim().split(/\s+/).filter(word => word.length > 2)
  if (words.length > 1) return (words[0][0] + words[1][0]).toUpperCase()
  const capitals = name.replace(/[^A-ZÀ-Ý]/g, '')
  return (capitals.length >= 2 ? capitals.slice(0, 2) : name.slice(0, 2)).toUpperCase()
}
