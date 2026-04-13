<template>
  <div class="common-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">添加用户</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
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
          :page-sizes="[15, 30, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
    
    <!-- 用户表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" show-password placeholder="请输入密码" />
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
import { listUsers, addUser, updateUser, deleteUser } from '@/api/user'

// 表格数据
const tableData = ref([])
// 分页参数
const pageNum = ref(1)
const pageSize = ref(15)  // 默认15条分页
const total = ref(0)
// 加载状态
const loading = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  password: '',
  status: 1
})

// 表单校验规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

/**
 * 页面加载时初始化数据
 */
onMounted(() => {
  loadData()
})

/**
 * 加载用户列表数据
 * @returns {Promise<void>} 无返回值
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await listUsers({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      const data = res.data
      if (data.records) {
        // 分页格式
        tableData.value = data.records
        total.value = data.total
      } else if (Array.isArray(data)) {
        // 非分页格式（兼容旧接口）
        tableData.value = data
        total.value = data.length
      }
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 添加用户按钮点击事件
 * @returns {void} 无返回值
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加用户'
  // 重置表单数据
  formData.id = null
  formData.username = ''
  formData.nickname = ''
  formData.email = ''
  formData.password = ''
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑用户按钮点击事件
 * @param {Object} row - 用户行数据
 * @returns {void} 无返回值
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  // 填充表单数据
  formData.id = row.id
  formData.username = row.username
  formData.nickname = row.nickname
  formData.email = row.email
  formData.password = ''
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 删除用户按钮点击事件
 * @param {Object} row - 用户行数据
 * @returns {Promise<void>} 无返回值
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除用户 "' + row.username + '" 吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
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
        // 编辑用户
        res = await updateUser({
          id: formData.id,
          nickname: formData.nickname,
          email: formData.email,
          status: formData.status
        })
      } else {
        // 添加用户
        res = await addUser({
          username: formData.username,
          nickname: formData.nickname,
          email: formData.email,
          password: formData.password,
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
}
</style>