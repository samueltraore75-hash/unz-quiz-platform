export default function Chip({ tone="grey", mono, children }) {
  const map = { green:"chip-green", red:"chip-red", grey:"chip-grey", blue:"chip-blue", warn:"chip-warn" };
  return <span className={`chip ${map[tone]||"chip-grey"}${mono?" chip-mono":""}`}>{children}</span>;
}
