<template>
  <div class="case-detail-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="title">{{ isEdit ? '编辑用例' : '新增用例' }}</span>
          </div>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="case-form">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="用例编号" prop="caseNo">
              <el-input v-model="form.caseNo" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="用例名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入用例名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="设计者" prop="designer">
              <el-select v-model="form.designer" placeholder="请选择设计者" style="width: 100%">
                <el-option v-for="user in userList" :key="user.id" :label="user.nickname || user.username" :value="user.username" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="关联系统" prop="systemId">
              <el-select v-model="form.systemId" placeholder="请选择系统" style="width: 100%" clearable>
                <el-option v-for="system in systemList" :key="system.id" :label="system.systemName" :value="system.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="用例性质" prop="caseType">
              <el-select v-model="form.caseType" placeholder="请选择用例性质" style="width: 100%">
                <el-option label="正例" value="positive" />
                <el-option label="反例" value="negative" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="用例描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入用例描述" 
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 用例步骤管理 -->
    <el-card v-if="form.id" class="step-card">
      <template #header>
        <div class="card-header">
          <span>用例步骤</span>
        </div>
      </template>
      
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="addStep">新增步骤</el-button>
          <el-button type="danger" icon="Delete" :disabled="!selectedStepIds.length" @click="batchDeleteStep">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <el-upload
            :action="stepImportUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleStepImportSuccess"
            :on-error="handleStepImportError"
            accept=".xlsx,.xls"
          >
            <el-button icon="Upload">导入</el-button>
          </el-upload>
          <el-button icon="Download" @click="handleStepExport">导出</el-button>
          <el-input 
            v-model="stepKeyword" 
            placeholder="搜索步骤描述" 
            style="width: 200px;"
            @keyup.enter="loadSteps"
            clearable
            @clear="loadSteps"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      
      <el-table 
        :data="filteredStepList" 
        style="width: 100%"
        @selection-change="handleStepSelectionChange"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="stepNo" label="步骤号" width="80" />
        <el-table-column prop="stepDescription" label="步骤描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="assertionDescription" label="断言描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="testData" label="测试数据" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="启用" width="70">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStepStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="screenshot" label="截图" width="60">
          <template #default="scope">
            <el-tag :type="scope.row.screenshot === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.screenshot === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="click" @command="(cmd) => handleStepCommand(cmd, scope.row)">
              <el-button size="small" type="primary">
                操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
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
      <Pagination
        v-model="stepPagination"
        :total="stepTotal"
        @refresh="loadSteps"
      />
    </el-card>
    
    <!-- 步骤编辑对话框 -->
    <el-dialog v-model="stepDialogVisible" :title="stepForm.id ? '编辑步骤' : '新增步骤'" width="600px">
      <el-form :model="stepForm" ref="stepFormRef" label-width="120px">
        <el-form-item label="步骤描述" prop="stepDescription">
          <el-input v-model="stepForm.stepDescription" type="textarea" :rows="3" placeholder="请输入步骤描述" />
        </el-form-item>
        <el-form-item label="断言描述" prop="assertionDescription">
          <el-input v-model="stepForm.assertionDescription" type="textarea" :rows="2" placeholder="请输入断言描述（可选）" />
        </el-form-item>
        <el-form-item label="测试数据" prop="testData">
          <el-input v-model="stepForm.testData" type="textarea" :rows="2" placeholder="请输入测试数据（可选）" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否启用">
              <el-switch v-model="stepForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否截图">
              <el-switch v-model="stepForm.screenshot" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="stepDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStep" :loading="stepLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaseDetail, addCase, updateCase } from '@/api/testCase'
import { getStepList, addStep as apiAddStep, updateStep, deleteStep as apiDeleteStep, batchDeleteStep as apiBatchDeleteStep, exportSteps, importSteps } from '@/api/testCaseStep'
import { listAllUsers } from '@/api/user'
import { listAllSystems } from '@/api/system'
import Pagination from '@/components/Pagination.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const stepFormRef = ref(null)
const loading = ref(false)

const isEdit = computed(() => !!route.query.id)
const caseId = computed(() => route.query.id)
const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const form = reactive({
  id: null,
  caseNo: '',
  name: '',
  description: '',
  designer: '',
  systemId: null,
  caseType: 'positive',
  status: 1
})

const systemList = ref([])

const stepList = ref([])
const stepPagination = ref({ pageNum: 1, pageSize: 10 })
const stepTotal = ref(0)
const stepKeyword = ref('')
const stepDialogVisible = ref(false)
const stepLoading = ref(false)
const selectedStepIds = ref([])
const userList = ref([])

const stepForm = reactive({
  id: null,
  caseId: null,
  stepDescription: '',
  assertionDescription: '',
  testData: '',
  status: 1,
  screenshot: 1
})

const stepImportUrl = computed(() => baseURL + '/api/case-step/import/' + form.id)
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('token')
}))

const filteredStepList = computed(() => {
  if (!stepKeyword.value) return stepList.value
  const keyword = stepKeyword.value.toLowerCase()
  return stepList.value.filter(step => 
    (step.stepDescription && step.stepDescription.toLowerCase().includes(keyword)) ||
    (step.assertionDescription && step.assertionDescription.toLowerCase().includes(keyword))
  )
})

const rules = {
  name: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  designer: [{ required: true, message: '请选择设计者', trigger: 'change' }],
  caseType: [{ required: true, message: '请选择用例性质', trigger: 'change' }]
}

onMounted(async () => {
  // 加载用户列表（用于设计者下拉框）
  try {
    const userRes = await listAllUsers()
    if (userRes.code === 200) {
      userList.value = userRes.data || []
    }
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
  
  // 加载系统列表（用于系统下拉框）
  try {
    const systemRes = await listAllSystems()
    if (systemRes.code === 200) {
      systemList.value = systemRes.data || []
    }
  } catch (error) {
    console.error('加载系统列表失败', error)
  }
  
  if (caseId.value) {
    try {
      const res = await getCaseDetail(caseId.value)
      if (res.code === 200) {
        Object.assign(form, res.data)
        loadSteps()
      }
    } catch (error) {
      ElMessage.error('加载用例详情失败')
    }
  }
})

const loadSteps = async () => {
  if (!form.id) return
  try {
    const res = await getStepList(form.id, stepPagination.value.pageNum, stepPagination.value.pageSize)
    if (res.code === 200) {
      stepList.value = res.data.records || []
      stepTotal.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载步骤失败')
  }
}

const goBack = () => {
  router.push('/test/case')
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const api = isEdit.value ? updateCase : addCase
    const res = await api(form)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      if (!isEdit.value) {
        Object.assign(form, res.data)
        loadSteps()
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleStepSelectionChange = (selection) => {
  selectedStepIds.value = selection.map(item => item.id)
}

const addStep = () => {
  if (!form.id) {
    ElMessage.warning('请先保存用例基本信息')
    return
  }
  Object.assign(stepForm, { id: null, caseId: form.id, stepDescription: '', assertionDescription: '', status: 1, screenshot: 1 })
  stepDialogVisible.value = true
}

const editStep = (row) => {
  Object.assign(stepForm, { id: row.id, caseId: row.caseId, stepDescription: row.stepDescription, assertionDescription: row.assertionDescription, testData: row.testData, status: row.status, screenshot: row.screenshot })
  stepDialogVisible.value = true
}

const handleStepCommand = (cmd, row) => {
  if (cmd === 'edit') {
    editStep(row)
  } else if (cmd === 'delete') {
    deleteStep(row)
  }
}

const submitStep = async () => {
  if (!stepForm.stepDescription) {
    ElMessage.warning('请输入步骤描述')
    return
  }
  stepLoading.value = true
  try {
    const api = stepForm.id ? updateStep : apiAddStep
    const res = await api(stepForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      stepDialogVisible.value = false
      loadSteps()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    stepLoading.value = false
  }
}

const handleStepStatusChange = async (row) => {
  try {
    await updateStep(row)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const deleteStep = async (row) => {
  try {
    await apiDeleteStep(row.id)
    ElMessage.success('删除成功')
    loadSteps()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const batchDeleteStep = async () => {
  try {
    await apiBatchDeleteStep(selectedStepIds.value)
    ElMessage.success('批量删除成功')
    loadSteps()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleStepExport = async () => {
  if (!form.id) return
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(baseURL + '/api/case-step/export/' + form.id, {
      headers: { Authorization: 'Bearer ' + token }
    })
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用例步骤_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleStepImportSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success(response.message || '导入成功')
    loadSteps()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleStepImportError = () => {
  ElMessage.error('导入失败')
}
</script>

<style scoped lang="scss">
.case-detail-page {
  .card-header {
    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;
    }
    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  .case-form {
    max-width: 900px;
  }
  .step-card {
    margin-top: 20px;
  }
  
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    flex-wrap: wrap;
    gap: 10px;
    
    .toolbar-left {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }
    
    .toolbar-right {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-wrap: wrap;
    }
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-table) {
  width: 100% !important;
}
</style>
