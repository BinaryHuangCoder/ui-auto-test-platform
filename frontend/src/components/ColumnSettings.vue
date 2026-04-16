<template>
  <div class="column-settings">
    <el-dropdown trigger="click" @command="handleCommand">
      <el-button type="primary" :icon="Setting">
        显示/隐藏列 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="column in columns"
            :key="column.prop"
            :command="column.prop"
          >
            <el-icon v-if="column.visible" style="color: #409eff;"><Select /></el-icon>
            <el-icon v-else style="opacity: 0.3;"><Select /></el-icon>
            <span style="margin-left: 8px;">{{ column.label }}</span>
          </el-dropdown-item>
          <el-dropdown-item divided command="reset">
            <el-icon><Refresh /></el-icon> 重置
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { Setting, ArrowDown, Refresh, Select } from '@element-plus/icons-vue'

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  storageKey: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:columns'])

// 从 localStorage 加载列设置
const loadColumnSettings = () => {
  try {
    const saved = localStorage.getItem(props.storageKey)
    if (saved) {
      const settings = JSON.parse(saved)
      props.columns.forEach(column => {
        if (settings.hasOwnProperty(column.prop)) {
          column.visible = settings[column.prop]
        }
      })
    }
  } catch (e) {
    console.error('加载列设置失败:', e)
  }
}

// 保存列设置到 localStorage
const saveColumnSettings = () => {
  try {
    const settings = {}
    props.columns.forEach(column => {
      settings[column.prop] = column.visible
    })
    localStorage.setItem(props.storageKey, JSON.stringify(settings))
  } catch (e) {
    console.error('保存列设置失败:', e)
  }
}

// 处理列设置命令
const handleCommand = (command) => {
  if (command === 'reset') {
    // 重置所有列为可见
    props.columns.forEach(column => {
      column.visible = true
    })
    saveColumnSettings()
  } else {
    // 切换列的可见性
    const column = props.columns.find(c => c.prop === command)
    if (column) {
      column.visible = !column.visible
      saveColumnSettings()
    }
  }
  emit('update:columns', [...props.columns])
}

// 初始化时加载设置
loadColumnSettings()
</script>

<style scoped>
.column-settings {
  display: inline-block;
}
</style>
