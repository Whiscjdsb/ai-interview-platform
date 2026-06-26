<template>
  <div class="stat-card">
    <div class="stat-card__label">{{ label }}</div>
    <div class="stat-card__value">{{ value }}</div>
    <div v-if="hint" class="stat-card__hint">{{ hint }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  hint?: string
}>()
</script>

<style scoped>
.stat-card {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
  min-height: 104px;
}

.stat-card__label {
  color: #62748e;
  font-size: 13px;
  margin-bottom: 10px;
}

.stat-card__value {
  color: #1f2933;
  font-size: 26px;
  font-weight: 750;
  line-height: 1.2;
}

.stat-card__hint {
  color: #16a34a;
  font-size: 12px;
  margin-top: 8px;
}
</style>
