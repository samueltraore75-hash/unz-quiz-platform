export default function Button({ variant="primary", size, block, round, children, ...props }) {
  let c = "btn";
  if (variant === "green")  c += " btn-green";
  else if (variant === "dark")   c += " btn-dark";
  else if (variant === "danger") c += " btn-danger";
  else if (variant === "blue")   c += " btn-blue";
  else                           c += " btn-ghost";
  if (size === "sm") c += " btn-sm";
  if (size === "lg") c += " btn-lg";
  if (size === "xl") c += " btn-xl";
  if (block) c += " btn-block";
  if (round) c += " btn-round";
  return <button className={c} {...props}>{children}</button>;
}
