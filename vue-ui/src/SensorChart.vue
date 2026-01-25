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
  Title,
  Colors
} from 'chart.js'

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Legend,
  Tooltip,
  Title,
  Colors
)

interface SensorData {
  temperature: number
  humidity: number
  generationDate: string
}

const selectedSensor = ref<number>(1)
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
      animation: false,
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
            text: 'Values'
          },
          min: 0,
          max: 40
        },
        x: {
          display: true,
          title: {
            display: true,
            text: 'Time (Minutes)'
          },
        }
      }
    }
  })
}

const currentMinuteData = {
  minute: null,
  tempSum: 0,
  humSum: 0,
  count: 0
}

onMounted(() => {
  if (!canvasRef.value) return

  // 1. Create chart immediately
  createEmptyChart(canvasRef.value)

  // 2. Open WebSocket
  socket = new WebSocket('ws://localhost:8080/sensor-data/' + selectedSensor.value)


  socket.onmessage = (event) => {
    if (!chart) return;

    const data = JSON.parse(event.data);
    const date = new Date(data.generationDate);
    const minute = date.getMinutes();

    // Check if we are still in the same minute
    if (currentMinuteData.minute === minute) {
      // 1. Update existing bucket
      currentMinuteData.tempSum += data.temperature;
      currentMinuteData.humSum += data.humidity;
      currentMinuteData.count++;

      const lastIdx = chart.data.labels.length - 1;

      // 2. Calculate new averages
      chart.data.datasets[0].data[lastIdx] = currentMinuteData.tempSum / currentMinuteData.count;
      chart.data.datasets[1].data[lastIdx] = currentMinuteData.humSum / currentMinuteData.count;
    } else {
      // 1. New minute detected: Reset counters
      currentMinuteData.minute = minute;
      currentMinuteData.tempSum = data.temperature;
      currentMinuteData.humSum = data.humidity;
      currentMinuteData.count = 1;

      // 2. Push new points to the chart
      chart.data.labels.push(minute);
      chart.data.datasets[0].data.push(data.temperature);
      chart.data.datasets[1].data.push(data.humidity);

      // Optional: Keep only the last 60 minutes on the chart
      if (chart.data.labels.length > 60) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
        chart.data.datasets[1].data.shift();
      }
    }

    // Use 'none' mode for better performance during high-frequency updates
    chart.update('none');
  }

  socket.onerror = (err) => {
    console.error('WebSocket error', err)
  }
})

onBeforeUnmount(() => {
  socket?.close()
  chart?.destroy()
})

function clearData() {
  if (!chart) return;

  // 1. Empty the chart arrays
  chart.data.labels = [];
  chart.data.datasets[0].data = [];
  chart.data.datasets[1].data = [];
  chart.update();

  // 2. Reset your averaging bucket
  currentMinuteData.minute = null;
  currentMinuteData.count = 0;
  currentMinuteData.tempSum = 0;
  currentMinuteData.humSum = 0;
}
</script>

<template>
  <div id="chartContainer">

    <div id="selector">
      <h3>Select a sensor</h3>
      <select name="select-sensor" v-model="selectedSensor" @change="clearData()" id="sensor-selector">
        <option value="1" selected>Sensor 1</option>
        <option value="2">Sensor 2</option>
      </select>
    </div>

    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<style scoped>
#selector {
  display: flex;
  gap: 0.5rem;
}

#chartContainer {
  padding: 1rem;
  border-style: inset;
  border-width: 3px;
  border-radius: 1rem;
  border-color: black;
}
</style>
