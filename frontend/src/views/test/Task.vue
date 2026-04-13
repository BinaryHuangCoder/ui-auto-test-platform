<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试任务</span>
        </div>
      </template>
      
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="openDialog('add')">新建任务</el-button>
          <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <el-input 
            v-model="keyword" 
            placeholder="搜索任务编号/名称/创建人" 
            style="width: 280px;"
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
        <el-table-column prop="taskNo" label="任务编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="taskName" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="creator" label="创建人" width="100">
          <template #default="scope">
            {{ scope.row.creatorNickname || scope.row.creator || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="cronExpression" label="定时策略" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <el-tag v-if="scope.row.cronExpression" type="info" size="small">
              {{ scope.row.cronExpression }}
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="openDialog('edit', scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper, refresh"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新建任务' : '编辑任务'" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="任务编号" v-if="dialogType === 'edit'">
          <span>{{ formData.taskNo }}</span>
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="formData.taskName" placeholder="请输入任务名称" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="定时策略" prop="cronExpression">
          <el-input v-model="formData.cronExpression" placeholder="请输入cron表达式，例如：0 0 12 * * ?" maxlength="100" />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            常用cron示例：<br/>
            每天12点执行：0 0 12 * * ?<br/>
            每小时执行一次：0 0 * * * ?<br/>
            每周一早上9点：0 0 9 ? * MON
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px;">{{ formData.status === 1 ? '启用' : '禁用' }}</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/api/request'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selectedIds = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  taskNo: '',
  name: '',
  creator: '',
  cronExpression: '',
  status: 1
})

const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 200, message: '任务名称长度在 2 到 200 个字符', trigger: 'blur' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 查询数据
const fetchData = async () => {
  try {
    const res = await request.get('/test-task/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: keyword.value
      }
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  }
}

// 搜索
const search = () => {
  pageNum.value = 1
  fetchData()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  fetchData()
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    const res = await request.put('/test-task', {
      id: row.id,
      status: row.status
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(res.message || '状态更新失败')
      // 回滚状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 打开对话框
const openDialog = (type, row = null) => {
  dialogType.value = type
  
  if (type === 'add') {
    Object.assign(formData, {
      id: null,
      taskNo: '',
      taskName: '',
      creator: '',
      cronExpression: '',
      status: 1
    })
  } else if (type === 'edit' && row) {
    Object.assign(formData, {
      id: row.id,
      taskNo: row.taskNo,
      taskName: row.taskName,
      creator: row.creator,
      cronExpression: row.cronExpression || '',
      status: row.status
    })
  }
  
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (dialogType.value === 'add') {
          res = await request.post('/test-task', formData)
        } else {
          res = await request.put('/test-task', formData)
        }
        
        if (res.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
          dialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除单个
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除任务"${row.taskName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/test-task/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 批量删除
const batchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 个任务吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete('/test-task/batch', {
        data: selectedIds.value
      })
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.common-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .toolbar-left, .toolbar-right {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
