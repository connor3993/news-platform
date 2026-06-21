import { ElNotification } from 'element-plus'

class NotifyWebSocket {
  constructor() {
    this.ws = null
    this.userId = null
    this.reconnectCount = 0
    this.maxReconnect = 5
    this.reconnectTimer = null
  }

  connect(userId) {
    if (!userId) return
    this.userId = userId
    this.reconnectCount = 0
    this.doConnect()
  }

  doConnect() {
    if (this.ws) {
      this.ws.close()
    }
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${window.location.host}/ws/notify/user/${this.userId}`
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      this.reconnectCount = 0
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        ElNotification({
          title: data.title || '消息通知',
          message: data.content || '',
          type: data.type?.includes('REJECT') ? 'warning' : 'info',
          duration: 5200
        })
      } catch (err) {
        console.error('WebSocket message parse error:', err)
      }
    }

    this.ws.onclose = () => {
      this.reconnect()
    }
  }

  reconnect() {
    if (!this.userId || this.reconnectCount >= this.maxReconnect) return
    this.reconnectCount += 1
    this.reconnectTimer = window.setTimeout(() => this.doConnect(), 3000)
  }

  disconnect() {
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.reconnectCount = this.maxReconnect
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.userId = null
  }
}

export default new NotifyWebSocket()
