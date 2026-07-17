export default function Select({ label, options = [], placeholder = "Sélectionner…", ...props }) {
  return (
    <div className="field">
      {label && <label className="field-label">{label}</label>}
      <select className="field-select" {...props}>
        <option value="">{placeholder}</option>
        {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </div>
  );
}
