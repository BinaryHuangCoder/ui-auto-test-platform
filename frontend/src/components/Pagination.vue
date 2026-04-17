<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ pageNum: 1, pageSize: 10 })
  },
  total: {
    type: Number,
    default: 0
  },
  pageSizes: {
    type: Array,
    default: () => [5, 10, 20, 50, 100]
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const currentPage = ref(props.modelValue.pageNum)
const currentPageSize = ref(props.modelValue.pageSize)

watch(() => props.modelValue, (val) => {
  currentPage.value = val.pageNum
  currentPageSize.value = val.pageSize
}, { deep: true })

const handleSizeChange = (val) => {
  currentPageSize.value = val
  emit('update:modelValue', { pageNum: currentPage.value, pageSize: val })
  emit('refresh')
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  emit('update:modelValue', { pageNum: val, pageSize: currentPageSize.value })
  emit('refresh')
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
