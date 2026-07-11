/**
 * 用户端 WebSocket 客户端
 * 连接 /ws/notify/user/{userId}，接收用户级实时通知（审核结果、稿件状态等）。
 */
import { toast } from '@/utils/message'

class UserWebSocketService {
  constructor() {
    this.ws = null
    this.userId = null
    this.reconnectCount = 0
    this.maxReconnect = 5
    this.reconnectTimer = null
    this.onMessage = null
  }

  connect(userId) {
    if (!userId) return
    this.userId = userId
    this.reconnectCount = 0
    this._doConnect()
  }

  _doConnect() {
    if (this.ws) {
      this.ws.close()
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws/notify/user/${this.userId}`

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('User WebSocket connected')
      this.reconnectCount = 0
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        toast(data.content || data.title || '收到新通知', data.type?.includes('REJECT') ? 'warning' : 'info')
        if (this.onMessage) this.onMessage(data)
      } catch (e) {
        console.error('User WebSocket message parse error:', e)
      }
    }

    this.ws.onclose = () => {
      console.log('User WebSocket disconnected')
      this._reconnect()
    }

    this.ws.onerror = (error) => {
      console.error('User WebSocket error:', error)
    }
  }

  _reconnect() {
    if (this.reconnectCount >= this.maxReconnect) {
      console.warn('User WebSocket max reconnect reached')
      return
    }
    this.reconnectCount++
    console.log(`User WebSocket reconnecting... (${this.reconnectCount}/${this.maxReconnect})`)
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
    this.userId = null
  }
}

export default new UserWebSocketService()
