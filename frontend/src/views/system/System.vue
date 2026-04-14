<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="toolbar-left">
            <el-button type="primary" icon="Plus" @click="handleAdd">添加系统</el-button>
            <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
          </div>
          <div class="toolbar-right">
            <el-input 
              v-model="keyword" 
              placeholder="搜索系统编号/名称/简称" 
              style="width: 280px;"
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
        <el-table-column prop="systemNo" label="系统编号" min-width="140" />
        <el-table-column prop="systemName" label="系统名称" min-width="180" />
        <el-table-column prop="systemShortName" label="系统简称" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="系统来源" width="120">
          <template #default="scope">
            {{ scope.row.source === 'manual' ? '手工新增' : '外部同步' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
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

    <!-- 系统表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="系统编号" prop="systemNo">
          <el-input v-model="formData.systemNo" :disabled="isEdit" placeholder="请输入系统编号" />
        </el-form-item>
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="formData.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        <el-form-item label="系统简称" prop="systemShortName">
          <el-input v-model="formData.systemShortName" placeholder="请输入系统简称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="系统来源" prop="source">
          <el-select v-model="formData.source" placeholder="请选择系统来源" style="width: 100%;">
            <el-option label="手工新增" value="manual" />
            <el-option label="外部同步" value="external" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSystemList, addSystem, updateSystem, deleteSystem } from '@/api/system'

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

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加系统')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  systemNo: '',
  systemName: '',
  systemShortName: '',
  status: 1,
  source: 'manual'
})

// 表单校验规则
const formRules = {
  systemNo: [
    { required: true, message: '请输入系统编号', trigger: 'blur' }
  ],
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' }
  ]
}

/**
 * 页面加载时初始化数据
 */
onMounted(() => {
  loadData()
})

/**
 * 加载系统列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getSystemList({
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
    console.error('加载系统列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 添加系统按钮点击事件
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加系统'
  // 重置表单数据
  formData.id = null
  formData.systemNo = ''
  formData.systemName = ''
  formData.systemShortName = ''
  formData.status = 1
  formData.source = 'manual'
  dialogVisible.value = true
}

/**
 * 编辑系统按钮点击事件
 * @param {Object} row - 系统行数据
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑系统'
  // 填充表单数据
  formData.id = row.id
  formData.systemNo = row.systemNo
  formData.systemName = row.systemName
  formData.systemShortName = row.systemShortName
  formData.status = row.status
  formData.source = row.source || 'manual'
  dialogVisible.value = true
}

/**
 * 删除系统按钮点击事件
 * @param {Object} row - 系统行数据
 */
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除选中的 ' + selectedIds.value.length + ' 个系统吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 批量删除，这里简单起见，逐个删除
    for (const id of selectedIds.value) {
      await deleteSystem(id)
    }
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除系统失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除系统 "' + row.systemName + '" 吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteSystem(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除系统失败:', error)
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
        // 编辑系统
        res = await updateSystem({
          id: formData.id,
          systemNo: formData.systemNo,
          systemName: formData.systemName,
          systemShortName: formData.systemShortName,
          status: formData.status,
          source: formData.source
        })
      } else {
        // 添加系统
        res = await addSystem({
          systemNo: formData.systemNo,
          systemName: formData.systemName,
          systemShortName: formData.systemShortName,
          status: formData.status,
          source: formData.source
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
