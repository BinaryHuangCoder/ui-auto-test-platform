<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>执行记录</span>
        </div>
      </template>
      
      <el-table 
        :data="tableData" 
        style="width: 100%"
        @row-click="showDetail"
      >
        <el-table-column prop="caseName" label="用例名称" min-width="150" />
        <el-table-column prop="executor" label="执行人" width="100">
          <template #default="scope">
            {{ scope.row.executorNickname || scope.row.executor || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="showDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="scope">
            {{ (scope.row.duration / 1000).toFixed(2) }}s
          </template>
        </el-table-column>
        <el-table-column prop="aiTotalTokenUsed" width="150">
          <template #header>
            <span>AI token消耗
              <el-tooltip content="数据来自Midscene AI调用的真实token消耗统计" placement="top">
                <el-icon style="margin-left: 4px; cursor: pointer; color: var(--el-color-info);"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="scope">
            {{ scope.row.aiTotalTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
    
    <!-- 步骤执行记录区域 -->
    <el-card v-if="currentExecution" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>步骤执行记录 - {{ currentExecution.caseName }}</span>
          <el-tag v-if="isRefreshing" type="warning" size="small">
            <el-icon class="is-loading"><Loading /></el-icon> 自动刷新中
          </el-tag>
        </div>
      </template>
      
      <el-table :data="stepList" style="width: 100%">
        <el-table-column prop="stepNo" label="步骤号" width="80" />
        <el-table-column prop="stepDescription" label="步骤描述" min-width="150" />
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
        <el-table-column prop="status" label="执行状态" width="100">
          <template #default="scope">
            <el-tag :type="getStepStatusType(scope.row.status)">
              {{ getStepStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assertionStatus" label="断言状态" width="100">
          <template #default="scope">
            <el-tag :type="getAssertionStatusType(scope.row.assertionStatus)">
              {{ getAssertionStatusText(scope.row.assertionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiResult" label="AI断言描述" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.aiResult" :class="{'assertion-success': scope.row.aiResult.includes('✅'), 'assertion-failed': scope.row.aiResult.includes('❌'), 'assertion-warning': scope.row.aiResult.includes('⚠️')}">
              {{ scope.row.aiResult }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="aiTokenUsed" label="AI token消耗" width="120">
          <template #default="scope">
            {{ scope.row.aiTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="截图" width="80">
          <template #default="scope">
            <el-button v-if="scope.row.screenshot" size="small" @click="showScreenshot(scope.row.screenshot)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="stepTotal > 0" class="pagination" style="margin-top: 10px;">
        <el-pagination
          v-model:current-page="stepPageNum"
          v-model:page-size="stepPageSize"
          :total="stepTotal"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadSteps"
          @current-change="loadSteps"
        />
      </div>
    </el-card>
    
    <!-- 执行详情对话框 -->
    <el-dialog v-model="detailVisible" title="执行详情" width="900px">
      <el-descriptions :column="2" border v-if="currentExecution">
        <el-descriptions-item label="用例名称">{{ currentExecution.caseName }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ currentExecution.executorNickname || currentExecution.executor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentExecution.startTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentExecution.duration ? (currentExecution.duration / 1000).toFixed(2) + 's' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentExecution.status)">
            {{ getStatusText(currentExecution.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>步骤执行记录</el-divider>
      
      <el-table :data="stepList" style="width: 100%">
        <el-table-column prop="stepNo" label="步骤号" width="80" />
        <el-table-column prop="stepDescription" label="步骤描述" min-width="150" />
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
        <el-table-column prop="status" label="执行状态" width="100">
          <template #default="scope">
            <el-tag :type="getStepStatusType(scope.row.status)">
              {{ getStepStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assertionStatus" label="断言状态" width="100">
          <template #default="scope">
            <el-tag :type="getAssertionStatusType(scope.row.assertionStatus)">
              {{ getAssertionStatusText(scope.row.assertionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiResult" label="AI断言描述" min-width="200">
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
              <el-tooltip content="数据来自Midscene AI调用的真实token消耗统计" placement="top">
                <el-icon style="margin-left: 4px; cursor: pointer; color: var(--el-color-info);"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="scope">
            {{ scope.row.aiTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="截图" width="80">
          <template #default="scope">
            <el-button v-if="scope.row.screenshot" size="small" @click="showScreenshot(scope.row.screenshot)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 截图预览对话框 -->
    <el-dialog v-model="screenshotVisible" title="执行截图" width="80%">
      <el-image
        v-if="currentScreenshot"
        :src="currentScreenshot"
        style="width: 100%;"
        :preview-src-list="[currentScreenshot]"
        :initial-index="0"
        fit="contain"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, QuestionFilled } from '@element-plus/icons-vue'
import { getExecutionList, getStepExecutions } from '@/api/testCaseExecution'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const detailVisible = ref(false)
const screenshotVisible = ref(false)
const currentExecution = ref(null)
const stepList = ref([])
const stepPageNum = ref(1)
const stepPageSize = ref(5)
const stepTotal = ref(0)
const currentScreenshot = ref('')
const refreshTimer = ref(null)
const isRefreshing = ref(false)

/**
 * 组件挂载时初始化数据
 */
onMounted(() => {
  loadData()
  startAutoRefresh()
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
    loadData()
    if (currentExecution.value && currentExecution.value.status === 'running') {
      loadSteps()
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
 * 加载执行记录列表
 */
const loadData = async () => {
  try {
    const res = await getExecutionList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
      // 自动选中第一条（最新）执行记录
      if (tableData.value.length > 0) {
        // 如果当前没有选中，或者选中的不是第一条，就选中第一条
        if (!currentExecution.value || currentExecution.value.id !== tableData.value[0].id) {
          showDetail(tableData.value[0])
        }
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

/**
 * 显示执行详情
 * @param row 执行记录行数据
 */
const showDetail = async (row) => {
  console.log('showDetail called, row:', row)
  // 清空上一次的步骤执行记录
  stepList.value = []
  stepPageNum.value = 1
  currentExecution.value = row
  
  try {
    console.log('开始请求步骤数据, executionId:', row.id)
    await loadSteps()
    detailVisible.value = true
  } catch (error) {
    console.error('加载步骤异常:', error)
    ElMessage.error('加载步骤失败')
  }
}

/**
 * 加载步骤执行记录
 */
const loadSteps = async () => {
  if (!currentExecution.value) return
  
  try {
    const res = await getStepExecutions(currentExecution.value.id, stepPageNum.value, stepPageSize.value)
    console.log('步骤数据返回:', res)
    if (res.code === 200) {
      if (res.data.records) {
        stepList.value = res.data.records
        stepTotal.value = res.data.total || 0
      } else {
        stepList.value = res.data || []
        stepTotal.value = stepList.value.length
      }
      console.log('stepList赋值后:', stepList.value)
      
      // 如果执行已完成，停止自动刷新步骤
      if (currentExecution.value.status !== 'running') {
        // 但保持列表的自动刷新
      }
    } else {
      console.error('获取步骤失败:', res)
    }
  } catch (error) {
    console.error('加载步骤异常:', error)
  }
}

/**
 * 显示截图
 * @param screenshot 截图URL
 */
const showScreenshot = (screenshot) => {
  currentScreenshot.value = screenshot
  screenshotVisible.value = true
}

/**
 * 格式化日期时间
 * @param datetime 日期时间字符串
 * @returns 格式化后的日期时间字符串
 */
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
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
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
}
</style>
