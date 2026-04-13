<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试用例</span>
        </div>
      </template>
      
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="goToAdd">新建用例</el-button>
          <el-button type="success" icon="VideoPlay" :disabled="!selectedIds.length" @click="batchRun">批量执行</el-button>
          <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <el-upload
            :action="importUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImportSuccess"
            :on-error="handleImportError"
            accept=".xlsx,.xls"
          >
            <el-button icon="Upload">导入</el-button>
          </el-upload>
          <el-button icon="Download" @click="handleExport">导出</el-button>
          <el-input 
            v-model="keyword" 
            placeholder="搜索用例名称/编号/设计者" 
            style="width: 220px;"
            @keyup.enter="search"
            clearable
            @clear="search"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      
      <el-table 
        :data="tableData" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="caseNo" label="用例编号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="name" label="用例名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="designer" label="设计者" width="90">
          <template #default="scope">
            {{ scope.row.designerNickname || scope.row.designer || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="caseType" label="用例性质" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.caseType === 'positive' ? 'success' : 'warning'" size="small">
              {{ scope.row.caseType === 'positive' ? '正例' : '反例' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <el-button size="small" type="primary">
                操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="run">
                    <el-icon><VideoPlay /></el-icon> 执行用例
                  </el-dropdown-item>
                  <el-dropdown-item command="execution">
                    <el-icon><List /></el-icon> 执行记录
                  </el-dropdown-item>
                  <el-dropdown-item command="edit" divided>
                    <el-icon><Edit /></el-icon> 编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete">
                    <el-icon><Delete /></el-icon> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper, refresh"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 执行记录对话框 -->
    <el-dialog v-model="executionVisible" title="执行记录" width="90%">
      <template #header>
        <div class="dialog-header">
          <span>执行记录</span>
          <el-tag v-if="isRefreshing" type="warning" size="small">
            <el-icon class="is-loading"><Loading /></el-icon> 自动刷新中
          </el-tag>
        </div>
      </template>
      
      <el-table 
        :data="executionList" 
        style="width: 100%"
        @row-click="showStepExecutions"
        :row-class-name="tableRowClassName"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="caseName" label="用例名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="executor" label="执行人" width="90">
          <template #default="scope">
            {{ scope.row.executorNickname || scope.row.executor || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="scope">
            {{ scope.row.duration ? (scope.row.duration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiTotalTokenUsed" width="150">
          <template #header>
            <span>AI token消耗
              <el-tooltip content="估算规则：中文每2字符≈1 token，英文每4字符≈1 token" placement="top">
                <el-icon style="margin-left: 4px; cursor: pointer; color: var(--el-color-info);"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="scope">
            {{ scope.row.aiTotalTokenUsed || 0 }}
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="execPageNum"
          v-model:page-size="execPageSize"
          :total="execTotal"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper, refresh"
          @size-change="loadExecutions"
          @current-change="loadExecutions"
        />
      </div>
      
      <el-divider>步骤执行记录</el-divider>
      
      <el-table :data="stepExecutionList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column prop="stepNo" label="步骤号" width="70" />
        <el-table-column prop="stepDescription" label="步骤描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="scope">
            {{ scope.row.duration ? (scope.row.duration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="执行状态" width="90">
          <template #default="scope">
            <el-tag :type="getStepStatusType(scope.row.status)" size="small">
              {{ getStepStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assertionStatus" label="断言状态" width="100">
          <template #default="scope">
            <el-tag :type="getAssertionStatusType(scope.row.assertionStatus)" size="small">
              {{ getAssertionStatusText(scope.row.assertionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiResult" label="AI断言描述" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.aiResult" :class="{'assertion-success': scope.row.aiResult.includes('✅'), 'assertion-failed': scope.row.aiResult.includes('❌'), 'assertion-warning': scope.row.aiResult.includes('⚠️')}">
              {{ scope.row.aiResult }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="aiTokenUsed" width="150">
          <template #header>
            <span>AI token消耗
              <el-tooltip content="估算规则：中文每2字符≈1 token，英文每4字符≈1 token" placement="top">
                <el-icon style="margin-left: 4px; cursor: pointer; color: var(--el-color-info);"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="scope">
            {{ scope.row.aiTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="截图" width="70">
          <template #default="scope">
            <el-button v-if="scope.row.screenshot" size="small" @click.stop="showScreenshot(scope.row.screenshot)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="stepExecPageNum"
          v-model:page-size="stepExecPageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          :total="stepExecTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadStepExecutions"
          @current-change="loadStepExecutions"
        />
      </div>
    </el-dialog>
    
    <!-- 截图预览对话框 -->
    <el-dialog v-model="screenshotVisible" title="执行截图" width="800px">
      <img v-if="currentScreenshot" :src="currentScreenshot" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, QuestionFilled } from '@element-plus/icons-vue'
import { getCaseList, deleteCase, batchDeleteCase, updateCase } from '@/api/testCase'
import { runCase as apiRunCase, runBatchCase, getExecutionList, getStepExecutions } from '@/api/testCaseExecution'

const router = useRouter()
const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const tableData = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

const executionVisible = ref(false)
const executionList = ref([])
const stepExecutionList = ref([])
const stepExecPageNum = ref(1)
const stepExecPageSize = ref(10)
const stepExecTotal = ref(0)
const execPageNum = ref(1)
const execPageSize = ref(10)
const execTotal = ref(0)
const currentExecutionId = ref(null)
const screenshotVisible = ref(false)
const currentScreenshot = ref('')
const activeExecutionRow = ref(null)
const refreshTimer = ref(null)
const isRefreshing = ref(false)

const importUrl = computed(() => baseURL + '/api/case/import')
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('token')
}))

/**
 * 组件挂载时初始化数据
 */
onMounted(() => {
  loadData()
})

/**
 * 组件卸载时清理定时器
 */
onUnmounted(() => {
  stopAutoRefresh()
})

/**
 * 启动自动刷新（每5秒刷新一次）
 */
const startAutoRefresh = () => {
  stopAutoRefresh()
  isRefreshing.value = true
  refreshTimer.value = setInterval(() => {
    if (executionVisible.value) {
      loadExecutions()
      if (activeExecutionRow.value) {
        loadStepExecutions()
      }
    }
  }, 5000)
}

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
  isRefreshing.value = false
}

/**
 * 加载用例列表数据
 */
const loadData = async () => {
  try {
    const res = await getCaseList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadData()
}

const search = () => {
  pageNum.value = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const goToAdd = () => {
  router.push('/test/case-detail')
}

const goToEdit = (row) => {
  router.push({ path: '/test/case-detail', query: { id: row.id } })
}

const handleStatusChange = async (row) => {
  try {
    await updateCase({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用例吗？', '提示', { type: 'warning' })
    await deleteCase(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的 ' + selectedIds.value.length + ' 个用例吗？', '提示', { type: 'warning' })
    await batchDeleteCase(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleCommand = async (cmd, row) => {
  switch (cmd) {
    case 'run':
      await executeCase(row)
      break
    case 'execution':
      await showExecutions(row)
      break
    case 'edit':
      goToEdit(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handleExport = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(baseURL + '/api/case/export', {
      headers: { Authorization: 'Bearer ' + token }
    })
    if (!response.ok) throw new Error('Export failed')
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '测试用例_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleImportSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success(response.message || '导入成功')
    loadData()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleImportError = () => {
  ElMessage.error('导入失败')
}

const executeCase = async (row) => {
  try {
    await ElMessageBox.confirm('确定要执行该用例吗？', '提示', { type: 'info' })
    const res = await apiRunCase(row.id)
    if (res.code === 200) {
      ElMessage.success('用例已开始执行')
      // 执行后立即打开执行记录页面并启动自动刷新
      await showExecutions(row)
    } else {
      ElMessage.error(res.message || '执行失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('执行失败')
  }
}

const showExecutions = async (row) => {
  currentExecutionId.value = row.id
  executionVisible.value = true
  // 打开执行记录对话框时启动自动刷新
  startAutoRefresh()
  await loadExecutions()
}

const loadExecutions = async () => {
  try {
    const res = await getExecutionList({
      pageNum: execPageNum.value,
      pageSize: execPageSize.value,
      caseId: currentExecutionId.value
    })
    if (res.code === 200) {
      executionList.value = res.data.records || []
      execTotal.value = res.data.total || 0
      // 自动选中第一条执行记录
      if (executionList.value.length > 0) {
        if (!activeExecutionRow.value || activeExecutionRow.value !== executionList.value[0].id) {
          await showStepExecutions(executionList.value[0])
        }
      }
    }
  } catch (error) {
    console.error('加载执行记录失败:', error)
  }
}

const showStepExecutions = async (row) => {
  activeExecutionRow.value = row.id
  try {
    const res = await getStepExecutions(row.id, stepExecPageNum.value, stepExecPageSize.value)
    if (res.code === 200) {
      // 后端返回数组，不是分页对象
      stepExecutionList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
      stepExecTotal.value = stepExecutionList.value.length
    }
  } catch (error) {
    ElMessage.error('加载步骤执行记录失败')
  }
}

const loadStepExecutions = async () => {
  if (!activeExecutionRow.value) return
  try {
    const res = await getStepExecutions(activeExecutionRow.value, stepExecPageNum.value, stepExecPageSize.value)
    if (res.code === 200) {
      // 后端返回数组，不是分页对象
      stepExecutionList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
      stepExecTotal.value = stepExecutionList.value.length
    }
  } catch (error) {
    ElMessage.error("加载步骤执行记录失败")
  }
}

const tableRowClassName = ({ row }) => {
  return row.id === activeExecutionRow.value ? 'success-row' : ''
}

const showScreenshot = (screenshot) => {
  // 检查是否是默认的占位图（绿色小方块）
  if (!screenshot || screenshot.includes("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")) {
    ElMessage.warning("暂无截图")
    return
  }
  currentScreenshot.value = screenshot
  screenshotVisible.value = true
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.getFullYear() + '-' + 
    String(date.getMonth() + 1).padStart(2, '0') + '-' + 
    String(date.getDate()).padStart(2, '0') + ' ' + 
    String(date.getHours()).padStart(2, '0') + ':' + 
    String(date.getMinutes()).padStart(2, '0') + ':' + 
    String(date.getSeconds()).padStart(2, '0')
}

/**
 * 获取执行状态对应的tag类型
 * @param status 状态值
 * @returns tag类型
 */
const getStatusType = (status) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  if (status === 'running') return 'warning'
  return 'info'
}

/**
 * 获取执行状态显示文本
 * @param status 状态值
 * @returns 状态文本
 */
const getStatusText = (status) => {
  if (status === 'success') return '执行成功'
  if (status === 'failed') return '执行失败'
  if (status === 'running') return '执行中'
  return status || '-'
}

/**
 * 获取步骤执行状态对应的tag类型
 * @param status 状态值
 * @returns tag类型
 */
const getStepStatusType = (status) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  if (status === 'pending') return 'info'
  return 'info'
}

/**
 * 获取步骤执行状态显示文本
 * @param status 状态值
 * @returns 状态文本
 */
const getStepStatusText = (status) => {
  if (status === 'success') return '成功'
  if (status === 'failed') return '失败'
  if (status === 'pending') return '未执行'
  return status || '-'
}

/**
 * 获取断言状态对应的tag类型
 * @param status 状态值
 * @returns tag类型
 */
const getAssertionStatusType = (status) => {
  if (status === 'success') return 'success'
  if (status === 'none') return 'info'
  if (status === 'failed') return 'danger'
  return 'info'
}

/**
 * 获取断言状态显示文本
 * @param status 状态值
 * @returns 状态文本
 */
const getAssertionStatusText = (status) => {
  if (status === 'success') return '断言成功'
  if (status === 'none') return '无'
  if (status === 'failed') return '断言失败'
  return status || '-'
}
</script>

<style scoped lang="scss">
.common-page {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    flex-wrap: wrap;
    gap: 10px;
    
    .toolbar-left, .toolbar-right {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-wrap: wrap;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

:deep(.el-table .success-row) {
  background-color: #f0f9eb;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-table) {
  width: 100% !important;
}

// AI断言描述样式
.assertion-success {
  color: #67c23a;
  font-weight: 500;
}

.assertion-failed {
  color: #f56c6c;
  font-weight: 500;
}

.assertion-warning {
  color: #e6a23c;
  font-weight: 500;
}

.text-muted {
  color: #909399;
}
</style>
