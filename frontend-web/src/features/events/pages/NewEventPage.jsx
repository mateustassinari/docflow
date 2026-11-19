import { useNavigate } from 'react-router-dom'
import TopBar from '../../../layouts/TopBar'
import CurrencyInput from '../../../shared/components/CurrencyInput'
import { User, Calendar, MapPin, StickyNote, ChevronDown } from 'lucide-react'
import { useNewEventPage } from '../hooks/useNewEventPage'

const SERVICOS = [
  { value: 'PINTURA_AO_VIVO', label: 'Pintura ao Vivo' },
  { value: 'AQUARELA_AO_VIVO', label: 'Aquarela ao Vivo' },
  { value: 'ENCOMENDA_ARTISTICA', label: 'Encomenda artística personalizada' },
  { value: 'IDENTIDADE_VISUAL', label: 'Identidade visual' },
]
const PRODUTOS_ENCOMENDA = ['Pintura artística', 'Bordado artístico', 'Retrato personalizado', 'Peça decorativa artesanal', 'Papelaria artística', 'Porta aliança']
const TIPOS = ['Corporativo', 'Casamento', 'Social', 'Conferência', 'Workshop', 'Jantar Executivo']

export default function NovoEvento() {
  const navigate = useNavigate()
  const {
    clientes,
    form,
    handleSubmit,
    isNewClient,
    loading,
    selectedClientId,
    set,
    setClienteCpf,
    setClienteTelefone,
    setChecked,
    setCurrency,
    setIsNewClient,
    setSelectedClientId,
    showEventoAoVivoFields,
    toast,
  } = useNewEventPage({
    onCreated: event => navigate(`/eventos/${event.id}`),
  })

  return (
    <>
      <TopBar breadcrumb={[{ label: 'Eventos', href: '/eventos' }]} title="Novo Evento" />
      <div className="page-content">
        <div className="page-header">
          <div className="page-header-left">
            <h1>Configuração de Evento</h1>
            <p>Insira as informações básicas para iniciar o planejamento logístico.</p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="card">
            <div className="form-section">
              <div className="form-section-title client-section-title">
                <div className="section-title-content">
                  <User size={17} />
                  Cliente
                </div>
                <div className="radio-toggle-group">
                  <label className="radio-option">
                    <input type="radio" checked={!isNewClient} onChange={() => setIsNewClient(false)} />
                    Selecionar existente
                  </label>
                  <label className="radio-option">
                    <input type="radio" checked={isNewClient} onChange={() => setIsNewClient(true)} />
                    Cadastrar novo
                  </label>
                </div>
              </div>

              {!isNewClient ? (
                <div className="form-group full form-field-spaced">
                  <label className="form-label">Selecione o cliente</label>
                  <div className="select-with-icon">
                    <select
                      value={selectedClientId}
                      onChange={(e) => setSelectedClientId(e.target.value)}
                      className="form-select select-with-icon-input"
                      required={!isNewClient}
                    >
                      <option value="" disabled>Selecione um cliente</option>
                      {clientes.map(c => <option key={c.id} value={c.id}>{c.nome} ({c.cpfCnpj || 'Sem documento'})</option>)}
                    </select>
                    <ChevronDown size={15} className="select-with-icon-chevron" />
                  </div>
                </div>
              ) : (
                <>
                  <div className="form-grid-2 form-field-spaced">
                    <div className="form-group full">
                      <label className="form-label">Nome completo</label>
                      <input className="form-input" placeholder="Ex: Roberto Carlos" value={form.clienteNome} onChange={set('clienteNome')} required={isNewClient} />
                    </div>
                  </div>
                  <div className="form-grid-2">
                    <div className="form-group">
                      <label className="form-label">E-mail</label>
                      <input type="email" className="form-input" placeholder="roberto@email.com" value={form.clienteEmail} onChange={set('clienteEmail')} required={isNewClient} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Telefone</label>
                      <input className="form-input" placeholder="(11) 99999-9999" value={form.clienteTelefone} onChange={setClienteTelefone} required={isNewClient} />
                    </div>
                  </div>
                  <div className="form-group full form-field-top">
                    <label className="form-label">CPF / CNPJ</label>
                    <input className="form-input" placeholder="000.000.000-00" value={form.clienteCpf} onChange={setClienteCpf} required={isNewClient} />
                  </div>
                </>
              )}
            </div>

            <div className="form-section">
              <div className="form-section-title">
                <Calendar size={17} />
                Evento
              </div>
              <div className="form-grid-2 form-field-spaced">
                <div className="form-group">
                  <label className="form-label">Serviço contratado</label>
                  <div className="select-with-icon">
                    <select className="form-select select-with-icon-input" value={form.tipoServico} onChange={set('tipoServico')}>
                      {SERVICOS.map(opcaoServico => <option key={opcaoServico.value} value={opcaoServico.value}>{opcaoServico.label}</option>)}
                    </select>
                    <ChevronDown size={15} className="select-with-icon-chevron" />
                  </div>
                </div>
                {showEventoAoVivoFields && (
                  <>
                    <div className="form-group">
                      <label className="form-label">Tipo de evento</label>
                      <div className="select-with-icon">
                        <select className="form-select select-with-icon-input" value={form.tipoEvento} onChange={set('tipoEvento')}>
                          {TIPOS.map(t => <option key={t}>{t}</option>)}
                        </select>
                        <ChevronDown size={15} className="select-with-icon-chevron" />
                      </div>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Data prevista</label>
                      <input type="date" className="form-input" value={form.data} onChange={set('data')} required />
                    </div>
                  </>
                )}
              </div>
              {showEventoAoVivoFields && <div className="form-grid-3 form-field-spaced">
                <div className="form-group">
                  <label className="form-label">Horário de início</label>
                  <input type="time" className="form-input" value={form.horarioInicio} onChange={set('horarioInicio')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Duração estimada</label>
                  <input className="form-input" placeholder="Ex: 4 horas" value={form.duracaoEstimada} onChange={set('duracaoEstimada')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Número de convidados</label>
                  <input type="number" min="0" className="form-input" placeholder="Ex: 120" value={form.numeroConvidados} onChange={set('numeroConvidados')} />
                </div>
              </div>}
              {showEventoAoVivoFields && <div className="form-group form-field-spaced">
                <label className="form-label">Local</label>
                <div className="form-input-icon">
                  <MapPin />
                  <input className="form-input" placeholder="Endereço ou nome do espaço" value={form.local} onChange={set('local')} required />
                </div>
              </div>}
              <div className="form-grid-2">
                <div className="form-group">
                  <label className="form-label">Valor total</label>
                  <CurrencyInput value={form.valor} onChange={setCurrency('valor')} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Valor sinal</label>
                  <CurrencyInput value={form.valorSinal} onChange={setCurrency('valorSinal')} />
                </div>
              </div>
              <div className="form-grid-2 form-field-top">
                <div className="form-group">
                  <label className="form-label">Forma de pagamento sinal</label>
                  <input className="form-input" placeholder="Ex: Pix no ato da assinatura" value={form.formaPagamentoSinal} onChange={set('formaPagamentoSinal')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Forma de pagamento restante</label>
                  <input className="form-input" placeholder="Ex: Pix no dia do evento" value={form.formaPagamentoRestante} onChange={set('formaPagamentoRestante')} />
                </div>
              </div>
            </div>

            {showEventoAoVivoFields && <div className="form-section">
              <div className="form-section-title">
                <StickyNote size={17} />
                Obra
              </div>
              <div className="form-grid-2 form-field-spaced">
                <div className="form-group">
                  <label className="form-label">Técnica</label>
                  <input className="form-input" placeholder="Ex: Aquarela, acrílica, live painting" value={form.tecnica} onChange={set('tecnica')} />
                </div>
                <div className="form-group">
                  <label className="form-label">Tamanho da tela</label>
                  <input className="form-input" placeholder="Ex: 60x80 cm" value={form.tamanhoTela} onChange={set('tamanhoTela')} />
                </div>
              </div>
              <div className="form-group form-field-spaced">
                <label className="form-label">Cena retratada</label>
                <textarea className="form-textarea" placeholder="Ex: primeira dança, cerimônia, recepção..." value={form.cenaRetratada} onChange={set('cenaRetratada')} rows={3} />
              </div>
              <div className="form-group">
                <label className="form-label">Entrega da obra</label>
                <input className="form-input" placeholder="Ex: ao final do evento ou em até 30 dias" value={form.entregaObra} onChange={set('entregaObra')} />
              </div>
            </div>}

            {form.tipoServico === 'ENCOMENDA_ARTISTICA' && (
              <div className="form-section">
                <div className="form-section-title">
                  <StickyNote size={17} />
                  Encomenda artística
                </div>
                <div className="form-grid-2 form-field-spaced">
                  <div className="form-group">
                    <label className="form-label">Tipo de produto</label>
                    <select className="form-select" value={form.tipoProduto} onChange={set('tipoProduto')}>
                      {PRODUTOS_ENCOMENDA.map(produto => <option key={produto}>{produto}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Dimensão</label>
                    <input className="form-input" value={form.dimensao} onChange={set('dimensao')} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Técnica</label>
                    <input className="form-input" value={form.tecnica} onChange={set('tecnica')} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Prazo estimado</label>
                    <input className="form-input" value={form.prazoEstimado} onChange={set('prazoEstimado')} />
                  </div>
                </div>
                <div className="form-group form-field-spaced">
                  <label className="form-label">Tema/referência</label>
                  <textarea className="form-textarea" rows={3} value={form.temaReferencia} onChange={set('temaReferencia')} />
                </div>
                <div className="form-grid-2">
                  <div className="form-group">
                    <label className="form-label">Forma de entrega</label>
                    <select className="form-select" value={form.formaEntrega} onChange={set('formaEntrega')}>
                      <option value="ENVIO">Envio</option>
                      <option value="RETIRADA">Retirada</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">{form.formaEntrega === 'RETIRADA' ? 'Local de retirada' : 'Endereço de envio'}</label>
                    <input className="form-input" value={form.enderecoEnvio} onChange={set('enderecoEnvio')} />
                  </div>
                  {form.formaEntrega === 'ENVIO' && (
                  <div className="form-group">
                    <label className="form-label">Frete/responsável pelo envio</label>
                    <input className="form-input" value={form.freteResponsavel} onChange={set('freteResponsavel')} />
                  </div>
                  )}
                </div>
              </div>
            )}

            {form.tipoServico === 'IDENTIDADE_VISUAL' && (
              <div className="form-section">
                <div className="form-section-title">
                  <StickyNote size={17} />
                  Identidade visual
                </div>
                <div className="form-grid-2 form-field-spaced">
                  <div className="form-group">
                    <label className="form-label">Prazo em dias úteis</label>
                    <input type="number" min="0" className="form-input" value={form.prazoDiasUteis} onChange={set('prazoDiasUteis')} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Rodadas de ajustes</label>
                    <input type="number" min="0" className="form-input" value={form.rodadasAjustes} onChange={set('rodadasAjustes')} />
                  </div>
                </div>
              </div>
            )}

            <div className="form-section form-section-last">
              <div className="form-section-title">
                <StickyNote size={17} />
                Observações
              </div>
              <div className="form-group">
                <textarea
                  className="form-textarea"
                  placeholder="Detalhes adicionais, restrições ou pedidos especiais..."
                  value={form.observacoes}
                  onChange={set('observacoes')}
                  rows={4}
                />
              </div>
              <label className="form-group" style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '8px', marginTop: '8px', marginBottom: 0 }}>
                <input type="checkbox" checked={form.contratoDispensado} onChange={setChecked('contratoDispensado')} />
                <span className="form-label" style={{ marginBottom: 0 }}>
                  Este evento não terá contrato (ex: evento antigo cadastrado retroativamente)
                </span>
              </label>
            </div>
          </div>

          <div className="form-footer">
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/eventos')}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner" /> : null}
              {loading ? 'Salvando...' : 'Salvar Evento'}
            </button>
          </div>
        </form>
      </div>

      {toast && <div className={`toast ${toast.type}`}>{toast.msg}</div>}
    </>
  )
}
