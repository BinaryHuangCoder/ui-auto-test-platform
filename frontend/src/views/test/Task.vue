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
          <el-button type="success" icon="VideoPlay" :disabled="!selectedIds.length" @click="batchRun">立刻执行</el-button>
          <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <ColumnSettings
            v-model:columns="taskTableColumns"
            storage-key="task-table-columns"
          />
          <el-input 
            v-model="keyword" 
            placeholder="搜索任务编号/名称/创建人" 
            style="width: 280px;"
            size="default"
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
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'taskNo')?.visible"
          prop="taskNo" 
          label="任务编号" 
          min-width="160" 
          show-overflow-tooltip 
        />
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'taskName')?.visible"
          prop="taskName" 
          label="任务名称" 
          min-width="180" 
          show-overflow-tooltip 
        />
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'creator')?.visible"
          prop="creator" 
          label="创建人" 
          width="100"
        >
          <template #default="scope">
            {{ scope.row.creatorNickname || scope.row.creator || '-' }}
          </template>
        </el-table-column>
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'createTime')?.visible"
          prop="createTime" 
          label="创建时间" 
          width="170"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'latestExecuteTime')?.visible"
          prop="latestExecuteTime" 
          label="最新执行时间" 
          width="170"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.latestExecuteTime) }}
          </template>
        </el-table-column>
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'cronExpression')?.visible"
          prop="cronExpression" 
          label="定时策略" 
          min-width="150" 
          show-overflow-tooltip
        >
          <template #default="scope">
            <el-tag v-if="scope.row.cronExpression" type="info" size="small">
              {{ scope.row.cronExpression }}
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column 
          prop="concurrency" 
          label="并发数" 
          width="90"
        >
          <template #default="scope">
            <el-tag type="info" size="small">{{ scope.row.concurrency || 1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          v-if="taskTableColumns.find(c => c.prop === 'status')?.visible"
          prop="status" 
          label="状态" 
          width="80"
        >
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="openCaseSelectDialog(scope.row)">
              <el-icon><Document /></el-icon> 选择用例
            </el-button>
            <el-button type="success" size="small" link @click="openExecutionDialog(scope.row)">
              <el-icon><List /></el-icon> 查看历史
            </el-button>
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
          <el-tabs v-model="cronTabType" type="card" style="width: 100%;">
            <el-tab-pane label="预设" name="preset">
              <el-select v-model="cronPreset" placeholder="选择常用定时策略" style="width: 100%;" @change="onCronPresetChange">
                <el-option label="每天 00:00 执行" value="0 0 0 * * ?" />
                <el-option label="每天 08:00 执行" value="0 0 8 * * ?" />
                <el-option label="每天 12:00 执行" value="0 0 12 * * ?" />
                <el-option label="每天 18:00 执行" value="0 0 18 * * ?" />
                <el-option label="每小时执行一次" value="0 0 * * * ?" />
                <el-option label="每30分钟执行一次" value="0 0/30 * * * ?" />
                <el-option label="每10分钟执行一次" value="0 0/10 * * * ?" />
                <el-option label="每分钟执行一次" value="0 * * * * ?" />
                <el-option label="每周一 09:00 执行" value="0 0 9 ? * MON" />
                <el-option label="每周五 18:00 执行" value="0 0 18 ? * FRI" />
                <el-option label="每月1号 00:00 执行" value="0 0 0 1 * ?" />
              </el-select>
            </el-tab-pane>
            <el-tab-pane label="自定义" name="custom">
              <el-form :model="cronCustom" label-width="80px" style="margin-top: 10px;">
                <el-form-item label="秒">
                  <el-input v-model="cronCustom.second" placeholder="0-59，* 表示每秒钟" style="width: 200px;" />
                </el-form-item>
                <el-form-item label="分">
                  <el-input v-model="cronCustom.minute" placeholder="0-59，* 表示每分钟" style="width: 200px;" />
                </el-form-item>
                <el-form-item label="时">
                  <el-input v-model="cronCustom.hour" placeholder="0-23，* 表示每小时" style="width: 200px;" />
                </el-form-item>
                <el-form-item label="日">
                  <el-input v-model="cronCustom.day" placeholder="1-31，? 表示不指定" style="width: 200px;" />
                </el-form-item>
                <el-form-item label="月">
                  <el-input v-model="cronCustom.month" placeholder="1-12，* 表示每月" style="width: 200px;" />
                </el-form-item>
                <el-form-item label="周">
                  <el-input v-model="cronCustom.week" placeholder="1-7 (1=周日)，? 表示不指定" style="width: 200px;" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="generateCronFromCustom">生成Cron表达式</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
          <el-input v-model="formData.cronExpression" placeholder="Cron表达式" style="margin-top: 10px;" maxlength="100" />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            Cron表达式格式：秒 分 时 日 月 周
          </div>
        </el-form-item>
        <el-form-item label="并发数" prop="concurrency">
          <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
            <el-slider
              v-model="formData.concurrency"
              :min="1"
              :max="10"
              :step="1"
              :show-tooltip="false"
              style="flex: 1;"
            />
            <el-input-number
              v-model="formData.concurrency"
              :min="1"
              :max="10"
              :step="1"
              :controls="false"
              style="width: 80px;"
            />
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            并发数范围：1-10，默认为1
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
    
    <!-- 选择用例对话框 -->
    <el-dialog v-model="caseSelectVisible" title="选择用例" width="90%" :close-on-click-modal="false">
      <div class="case-select-toolbar">
        <el-input 
          v-model="caseKeyword" 
          placeholder="搜索用例编号/名称" 
          style="width: 300px;"
          @keyup.enter="searchCases"
          clearable
          @clear="searchCases"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <el-table 
        ref="caseTableRef"
        :data="caseTableData" 
        style="width: 100%"
        @selection-change="handleCaseSelectionChange"
        :header-cell-style="{ background: '#f5f7fa' }"
        row-key="id"
        :reserve-selection="true"
      >
        <el-table-column type="selection" width="55" :reserve-selection="true" />
        <el-table-column prop="caseNo" label="用例编号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="name" label="用例名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="designer" label="设计者" width="100">
          <template #default="scope">
            {{ scope.row.designerNickname || scope.row.designer || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="caseType" label="用例性质" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.caseType === 'positive' ? 'success' : 'warning'" size="small">
              {{ scope.row.caseType === 'positive' ? '正例' : '反例' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="casePageNum"
          v-model:page-size="casePageSize"
          :total="caseTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper, refresh"
          @size-change="handleCaseSizeChange"
          @current-change="handleCaseCurrentChange"
        />
      </div>
      
      <template #footer>
        <el-button @click="caseSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCaseSelection">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看执行历史对话框 -->
    <el-dialog v-model="executionVisible" width="90%" :close-on-click-modal="false">
      <template #header>
        <div class="card-header">
          <span>执行历史</span>
          <el-tag v-if="isRefreshing" type="warning" size="small">
            <el-icon class="is-loading"><Loading /></el-icon> 自动刷新中
          </el-tag>
        </div>
      </template>
      <el-divider>任务执行记录</el-divider>
      <el-table 
        :data="executionList" 
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-class-name="taskExecutionTableRowClassName"
        @row-click="showTaskCaseExecutions"
      >
        <el-table-column prop="taskName" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="executor" label="执行人" width="100">
          <template #default="scope">
            {{ scope.row.executorNickname || scope.row.executor || '-' }}
          </template>
        </el-table-column>
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getExecutionStatusType(scope.row.status)" size="small">
              {{ getExecutionStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务进度" width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-progress
                :percentage="getProgressPercentage(scope.row)"
                :color="getProgressColor(scope.row)"
                :stroke-width="8"
                style="flex: 1;"
              />
            </div>
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
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="execPageNum"
          v-model:page-size="execPageSize"
          :total="execTotal"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper, refresh"
          @size-change="loadExecutions"
          @current-change="loadExecutions"
        />
      </div>
      
      <el-divider v-if="taskCaseExecutionList.length > 0">用例执行记录</el-divider>
      <el-table 
        v-if="taskCaseExecutionList.length > 0"
        :data="taskCaseExecutionList" 
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-class-name="caseExecutionTableRowClassName"
        @row-click="showCaseStepExecutions"
      >
        <el-table-column prop="caseName" label="用例名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="executor" label="执行人" width="90">
          <template #default="scope">
            {{ scope.row.executorNickname || scope.row.executor || '-' }}
          </template>
        </el-table-column>
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getExecutionStatusType(scope.row.status)" size="small">
              {{ getExecutionStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试进度" width="150">
          <template #default="scope">
            <el-progress
              :percentage="getProgressPercentage(scope.row)"
              :color="getProgressColor(scope.row)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="imageAssertionModel" label="图像断言模型" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.imageAssertionModel || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stepFusionModel" label="数据融合模型" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.stepFusionModel || '-' }}
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
      </el-table>

      <div v-if="taskCaseTotal > 0" class="pagination" style="margin-top: 10px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="taskCasePageNum"
          v-model:page-size="taskCasePageSize"
          :total="taskCaseTotal"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTaskCaseExecutions"
          @current-change="loadTaskCaseExecutions"
        />
      </div>
      
      <el-divider v-if="stepExecutionList.length > 0">步骤执行记录</el-divider>
      <el-table 
        v-if="stepExecutionList.length > 0"
        :data="stepExecutionList" 
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="stepNo" label="步骤号" width="70" />
        <el-table-column prop="stepDescription" label="步骤描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="testData" label="测试数据" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.testData || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="执行状态" width="90">
          <template #default="scope">
            <el-tag :type="getStepStatusType(scope.row.status)" size="small">
              {{ getStepStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="执行失败描述" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.errorMessage && scope.row.errorMessage !== '无'" style="color: var(--el-color-danger);">
              {{ scope.row.errorMessage }}
            </span>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="assertionStatus" label="断言状态" width="100">
          <template #default="scope">
            <el-tag :type="getAssertionStatusType(scope.row.assertionStatus)" size="small">
              {{ getAssertionStatusText(scope.row.assertionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiResult" label="AI断言描述" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.aiResult" :class="{'assertion-success': scope.row.aiResult.includes('✅'), 'assertion-failed': scope.row.aiResult.includes('❌'), 'assertion-warning': scope.row.aiResult.includes('⚠️')}">
              {{ scope.row.aiResult }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="scope">
            {{ scope.row.duration ? (scope.row.duration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stepFusionDuration" label="数据融合耗时" width="110">
          <template #default="scope">
            {{ scope.row.stepFusionDuration ? (scope.row.stepFusionDuration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="pageOperationDuration" label="页面操作耗时" width="110">
          <template #default="scope">
            {{ scope.row.pageOperationDuration ? (scope.row.pageOperationDuration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assertionDuration" label="AI断言耗时" width="110">
          <template #default="scope">
            {{ scope.row.assertionDuration ? (scope.row.assertionDuration / 1000).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column width="180">
          <template #header>
            <span>AI token消耗
              <el-tooltip content="数据融合token消耗+页面操作token消耗+AI断言token消耗" placement="top">
                <el-icon style="margin-left: 4px; cursor: pointer; color: var(--el-color-info);"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="scope">
            {{ scope.row.aiTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="stepFusionTokenUsed" label="数据融合token消耗" width="170">
          <template #default="scope">
            {{ scope.row.stepFusionTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="pageOperationTokenUsed" label="页面操作token消耗" width="170">
          <template #default="scope">
            {{ scope.row.pageOperationTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="assertionTokenUsed" label="AI断言token消耗" width="170">
          <template #default="scope">
            {{ scope.row.assertionTokenUsed || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="截图" width="70">
          <template #default="scope">
            <el-button v-if="scope.row.screenshot" size="small" @click.stop="showScreenshot(scope.row.screenshot)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="stepTotal > 0" class="pagination" style="margin-top: 10px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="stepPageNum"
          v-model:page-size="stepPageSize"
          :total="stepTotal"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadStepExecutions"
          @current-change="loadStepExecutions"
        />
      </div>
      
      <template #footer>
        <el-button @click="executionVisible = false">关闭</el-button>
      </template>
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
import { ref, reactive, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Document, List, QuestionFilled, VideoPlay, Loading } from '@element-plus/icons-vue'
import ColumnSettings from '@/components/ColumnSettings.vue'
import request from '@/api/request'
import { getExecutionList, getStepExecutions } from '@/api/testCaseExecution'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(5)
const keyword = ref('')
const selectedIds = ref([])

// 任务列表列配置
const taskTableColumns = ref([
  { prop: 'taskNo', label: '任务编号', visible: true },
  { prop: 'taskName', label: '任务名称', visible: true },
  { prop: 'creator', label: '创建人', visible: true },
  { prop: 'createTime', label: '创建时间', visible: true },
  { prop: 'latestExecuteTime', label: '最新执行时间', visible: true },
  { prop: 'cronExpression', label: '定时策略', visible: true },
  { prop: 'concurrency', label: '并发数', visible: true },
  { prop: 'status', label: '状态', visible: true }
])

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  taskNo: '',
  taskName: '',
  creator: '',
  cronExpression: '',
  concurrency: 1,
  status: 1
})

// Cron表达式生成器
const cronTabType = ref('preset')
const cronPreset = ref('')
const cronCustom = reactive({
  second: '0',
  minute: '0',
  hour: '0',
  day: '?',
  month: '*',
  week: '?'
})

// 选择预设Cron
const onCronPresetChange = (value) => {
  formData.cronExpression = value
}

// 从自定义选项生成Cron表达式
const generateCronFromCustom = () => {
  const { second, minute, hour, day, month, week } = cronCustom
  formData.cronExpression = `${second} ${minute} ${hour} ${day} ${month} ${week}`
}

const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 200, message: '任务名称长度在 2 到 200 个字符', trigger: 'blur' }
  ]
}

// 选择用例对话框
const caseSelectVisible = ref(false)
const currentTask = ref(null)
const caseTableData = ref([])
const caseTotal = ref(0)
const casePageNum = ref(1)
const casePageSize = ref(20)
const caseKeyword = ref('')
const selectedCaseIds = ref([])
const caseTableRef = ref(null)

// 执行历史对话框
const executionVisible = ref(false)
const executionList = ref([])
const execTotal = ref(0)
const execPageNum = ref(1)
const execPageSize = ref(5)
const activeTaskExecutionRow = ref(null)
const taskCaseExecutionList = ref([])
const taskCasePageNum = ref(1)
const taskCasePageSize = ref(5)
const taskCaseTotal = ref(0)
const activeCaseExecutionRow = ref(null)
const stepExecutionList = ref([])
const stepPageNum = ref(1)
const stepPageSize = ref(5)
const stepTotal = ref(0)
const screenshotVisible = ref(false)
const currentScreenshot = ref('')
const refreshTimer = ref(null)
const isRefreshing = ref(false)

// 获取执行状态类型
const getExecutionStatusType = (status) => {
  const map = {
    pending: 'info',
    running: 'primary',
    success: 'success',
    failed: 'danger',
    stopped: 'warning'
  }
  return map[status] || 'info'
}

// 获取执行状态文本
const getExecutionStatusText = (status) => {
  const map = {
    pending: '待执行',
    running: '执行中',
    success: '成功',
    failed: '失败',
    stopped: '已停止'
  }
  return map[status] || status
}

// 获取步骤执行状态对应的tag类型
const getStepStatusType = (status) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  if (status === 'pending') return 'info'
  return 'info'
}

// 获取步骤执行状态显示文本
const getStepStatusText = (status) => {
  if (status === 'success') return '成功'
  if (status === 'failed') return '失败'
  if (status === 'pending') return '未执行'
  return status || '-'
}

// 获取断言状态对应的tag类型
const getAssertionStatusType = (status) => {
  if (status === 'success') return 'success'
  if (status === 'none') return 'info'
  if (status === 'failed') return 'danger'
  return 'info'
}

// 获取断言状态显示文本
const getAssertionStatusText = (status) => {
  if (status === 'success') return '断言成功'
  if (status === 'none') return '无'
  if (status === 'failed') return '断言失败'
  return status || '-'
}

// 任务执行记录表格行样式
const taskExecutionTableRowClassName = ({ row }) => {
  return row.id === activeTaskExecutionRow.value ? 'success-row' : ''
}

// 用例执行记录表格行样式
const caseExecutionTableRowClassName = ({ row }) => {
  return row.id === activeCaseExecutionRow.value ? 'success-row' : ''
}

// 点击任务执行记录，加载该任务执行记录关联的用例执行记录
const showTaskCaseExecutions = async (row) => {
  activeTaskExecutionRow.value = row.id
  taskCaseExecutionList.value = []
  taskCasePageNum.value = 1
  stepExecutionList.value = []
  activeCaseExecutionRow.value = null
  
  try {
    // 直接获取该任务执行记录关联的用例执行记录
    const res = await getExecutionList({ pageNum: taskCasePageNum.value, pageSize: taskCasePageSize.value, taskExecutionId: row.id })
    if (res.code === 200 && res.data && res.data.records) {
      taskCaseExecutionList.value = res.data.records
      taskCaseTotal.value = res.data.total || 0
      // 自动选中第一条
      if (taskCaseExecutionList.value.length > 0) {
        await showCaseStepExecutions(taskCaseExecutionList.value[0])
      }
    }
  } catch (error) {
    console.error('加载任务用例执行记录失败:', error)
  }
}

// 加载任务用例执行记录分页
const loadTaskCaseExecutions = async () => {
  if (!activeTaskExecutionRow.value) return
  try {
    const res = await getExecutionList({ pageNum: taskCasePageNum.value, pageSize: taskCasePageSize.value, taskExecutionId: activeTaskExecutionRow.value })
    if (res.code === 200 && res.data && res.data.records) {
      taskCaseExecutionList.value = res.data.records
      taskCaseTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载任务用例执行记录失败:', error)
  }
}

// 点击用例执行记录，加载步骤执行记录
const showCaseStepExecutions = async (row) => {
  activeCaseExecutionRow.value = row.id
  stepPageNum.value = 1
  try {
    const res = await getStepExecutions(row.id, stepPageNum.value, stepPageSize.value)
    if (res.code === 200) {
      if (res.data.records) {
        stepExecutionList.value = res.data.records
        stepTotal.value = res.data.total || 0
      } else {
        stepExecutionList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
        stepTotal.value = stepExecutionList.value.length
      }
    }
  } catch (error) {
    ElMessage.error('加载步骤执行记录失败')
  }
}

// 加载步骤执行记录分页
const loadStepExecutions = async () => {
  if (!activeCaseExecutionRow.value) return
  try {
    const res = await getStepExecutions(activeCaseExecutionRow.value, stepPageNum.value, stepPageSize.value)
    if (res.code === 200) {
      if (res.data.records) {
        stepExecutionList.value = res.data.records
        stepTotal.value = res.data.total || 0
      } else {
        stepExecutionList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
        stepTotal.value = stepExecutionList.value.length
      }
    }
  } catch (error) {
    ElMessage.error('加载步骤执行记录失败')
  }
}

// 显示截图
const showScreenshot = (screenshot) => {
  // 检查是否是默认的占位图（绿色小方块）
  if (!screenshot || screenshot.includes("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")) {
    ElMessage.warning("暂无截图")
    return
  }
  currentScreenshot.value = screenshot
  screenshotVisible.value = true
}

// ==================== 选择用例相关 ====================
const openCaseSelectDialog = async (task) => {
  currentTask.value = task
  caseSelectVisible.value = true
  casePageNum.value = 1
  caseKeyword.value = ''
  selectedCaseIds.value = []
  
  await Promise.all([
    fetchCaseList(),
    fetchSelectedCaseIds()
  ])
}

const fetchCaseList = async () => {
  try {
    const res = await request.get('/case/list', {
      params: {
        pageNum: casePageNum.value,
        pageSize: casePageSize.value,
        keyword: caseKeyword.value
      }
    })
    if (res.code === 200) {
      caseTableData.value = res.data.records || []
      caseTotal.value = res.data.total || 0
      
      // 自动勾选已关联的用例
      await nextTick()
      if (caseTableRef.value && selectedCaseIds.value.length > 0) {
        caseTableData.value.forEach(row => {
          if (selectedCaseIds.value.includes(row.id)) {
            caseTableRef.value.toggleRowSelection(row, true)
          }
        })
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  }
}

const fetchSelectedCaseIds = async () => {
  if (!currentTask.value) return
  try {
    const res = await request.get(`/test-task/${currentTask.value.id}/case-ids`)
    if (res.code === 200) {
      selectedCaseIds.value = res.data || []
    }
  } catch (error) {
    console.error('获取关联用例失败:', error)
  }
}

const searchCases = () => {
  casePageNum.value = 1
  fetchCaseList()
}

const handleCaseSelectionChange = (selection) => {
  selectedCaseIds.value = selection.map(item => item.id)
}

const handleCaseSizeChange = (val) => {
  casePageSize.value = val
  fetchCaseList()
}

const handleCaseCurrentChange = (val) => {
  casePageNum.value = val
  fetchCaseList()
}

const saveCaseSelection = async () => {
  if (!currentTask.value) return
  try {
    const res = await request.post(`/test-task/${currentTask.value.id}/case-ids`, selectedCaseIds.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      caseSelectVisible.value = false
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// ==================== 执行历史相关 ====================
const openExecutionDialog = (task) => {
  currentTask.value = task
  executionVisible.value = true
  execPageNum.value = 1
  activeTaskExecutionRow.value = null
  taskCaseExecutionList.value = []
  activeCaseExecutionRow.value = null
  stepExecutionList.value = []
  loadExecutions()
  startAutoRefresh()
}

// 启动自动刷新（每5秒刷新一次）
const startAutoRefresh = () => {
  stopAutoRefresh()
  isRefreshing.value = true
  refreshTimer.value = setInterval(() => {
    loadExecutions()
    if (activeTaskExecutionRow.value) {
      // 如果当前选中的任务执行记录状态是running，也刷新用例执行记录
      const activeExecution = executionList.value.find(e => e.id === activeTaskExecutionRow.value)
      if (activeExecution && activeExecution.status === 'running') {
        showTaskCaseExecutions(activeExecution)
      }
    }
  }, 5000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
  isRefreshing.value = false
}

const loadExecutions = async () => {
  if (!currentTask.value) return
  try {
    const res = await request.get('/test-task-execution/page', {
      params: {
        pageNum: execPageNum.value,
        pageSize: execPageSize.value,
        taskId: currentTask.value.id
      }
    })
    if (res.code === 200) {
      executionList.value = res.data.records || []
      execTotal.value = res.data.total || 0
      // 自动选中第一条（最新）执行记录
      if (executionList.value.length > 0) {
        // 如果当前没有选中，或者选中的不是第一条，就选中第一条
        if (!activeTaskExecutionRow.value || activeTaskExecutionRow.value !== executionList.value[0].id) {
          await showTaskCaseExecutions(executionList.value[0])
        }
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  }
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

// 监听执行历史对话框关闭，停止自动刷新
watch(executionVisible, (newVal) => {
  if (!newVal) {
    stopAutoRefresh()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})

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
      concurrency: 1,
      status: 1
    })
  } else if (type === 'edit' && row) {
    Object.assign(formData, {
      id: row.id,
      taskNo: row.taskNo,
      taskName: row.taskName,
      creator: row.creator,
      cronExpression: row.cronExpression || '',
      concurrency: row.concurrency || 1,
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

// 批量立刻执行
const batchRun = () => {
  ElMessageBox.confirm(
    `确定要立刻执行选中的 ${selectedIds.value.length} 个任务吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.post('/test-task/run/batch', selectedIds.value)
      if (res.code === 200) {
        ElMessage.success('已开始执行，请在执行历史中查看结果')
        selectedIds.value = []
      } else {
        ElMessage.error(res.message || '执行失败')
      }
    } catch (error) {
      console.error('执行失败:', error)
      ElMessage.error('执行失败')
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

/**
 * 获取测试进度百分比
 * @param row 执行记录行
 * @returns 进度百分比
 */
const getProgressPercentage = (row) => {
  if (!row.totalCount || row.totalCount === 0) return 0
  const completedCount = (row.passedCount || 0) + (row.failedCount || 0)
  return Math.round((completedCount / row.totalCount) * 100)
}

/**
 * 获取测试进度条颜色
 * @param row 执行记录行
 * @returns 进度条颜色
 */
const getProgressColor = (row) => {
  const percentage = getProgressPercentage(row)
  if (row.status === 'success') return '#67c23a' // 绿色
  if (row.status === 'failed') return '#f56c6c' // 红色
  if (percentage >= 80) return '#67c23a' // 80%以上绿色
  if (percentage >= 50) return '#e6a23c' // 50-80%橙色
  return '#409eff' // 默认蓝色
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
  
  .case-select-toolbar {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

:deep(.el-table .success-row) {
  background-color: #f0f9eb;
}

.assertion-success {
  color: #67c23a;
}

.assertion-failed {
  color: #f56c6c;
}

.assertion-warning {
  color: #e6a23c;
}

.text-muted {
  color: #909399;
}
</style>
