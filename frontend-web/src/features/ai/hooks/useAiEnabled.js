import { useEffect, useState } from 'react'
import { aiService } from '../services/aiService'

// O status não muda enquanto o sistema roda: uma consulta só, compartilhada entre menu e página.
let statusRequest = null

function fetchStatus() {
  if (!statusRequest) {
    statusRequest = aiService
      .getStatus()
      .then(data => Boolean(data?.enabled))
      .catch(() => false)
  }
  return statusRequest
}

/** null enquanto carrega; depois true ou false conforme ai.enabled no backend. */
export function useAiEnabled() {
  const [enabled, setEnabled] = useState(null)

  useEffect(() => {
    let active = true
    fetchStatus().then(value => {
      if (active) setEnabled(value)
    })
    return () => {
      active = false
    }
  }, [])

  return enabled
}
