<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Legend,
  Tooltip,
  Title
} from 'chart.js'

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Legend,
  Tooltip,
  Title
)

interface SensorData {
  temperature: number
  humidity: number
  generationDate: string
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chart: Chart | null = null
let socket: WebSocket | null = null

function createEmptyChart(canvas: HTMLCanvasElement) {
  chart = new Chart(canvas, {
    type: 'line',
    data: {
      labels: [],
      datasets: [
        {
          label: 'Temperature (°C)',
          data: [],
          yAxisID: 'y',
          tension: 0.1,
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239,68,68,0.2)',

        },
        {
          label: 'Humidity',
          data: [],
          yAxisID: 'y',
          tension: 0.1,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59,130,246,0.2)'
        }
      ]
    },
    options: {
      responsive: true,
      animation: false, // important for live updates
      plugins: {
        title: {
          display: true,
          text: 'Live Sensor Readings'
        }
      },
      scales: {
        y: {
          display: true,
          type: 'linear',
          position: 'left',
          title: {
            display: true,
            text: 'Value'
          },
          min: 0,
          max: 40
        },
      }
    }
  })
}

onMounted(() => {
  if (!canvasRef.value) return

  // 1. Create chart immediately
  createEmptyChart(canvasRef.value)

  // 2. Open WebSocket
  socket = new WebSocket('ws://localhost:8080/sensor-data')

  socket.onmessage = (event) => {
    if (!chart) return

    const data: SensorData = JSON.parse(event.data)

    chart.data.labels?.push(data.generationDate)
    chart.data.datasets[0].data.push(data.temperature)
    chart.data.datasets[1].data.push(data.humidity)

    chart.update()
  }

  socket.onerror = (err) => {
    console.error('WebSocket error', err)
  }
})

onBeforeUnmount(() => {
  socket?.close()
  chart?.destroy()
})
</script>

<template>
  <div>
    <canvas ref="canvasRef"></canvas>
  </div>
</template>
