// 极简提示：右下角黑框 toast，不依赖任何 UI 库
let container = null

function ensureContainer() {
  if (container) return container
  container = document.createElement('div')
  container.style.position = 'fixed'
  container.style.right = '12px'
  container.style.bottom = '12px'
  container.style.zIndex = '9999'
  container.style.display = 'flex'
  container.style.flexDirection = 'column'
  container.style.gap = '6px'
  document.body.appendChild(container)
  return container
}

export function toast(message, type = 'info') {
  const c = ensureContainer()
  const el = document.createElement('div')
  el.textContent = message
  el.style.border = '1px solid #000'
  el.style.background = type === 'error' ? '#000' : '#fff'
  el.style.color = type === 'error' ? '#fff' : '#000'
  el.style.padding = '6px 10px'
  el.style.fontSize = '13px'
  el.style.maxWidth = '320px'
  el.style.fontFamily = 'monospace, sans-serif'
  c.appendChild(el)
  setTimeout(() => el.remove(), 3000)
}

export default toast
