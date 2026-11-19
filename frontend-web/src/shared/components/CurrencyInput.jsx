import { maskCurrency } from '../../utils/masks'

export default function CurrencyInput({ value, onChange, placeholder = '0,00', disabled = false, required = false }) {
  return (
    <div className={`currency-input ${disabled ? 'disabled' : ''}`}>
      <span>R$</span>
      <input
        className="form-input"
        inputMode="decimal"
        placeholder={placeholder}
        value={value}
        onChange={event => onChange(maskCurrency(event.target.value))}
        disabled={disabled}
        required={required}
      />
    </div>
  )
}
