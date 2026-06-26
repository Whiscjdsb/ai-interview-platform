<template>
  <div class="pagination-row">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @update:current-page="$emit('update:page', $event)"
      @update:page-size="$emit('update:size', $event)"
      @size-change="$emit('change')"
      @current-change="$emit('change')"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ page: number; size: number; total: number }>()
defineEmits<{
  'update:page': [value: number]
  'update:size': [value: number]
  change: []
}>()
</script>

<style scoped>
.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
  overflow-x: auto;
}
</style>
