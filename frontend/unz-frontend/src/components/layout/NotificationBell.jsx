import { useEffect, useRef, useState } from "react";
import { notificationsApi } from "../../api/notifications";

function timeAgo(iso) {
  const d = Math.floor((Date.now() - new Date(iso)) / 60000);
  if (d < 1) return "à l'instant";
  if (d < 60) return `il y a ${d} min`;
  if (d < 1440) return `il y a ${Math.floor(d/60)} h`;
  return `il y a ${Math.floor(d/1440)} j`;
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [count, setCount] = useState(0);
  const [items, setItems] = useState(null);
  const ref = useRef(null);

  useEffect(() => {
    notificationsApi.count().then(r => setCount(r.nonLues)).catch(() => {});
    const id = setInterval(() => notificationsApi.count().then(r => setCount(r.nonLues)).catch(() => {}), 60000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    const close = e => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  async function toggle() {
    const next = !open;
    setOpen(next);
    if (next && items === null) {
      const list = await notificationsApi.list().catch(() => []);
      setItems(list);
    }
  }

  async function markRead(id) {
    await notificationsApi.lire(id).catch(() => {});
    setItems(prev => prev?.map(n => n.id === id ? {...n, lue:true} : n));
    setCount(c => Math.max(0, c - 1));
  }

  async function markAll() {
    await notificationsApi.lireTout().catch(() => {});
    setItems(prev => prev?.map(n => ({...n, lue:true})));
    setCount(0);
  }

  return (
    <div className="notif-bell-wrap" ref={ref} style={{ position:"relative" }}>
      <button className="notif-bell" onClick={toggle} aria-label="Notifications">
        <i className="ti ti-bell" style={{ fontSize:17 }} aria-hidden="true" />
        {count > 0 && <span className="notif-badge">{count > 9 ? "9+" : count}</span>}
      </button>
      {open && (
        <div className="notif-panel">
          <div className="notif-hdr">
            <span>Notifications</span>
            {count > 0 && <button className="notif-mark" onClick={markAll}>Tout marquer lu</button>}
          </div>
          {items === null && <p className="notif-empty">Chargement…</p>}
          {items?.length === 0 && <p className="notif-empty">Aucune notification.</p>}
          {items?.map(n => (
            <button key={n.id} className={`notif-item ${n.lue ? "" : "unread"}`} onClick={() => !n.lue && markRead(n.id)}>
              <p className="notif-item-t">{n.titre}</p>
              <p className="notif-item-m">{n.message}</p>
              <p className="notif-item-d">{timeAgo(n.createdAt)}</p>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
