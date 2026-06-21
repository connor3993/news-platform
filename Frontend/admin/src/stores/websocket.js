import { defineStore } from 'pinia'
import { ref } from 'vue'
import wsService from '@/utils/websocket'

export const useWebSocketStore = defineStore('websocket', () => {
  const connected = ref(false)
  const messageList = ref([])
  const reconnectCount = ref(0)

  function connect(adminId) {
    wsService.onConnect = () => {
      connected.value = true
      reconnectCount.value = 0
    }
    wsService.onDisconnect = () => {
      connected.value = false
      reconnectCount.value = wsService.reconnectCount
    }
    wsService.onMessage = (data) => {
      pushMessage(data)
    }
    wsService.connect(adminId)
  }

  function disconnect() {
    wsService.disconnect()
    connected.value = false
    messageList.value = []
  }

  function pushMessage(msg) {
    messageList.value.unshift({
      ...msg,
      time: msg.time || new Date().toLocaleString()
    })
    if (messageList.value.length > 50) {
      messageList.value.pop()
    }
  }

  return {
    connected,
    messageList,
    reconnectCount,
    connect,
    disconnect,
    pushMessage
  }
})
