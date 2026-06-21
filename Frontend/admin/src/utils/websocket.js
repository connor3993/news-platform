import { ElNotification } from 'element-plus'

class WebSocketService {
  constructor() {
    this.ws = null
    this.adminId = null
    this.reconnectCount = 0
    this.maxReconnect = 5
    this.reconnectTimer = null
    this.onMessage = null
    this.onConnect = null
    this.onDisconnect = null
  }

  connect(adminId) {
    this.adminId = adminId
    this.reconnectCount = 0
    this._doConnect()
  }

  _doConnect() {
    if (this.ws) {
      this.ws.close()
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws/audit/${this.adminId}`

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('WebSocket connected')
      this.reconnectCount = 0
      if (this.onConnect) this.onConnect()
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        ElNotification({
          title: data.title || '通知',
          message: data.content || '',
          type: data.type?.includes('REJECT') ? 'warning' : 'info',
          duration: 5000
        })
        if (this.onMessage) this.onMessage(data)
      } catch (e) {
        console.error('WebSocket message parse error:', e)
      }
    }

    this.ws.onclose = () => {
      console.log('WebSocket disconnected')
      if (this.onDisconnect) this.onDisconnect()
      this._reconnect()
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
  }

  _reconnect() {
    if (this.reconnectCount >= this.maxReconnect) {
      console.warn('WebSocket max reconnect reached')
      return
    }
    this.reconnectCount++
    console.log(`WebSocket reconnecting... (${this.reconnectCount}/${this.maxReconnect})`)
    this.reconnectTimer = setTimeout(() => {
      this._doConnect()
    }, 3000)
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.reconnectCount = this.maxReconnect
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.adminId = null
  }
}

export default new WebSocketService()
