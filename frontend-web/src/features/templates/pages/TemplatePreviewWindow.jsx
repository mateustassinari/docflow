import { useEffect, useRef, useState } from 'react'
import { ChevronLeft, ChevronRight, LoaderCircle } from 'lucide-react'
import { templatesService } from '../services/templatesService'

const PREVIEW_CHANNEL = 'template-preview'
const PREVIEW_STORAGE_KEY = 'template-preview-payload'

async function countPdfPages(blob) {
  const text = new TextDecoder('latin1').decode(await blob.arrayBuffer())
  const matches = text.match(/\/Type\s*\/Page\b/g)
  return Math.max(matches?.length || 1, 1)
}

function readStoredPayload() {
  const raw = localStorage.getItem(PREVIEW_STORAGE_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export default function TemplatePreviewWindow() {
  const [payload, setPayload] = useState(() => readStoredPayload())
  const [previewUrl, setPreviewUrl] = useState('')
  const [pageCount, setPageCount] = useState(1)
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const previewObjectUrlRef = useRef('')

  useEffect(() => {
    if (!('BroadcastChannel' in window)) return undefined

    const channel = new BroadcastChannel(PREVIEW_CHANNEL)
    channel.onmessage = event => {
      setPayload(event.data)
    }

    return () => channel.close()
  }, [])

  useEffect(() => {
    const onStorage = event => {
      if (event.key !== PREVIEW_STORAGE_KEY || !event.newValue) return

      try {
        setPayload(JSON.parse(event.newValue))
      } catch {
        setPayload(null)
      }
    }

    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  useEffect(() => {
    if (!payload) return undefined

    const controller = new AbortController()
    const timeoutId = setTimeout(async () => {
      setLoading(true)
      setError('')

      try {
        const res = await templatesService.preview(payload, { signal: controller.signal })
        const blob = await res.blob()
        if (previewObjectUrlRef.current) {
          URL.revokeObjectURL(previewObjectUrlRef.current)
        }

        const nextUrl = URL.createObjectURL(blob)
        previewObjectUrlRef.current = nextUrl
        setPreviewUrl(nextUrl)
        setPage(1)
        setPageCount(await countPdfPages(blob))
      } catch (err) {
        if (err.name !== 'AbortError') {
          setError(err.message || 'Não foi possível gerar o PDF de visualização.')
          setPage(1)
          setPageCount(1)
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }, 250)

    return () => {
      controller.abort()
      clearTimeout(timeoutId)
    }
  }, [payload])

  useEffect(() => {
    return () => {
      if (previewObjectUrlRef.current) {
        URL.revokeObjectURL(previewObjectUrlRef.current)
      }
    }
  }, [])

  return (
    <main className="preview-window-page">
      <header className="preview-window-header">
        <div>
          <strong>Preview do template</strong>
          <span>Atualiza automaticamente enquanto você edita o modelo.</span>
        </div>
        <div className="template-preview-status">
          {loading ? (
            <>
              <LoaderCircle size={14} className="spin-icon" />
              Atualizando PDF...
            </>
          ) : (
            'Sincronizado com o editor'
          )}
        </div>
      </header>

      <section className="preview-window-stage">
        {!payload ? (
          <div className="template-pdf-placeholder">
            Abra esta janela a partir da tela de templates para iniciar a visualização.
          </div>
        ) : error ? (
          <div className="template-pdf-placeholder error">{error}</div>
        ) : previewUrl ? (
          <PdfPreviewViewer page={page} pageCount={pageCount} url={previewUrl} onPageChange={setPage} />
        ) : (
          <div className="template-pdf-placeholder">Gerando visualização...</div>
        )}
      </section>
    </main>
  )
}

function PdfPreviewViewer({ url, page, pageCount, onPageChange }) {
  const hasMultiplePages = pageCount > 1
  const pages = Array.from({ length: pageCount }, (_, index) => index + 1)
  const pageUrl = `${url}#page=${page}&toolbar=1&navpanes=0&view=FitH`

  const goToPage = nextPage => {
    onPageChange(Math.min(Math.max(nextPage, 1), pageCount))
  }

  return (
    <div className={`pdf-viewer ${hasMultiplePages ? 'with-thumbnails' : ''}`}>
      {hasMultiplePages && (
        <aside className="pdf-viewer-thumbnails" aria-label="Páginas do PDF">
          {pages.map(pageNumber => (
            <button
              key={pageNumber}
              type="button"
              className={`pdf-thumbnail ${pageNumber === page ? 'active' : ''}`}
              onClick={() => goToPage(pageNumber)}
              title={`Página ${pageNumber}`}
            >
              <span className="pdf-thumbnail-frame">
                <iframe
                  title={`Miniatura da página ${pageNumber}`}
                  src={`${url}#page=${pageNumber}&toolbar=0&navpanes=0&view=FitH`}
                  tabIndex="-1"
                />
              </span>
              <span className="pdf-thumbnail-label">{pageNumber}</span>
            </button>
          ))}
        </aside>
      )}

      <div className="pdf-viewer-main">
        {hasMultiplePages && (
          <div className="pdf-viewer-pagination">
            <button type="button" className="page-btn" onClick={() => goToPage(page - 1)} disabled={page === 1} title="Página anterior">
              <ChevronLeft size={14} />
            </button>
            <span>{page} / {pageCount}</span>
            <button type="button" className="page-btn" onClick={() => goToPage(page + 1)} disabled={page === pageCount} title="Próxima página">
              <ChevronRight size={14} />
            </button>
          </div>
        )}
        <iframe
          key={pageUrl}
          title="Preview real do template"
          className="template-pdf-frame"
          src={pageUrl}
        />
      </div>
    </div>
  )
}
