import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Mesma origem da API também em desenvolvimento (como o nginx no Docker): sem CORS, e o cookie de sessão vai sozinho
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: path => path.replace(/^\/api/, ''),
      },
    },
  },
})
