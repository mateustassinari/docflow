import { createContext, useContext } from 'react'

export const AuthContext = createContext(null)

/** { status: 'loading' | 'anonymous' | 'authenticated', username, login, logout } */
export function useAuth() {
  return useContext(AuthContext)
}
