<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <span>UI自动化测试</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/test">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>测试管理</span>
          </template>
          <el-menu-item index="/test/case">
            <el-icon><Document /></el-icon>
            测试用例
          </el-menu-item>
          <el-menu-item index="/test/task">
            <el-icon><List /></el-icon>
            测试任务
          </el-menu-item>
          <el-menu-item index="/test/report">
            <el-icon><DataAnalysis /></el-icon>
            测试报告
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>平台管理</span>
          </template>
          <el-menu-item index="/system/user">
            <el-icon><User /></el-icon>
            用户管理
          </el-menu-item>
          <el-menu-item index="/system/department">
            <el-icon><OfficeBuilding /></el-icon>
            部门管理
          </el-menu-item>
          <el-menu-item index="/system/system">
            <el-icon><Monitor /></el-icon>
            系统管理
          </el-menu-item>
          <el-menu-item index="/system/model">
            <el-icon><Cpu /></el-icon>
            模型管理
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :src="userInfo.avatar" size="small" />
              <span>{{ userInfo.nickname || username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="resetPwd">
                  <el-icon><Lock /></el-icon> 重置密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 注销
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 个人中心对话框 -->
    <el-dialog v-model="profileVisible" title="个人中心" width="500px">
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="uploadAvatar"
          >
            <el-avatar v-if="profileForm.avatar" :src="profileForm.avatar" :size="80" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="profileForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="profileForm.department" placeholder="请选择部门（非必填）" clearable style="width: 100%;">
            <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.name" :value="dept.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="profileForm.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="profileLoading">保存</el-button>
      </template>
    </el-dialog>
    

    
    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-alert
          title="密码强度要求："
          type="info"
          :closable="false"
          style="margin-bottom: 15px;"
        >
          <ul style="margin: 5px 0 0 0; padding-left: 20px; font-size: 12px;">
            <li>长度至少8位</li>
            <li>必须包含大写字母</li>
            <li>必须包含小写字母</li>
            <li>必须包含数字</li>
            <li>必须包含特殊字符(!@#$%^&*)</li>
          </ul>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确认重置</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Cropper from 'cropperjs'
import { HomeFilled, Document, List, DataAnalysis, Setting, User, OfficeBuilding, Monitor, Cpu, UserFilled, Lock, SwitchButton, Plus, ArrowDown } from '@element-plus/icons-vue'
import { resetPassword, changePassword, getUserInfo, updateUserProfile } from '@/api/user'
import { getDepartmentList } from '@/api/department'

const route = useRoute()
const router = useRouter()

const username = ref(localStorage.getItem('username') || 'admin')
const resetPwdVisible = ref(false)
const profileVisible = ref(false)
const profileLoading = ref(false)
const pwdForm = ref({
  newPassword: ''
})
const userInfo = ref({})
const profileForm = ref({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  gender: 0,
  department: '',
  phone: '',
  employeeNo: '',
  email: ''
})

// 裁剪相关
const cropperVisible = ref(false)
const cropperSrc = ref('')
const cropperImageRef = ref(null)
let cropperInstance = null
const departmentList = ref([])

// 表单验证规则
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const profileRules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }]
}

const pageTitle = computed(() => {
  const titleMap = {
    '/dashboard': '首页',
    '/test/case': '测试用例',
    '/test/case-detail': '用例详情',
    '/test/task': '测试任务',
    '/test/report': '测试报告',
    '/system/user': '用户管理',
    '/system/department': '部门管理',
    '/system/system': '系统管理',
    '/system/model': '模型管理'
  }
  return titleMap[route.path] || 'UI自动化测试平台'
})

const breadcrumbs = computed(() => {
  const path = route.path
  const crumbs = []
  
  // 首页
  crumbs.push({ title: '首页', path: '/dashboard' })
  
  if (path.startsWith('/test')) {
    crumbs.push({ title: '测试管理', path: '' })
    if (path === '/test/case') {
      crumbs.push({ title: '测试用例', path: '/test/case' })
    } else if (path === '/test/case-detail') {
      crumbs.push({ title: '测试用例', path: '/test/case' })
      crumbs.push({ title: '用例详情', path: '' })
    } else if (path === '/test/task') {
      crumbs.push({ title: '测试任务', path: '/test/task' })
    } else if (path === '/test/report') {
      crumbs.push({ title: '测试报告', path: '/test/report' })
    }
  } else if (path.startsWith('/system')) {
    crumbs.push({ title: '平台管理', path: '' })
    if (path === '/system/user') {
      crumbs.push({ title: '用户管理', path: '/system/user' })
    } else if (path === '/system/department') {
      crumbs.push({ title: '部门管理', path: '/system/department' })
    } else if (path === '/system/system') {
      crumbs.push({ title: '系统管理', path: '/system/system' })
    } else if (path === '/system/model') {
      crumbs.push({ title: '模型管理', path: '/system/model' })
    }
  }
  
  return crumbs
})

onMounted(() => {
  loadUserInfo()
  loadDepartments()
})

/**
 * 加载部门列表并扁平化
 */
const loadDepartments = async () => {
  try {
    const res = await getDepartmentList()
    if (res.code === 200 && res.data) {
      // 扁平化树形部门数据
      const flatten = (list) => {
        let result = []
        for (const item of list) {
          result.push(item)
          if (item.children && item.children.length > 0) {
            result = result.concat(flatten(item.children))
          }
        }
        return result
      }
      departmentList.value = flatten(res.data)
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      profileForm.value = { ...res.data }
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要注销登录吗？', '提示', { type: 'warning' })
      localStorage.clear()
      router.push('/login')
    } catch (e) {}
  } else if (cmd === 'resetPwd') {
    resetPwdVisible.value = true
    pwdForm.value.newPassword = ''
  } else if (cmd === 'profile') {
    await loadUserInfo()
    profileVisible.value = true
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
  }
  return isImage && isLt2M
}

const uploadAvatar = (options) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.value.avatar = e.target.result
    ElMessage.success('头像上传成功')
  }
  reader.readAsDataURL(options.file)
}

const saveProfile = async () => {
  profileLoading.value = true
  try {
    const res = await updateUserProfile({
      nickname: profileForm.value.nickname,
      avatar: profileForm.value.avatar,
      gender: profileForm.value.gender,
      department: profileForm.value.department,
      phone: profileForm.value.phone,
      employeeNo: profileForm.value.employeeNo,
      email: profileForm.value.email
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      profileVisible.value = false
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    profileLoading.value = false
  }
}

const submitResetPwd = async () => {
  if (!pwdForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  const pwd = pwdForm.value.newPassword
  if (pwd.length < 8) {
    ElMessage.error('密码长度必须至少8位')
    return
  }
  if (!/[A-Z]/.test(pwd)) {
    ElMessage.error('密码必须包含大写字母')
    return
  }
  if (!/[a-z]/.test(pwd)) {
    ElMessage.error('密码必须包含小写字母')
    return
  }
  if (!/[0-9]/.test(pwd)) {
    ElMessage.error('密码必须包含数字')
    return
  }
  if (!/[!@#$%^&*]/.test(pwd)) {
    ElMessage.error('密码必须包含特殊字符(!@#$%^&*)')
    return
  }
  
  try {
    const res = await changePassword({
      username: username.value,
      oldPassword: '123456',
      newPassword: pwdForm.value.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      resetPwdVisible.value = false
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    ElMessage.error('重置失败')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    background-color: #263445;
  }
  
  .sidebar-menu {
    border-right: none;
    background-color: #304156;
    
    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      color: #bfcbd9;
      
      &:hover {
        background-color: #263445;
      }
    }
    
    :deep(.el-menu-item.is-active) {
      background-color: #409eff !important;
      color: #fff;
    }
  }
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  
  .page-title {
    font-size: 18px;
    font-weight: 500;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 4px;
    
    &:hover {
      background-color: #f5f7fa;
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}

.avatar-uploader {
  display: inline-block;
  
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 80px;
    height: 80px;
    text-align: center;
    line-height: 80px;
  }
}

.cropper-container {
  width: 100%;
}

.cropper-wrapper {
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.cropper-tools {
  margin-top: 15px;
  text-align: center;
}
</style>