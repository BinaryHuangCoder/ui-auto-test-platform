<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">128</div>
              <div class="stat-label">测试用例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon :size="30"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">96</div>
              <div class="stat-label">通过用例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon :size="30"><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">24</div>
              <div class="stat-label">运行中任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon :size="30"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">3</div>
              <div class="stat-label">失败用例</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和最近任务 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" icon="Plus">新建用例</el-button>
            <el-button type="success" icon="VideoPlay">执行任务</el-button>
            <el-button type="warning" icon="Upload">导入用例</el-button>
            <el-button type="info" icon="Download">导出报告</el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近任务</span>
            </div>
          </template>
          <el-table :data="recentTasks" style="width: 100%" :show-header="false">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.statusType" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="执行时间" width="150" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 欢迎信息 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 UI 自动化测试平台</span>
        </div>
      </template>
      <div class="welcome-content">
        <p>🦞 欢迎使用基于视觉模型的 UI 自动化测试平台！</p>
        <p style="margin-top: 10px; color: #606266">
          本平台支持：
        </p>
        <ul style="margin-top: 10px; color: #606266; line-height: 2">
          <li>📝 测试用例管理与维护</li>
          <li>🎯 自动化测试任务调度</li>
          <li>👁️ 基于视觉模型的图像识别</li>
          <li>📊 测试报告生成与分析</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const recentTasks = ref([
  { name: '登录功能测试', status: '成功', statusType: 'success', time: '2026-03-26 10:30' },
  { name: '首页 UI 测试', status: '运行中', statusType: 'warning', time: '2026-03-26 10:15' },
  { name: '支付流程测试', status: '成功', statusType: 'success', time: '2026-03-26 09:45' },
  { name: '用户管理测试', status: '失败', statusType: 'danger', time: '2026-03-26 09:20' }
])
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
      
      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
  
  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .welcome-content {
    p {
      margin: 0;
    }
    
    ul {
      padding-left: 20px;
    }
  }
}
</style>
