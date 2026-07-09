import { useEffect, useState } from "react";
export default function Timer({ dateDebut, dureeMinutes, onExpire }) {
  const fin = new Date(dateDebut).getTime() + dureeMinutes * 60000;
  const calc = () => Math.max(0, Math.round((fin - Date.now()) / 1000));
  const [rem, setRem] = useState(calc);
  useEffect(() => {
    if (rem <= 0) { onExpire?.(); return; }
    const id = setInterval(() => setRem(calc()), 1000);
    return () => clearInterval(id);
  }, [rem]);
  const m = String(Math.floor(rem / 60)).padStart(2, "0");
  const s = String(rem % 60).padStart(2, "0");
  const tone = rem < 60 ? "timer-red" : rem < 300 ? "timer-warn" : "timer-ok";
  return <span className={`timer ${tone}`}><i className="ti ti-clock" style={{fontSize:12,marginRight:4}} aria-hidden="true"/>{m}:{s}</span>;
}
