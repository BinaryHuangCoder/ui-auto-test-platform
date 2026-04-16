<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div style="display: flex; gap: 10px;">
            <ColumnSettings
              v-model:columns="departmentTableColumns"
              storage-key="department-table-columns"
            />
            <el-button type="primary" icon="Plus" @click="handleAdd()">添加部门</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="tableData" 
        style="width: 100%" 
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
        default-expand-all
      >
        <el-table-column 
          v-if="departmentTableColumns.find(c => c.prop === 'name')?.visible"
          prop="name" 
          label="部门名称" 
          min-width="180" 
        />
        <el-table-column 
          v-if="departmentTableColumns.find(c => c.prop === 'leader')?.visible"
          prop="leader" 
          label="部门负责人" 
          width="120" 
        />
        <el-table-column 
          v-if="departmentTableColumns.find(c => c.prop === 'status')?.visible"
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
          v-if="departmentTableColumns.find(c => c.prop === 'createTime')?.visible"
          prop="createTime" 
          label="创建时间" 
          width="170"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button size="small" icon="Plus" @click="handleAdd(scope.row)">添加</el-button>
              <el-button size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[15, 30, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
    
    <!-- 部门表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-cascader
            v-model="formData.parentId"
            :options="departmentOptions"
            :props="{ 
              checkStrictly: true, 
              value: 'id', 
              label: 'name',
              children: 'children',
              expandTrigger: 'hover',
              emitPath: false
            }"
            placeholder="请选择上级部门（不选则为顶级部门）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门负责人" prop="leader">
          <el-input v-model="formData.leader" placeholder="请输入部门负责人" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import ColumnSettings from '@/components/ColumnSettings.vue'
import { getDepartmentList, addDepartment, updateDepartment, deleteDepartment } from '@/api/department'

// 表格数据
const tableData = ref([])
// 分页参数
const pageNum = ref(1)
const pageSize = ref(15)  // 默认15条分页
const total = ref(0)
// 加载状态
const loading = ref(false)

// 部门列表列配置
const departmentTableColumns = ref([
  { prop: 'name', label: '部门名称', visible: true },
  { prop: 'leader', label: '部门负责人', visible: true },
  { prop: 'status', label: '状态', visible: true },
  { prop: 'createTime', label: '创建时间', visible: true }
])

// 部门选项（用于级联选择器）
const departmentOptions = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加部门')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const parentName = ref('')

// 表单数据
const formData = reactive({
  id: null,
  parentId: null,
  name: '',
  leader: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ]
}

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @returns {string} 格式化后的日期时间字符串（年-月-日 时:分:秒）
 */
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

/**
 * 页面加载时初始化数据
 */
onMounted(() => {
  loadData()
})

/**
 * 加载部门列表数据
 * @returns {Promise<void>} 无返回值
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      // 树形数据处理
      tableData.value = res.data || []
      total.value = res.data?.total || res.data?.length || 0
      // 加载部门选项用于级联选择器
      departmentOptions.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 添加部门按钮点击事件
 * @param {Object} row - 父部门行数据（可选）
 * @returns {void} 无返回值
 */
const handleAdd = (row = null) => {
  isEdit.value = false
  dialogTitle.value = '添加部门'
  // 重置表单数据
  formData.id = null
  formData.parentId = row ? row.id : null
  formData.name = ''
  formData.leader = ''
  formData.sort = 0
  formData.status = 1
  parentName.value = row ? row.name : ''
  dialogVisible.value = true
}

/**
 * 编辑部门按钮点击事件
 * @param {Object} row - 部门行数据
 * @returns {void} 无返回值
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  // 填充表单数据
  formData.id = row.id
  formData.parentId = row.parentId === 0 ? null : row.parentId
  formData.name = row.name
  formData.leader = row.leader || ''
  formData.sort = row.sort || 0
  formData.status = row.status
  parentName.value = row.parentId ? getParentName(row.parentId) : ''
  dialogVisible.value = true
}

/**
 * 获取父部门名称
 * @param {number} parentId - 父部门ID
 * @returns {string} 父部门名称
 */
const getParentName = (parentId) => {
  const findParent = (list) => {
    for (const item of list) {
      if (item.id === parentId) return item.name
      if (item.children) {
        const found = findParent(item.children)
        if (found) return found
      }
    }
    return ''
  }
  return findParent(departmentOptions.value)
}

/**
 * 删除部门按钮点击事件
 * @param {Object} row - 部门行数据
 * @returns {Promise<void>} 无返回值
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除部门 "' + row.name + '" 吗？' + (row.children && row.children.length > 0 ? '（该部门包含子部门，将一并删除）' : ''),
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteDepartment(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 提交表单
 * @returns {Promise<void>} 无返回值
 */
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      let res
      if (isEdit.value) {
        // 编辑部门
        res = await updateDepartment({
          id: formData.id,
          parentId: formData.parentId,
          name: formData.name,
          leader: formData.leader,
          sort: formData.sort,
          status: formData.status
        })
      } else {
        // 添加部门
        res = await addDepartment({
          parentId: formData.parentId || 0,
          name: formData.name,
          leader: formData.leader,
          sort: formData.sort,
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
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  // 操作按钮不堆叠，使用flex布局
  .operation-buttons {
    display: flex;
    flex-wrap: nowrap;
    gap: 4px;
    
    .el-button {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}
</style>