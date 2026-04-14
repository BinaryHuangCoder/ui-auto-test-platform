import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'Home' }
      },
      {
        path: 'test/case',
        name: 'TestCase',
        component: () => import('@/views/test/Case.vue'),
        meta: { title: '测试用例', icon: 'Document' }
      },
      {
        path: 'test/case-detail',
        name: 'CaseDetail',
        component: () => import('@/views/test/CaseDetail.vue'),
        meta: { title: '用例详情', icon: 'Document' }
      },
      {
        path: 'test/task',
        name: 'TestTask',
        component: () => import('@/views/test/Task.vue'),
        meta: { title: '测试任务', icon: 'Schedule' }
      },
      {
        path: 'test/report',
        name: 'TestReport',
        component: () => import('@/views/test/Report.vue'),
        meta: { title: '测试报告', icon: 'DataAnalysis' }
      },
      {
        path: 'vision/model',
        name: 'VisionModel',
        component: () => import('@/views/vision/Model.vue'),
        meta: { title: '模型管理', icon: 'Setting' }
      },
      {
        path: 'vision/recognize',
        name: 'VisionRecognize',
        component: () => import('@/views/vision/Recognize.vue'),
        meta: { title: '图像识别', icon: 'Camera' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/department',
        name: 'SystemDepartment',
        component: () => import('@/views/system/Department.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'system/system',
        name: 'SystemManagement',
        component: () => import('@/views/system/System.vue'),
        meta: { title: '系统管理', icon: 'Setting' }
      },
      {
        path: 'system/model',
        name: 'ModelManagement',
        component: () => import('@/views/system/Model.vue'),
        meta: { title: '模型配置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
