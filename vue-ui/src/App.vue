<script setup lang="ts">
import { ref } from 'vue'
import SensorChart from './SensorChart.vue';
const number = ref<number>(0)
number.value = 2
</script>

<template>
  <div class="container">
    <h1 class="text-center">Sensor Simulator Data</h1>
    <SensorChart />
  </div>
</template>

<style scoped>
h1 {
  text-align: center;
  margin-top: 2rem;
}
</style>
