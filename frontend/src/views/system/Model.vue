<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="toolbar-left">
            <el-button type="primary" icon="Plus" @click="handleAdd">添加模型</el-button>
            <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
            <el-button type="warning" icon="Setting" @click="openScenarioConfig">模型配置</el-button>
          </div>
          <div class="toolbar-right">
            <ColumnSettings
              v-model:columns="modelTableColumns"
              storage-key="model-table-columns"
            />
            <el-input 
              v-model="keyword" 
              placeholder="搜索模型名称/模型家族" 
              style="width: 280px;"
              size="default"
              @keyup.enter="loadData"
              clearable
              @clear="loadData"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'modelName')?.visible"
          prop="modelName" 
          label="模型名称" 
          min-width="180" 
        />
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'modelUrl')?.visible"
          prop="modelUrl" 
          label="模型地址" 
          min-width="250" 
          show-overflow-tooltip 
        />
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'apiKey')?.visible"
          prop="apiKey" 
          label="API Key" 
          min-width="200" 
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.apiKey ? '***' + scope.row.apiKey.slice(-4) : '-' }}
          </template>
        </el-table-column>
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'modelFamily')?.visible"
          prop="modelFamily" 
          label="模型家族" 
          min-width="120" 
        />
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'modelDescription')?.visible"
          prop="modelDescription" 
          label="模型描述" 
          min-width="200" 
          show-overflow-tooltip 
        />
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'status')?.visible"
          prop="status" 
          label="状态" 
          width="100"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          v-if="modelTableColumns.find(c => c.prop === 'createTime')?.visible"
          prop="createTime" 
          label="创建时间" 
          width="170" 
        />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              icon="Connection" 
              :loading="testingConnectionIds.has(scope.row.id)"
              :disabled="testingConnectionIds.has(scope.row.id)"
              @click="handleTestConnection(scope.row)"
            >测试连接</el-button>
            <el-button size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 模型表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="formData.modelName" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型地址" prop="modelUrl">
          <el-input v-model="formData.modelUrl" placeholder="请输入模型地址" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="formData.apiKey" type="password" placeholder="请输入API Key" show-password />
        </el-form-item>
        <el-form-item label="模型家族" prop="modelFamily">
          <el-input v-model="formData.modelFamily" placeholder="请输入模型家族" />
        </el-form-item>
        <el-form-item label="模型描述" prop="modelDescription">
          <el-input v-model="formData.modelDescription" type="textarea" :rows="3" placeholder="请输入模型描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 模型配置对话框 -->
    <el-dialog v-model="scenarioDialogVisible" title="模型配置" width="550px">
      <el-form ref="scenarioFormRef" :model="scenarioForm" label-width="120px">
        <el-form-item label="图像断言检查">
          <el-select v-model="scenarioForm.imageAssertionModelId" placeholder="请选择模型" clearable style="width: 100%;">
            <el-option v-for="model in allModels" :key="model.id" :label="model.modelName" :value="model.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="步骤数据融合">
          <el-select v-model="scenarioForm.stepFusionModelId" placeholder="请选择模型" clearable style="width: 100%;">
            <el-option v-for="model in allModels" :key="model.id" :label="model.modelName" :value="model.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scenarioDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScenarioConfig" :loading="scenarioLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Connection, Setting, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ColumnSettings from '@/components/ColumnSettings.vue'
import { getModelList, addModel, updateModel, deleteModel, testModelConnection, getModelScenarios, updateModelScenario } from '@/api/model'

// 表格数据
const tableData = ref([])
// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
// 加载状态
const loading = ref(false)
// 搜索关键词
const keyword = ref('')
// 选中的ID列表
const selectedIds = ref([])

// 模型列表列配置
const modelTableColumns = ref([
  { prop: 'modelName', label: '模型名称', visible: true },
  { prop: 'modelUrl', label: '模型地址', visible: true },
  { prop: 'apiKey', label: 'API Key', visible: true },
  { prop: 'modelFamily', label: '模型家族', visible: true },
  { prop: 'modelDescription', label: '模型描述', visible: true },
  { prop: 'status', label: '状态', visible: true },
  { prop: 'createTime', label: '创建时间', visible: true }
])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加模型')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  modelName: '',
  modelUrl: '',
  apiKey: '',
  modelFamily: '',
  modelDescription: '',
  status: 1
})

// 表单校验规则
const formRules = {
  modelName: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ]
}

// 模型配置对话框状态
const scenarioDialogVisible = ref(false)
const scenarioLoading = ref(false)
const scenarioFormRef = ref(null)
const allModels = ref([])

// 测试连接 loading 状态
const testingConnectionIds = ref(new Set())

// 场景配置表单数据
const scenarioForm = reactive({
  imageAssertionModelId: null,
  stepFusionModelId: null
})

/**
 * 页面加载时初始化数据
 */
onMounted(() => {
  loadData()
  loadAllModels()
})

/**
 * 加载所有可用模型
 */
const loadAllModels = async () => {
  try {
    const res = await getModelList({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      const data = res.data
      if (data.records) {
        allModels.value = data.records
      } else if (Array.isArray(data)) {
        allModels.value = data
      }
    }
  } catch (error) {
    console.error('加载所有模型失败:', error)
  }
}

/**
 * 测试模型连接
 * @param {Object} row - 模型信息
 */
const handleTestConnection = async (row) => {
  if (testingConnectionIds.value.has(row.id)) {
    return
  }
  
  testingConnectionIds.value.add(row.id)
  try {
    const res = await testModelConnection(row)
    if (res.code === 200) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(res.message || '连接失败')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    ElMessage.error('连接失败')
  } finally {
    testingConnectionIds.value.delete(row.id)
  }
}

/**
 * 打开模型配置对话框
 */
const openScenarioConfig = async () => {
  try {
    const res = await getModelScenarios()
    if (res.code === 200) {
      const scenarios = res.data || []
      // 初始化场景表单
      for (const scenario of scenarios) {
        if (scenario.scenarioCode === 'image_assertion') {
          scenarioForm.imageAssertionModelId = scenario.modelId
        } else if (scenario.scenarioCode === 'step_fusion') {
          scenarioForm.stepFusionModelId = scenario.modelId
        }
      }
      scenarioDialogVisible.value = true
    }
  } catch (error) {
    console.error('加载场景配置失败:', error)
    ElMessage.error('加载场景配置失败')
  }
}

/**
 * 提交模型配置
 */
const submitScenarioConfig = async () => {
  scenarioLoading.value = true
  try {
    // 更新图像断言检查场景
    if (scenarioForm.imageAssertionModelId) {
      await updateModelScenario('image_assertion', { modelId: scenarioForm.imageAssertionModelId })
    }
    // 更新步骤数据融合场景
    if (scenarioForm.stepFusionModelId) {
      await updateModelScenario('step_fusion', { modelId: scenarioForm.stepFusionModelId })
    }
    ElMessage.success('配置成功')
    scenarioDialogVisible.value = false
  } catch (error) {
    console.error('保存场景配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    scenarioLoading.value = false
  }
}

/**
 * 加载模型列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getModelList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })
    if (res.code === 200) {
      const data = res.data
      if (data.records) {
        tableData.value = data.records
        total.value = data.total
      } else if (Array.isArray(data)) {
        tableData.value = data
        total.value = data.length
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 添加模型按钮点击事件
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加模型'
  // 重置表单数据
  formData.id = null
  formData.modelName = ''
  formData.modelUrl = ''
  formData.apiKey = ''
  formData.modelFamily = ''
  formData.modelDescription = ''
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑模型按钮点击事件
 * @param {Object} row - 模型行数据
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑模型'
  // 填充表单数据
  formData.id = row.id
  formData.modelName = row.modelName
  formData.modelUrl = row.modelUrl
  formData.apiKey = row.apiKey
  formData.modelFamily = row.modelFamily
  formData.modelDescription = row.modelDescription
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 删除模型按钮点击事件
 * @param {Object} row - 模型行数据
 */
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除选中的 ' + selectedIds.value.length + ' 个模型吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 批量删除，这里简单起见，逐个删除
    for (const id of selectedIds.value) {
      await deleteModel(id)
    }
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除模型失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除模型 "' + row.modelName + '" 吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteModel(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模型失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 提交表单
 */
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      let res
      if (isEdit.value) {
        // 编辑模型
        res = await updateModel({
          id: formData.id,
          modelName: formData.modelName,
          modelUrl: formData.modelUrl,
          apiKey: formData.apiKey,
          modelFamily: formData.modelFamily,
          modelDescription: formData.modelDescription,
          status: formData.status
        })
      } else {
        // 添加模型
        res = await addModel({
          modelName: formData.modelName,
          modelUrl: formData.modelUrl,
          apiKey: formData.apiKey,
          modelFamily: formData.modelFamily,
          modelDescription: formData.modelDescription,
          status: formData.status
        })
      }

      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('提交表单失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.common-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .toolbar-left {
      display: flex;
      gap: 10px;
    }
    .toolbar-right {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
