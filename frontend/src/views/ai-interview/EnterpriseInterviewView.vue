<template>
  <div class="enterprise-page">
    <div class="page-header">
      <div>
        <h1>企业级模拟面试</h1>
        <p>选择企业面试官风格、岗位模型和评分权重，生成更贴近真实招聘场景的 AI 面试。</p>
      </div>
      <el-button @click="router.push('/ai-interview')">返回 AI 面试助手</el-button>
    </div>

    <section class="enterprise-grid">
      <div class="panel setup-panel">
        <h2>选择企业风格</h2>
        <div class="company-grid">
          <button
            v-for="company in companyOptions"
            :key="company.value"
            class="company-card"
            :class="{ active: form.interviewerType === company.value }"
            type="button"
            @click="form.interviewerType = company.value"
          >
            <span class="company-logo" :style="{ background: company.color }">{{ company.shortName }}</span>
            <strong>{{ company.label }}</strong>
            <small>{{ company.description }}</small>
          </button>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="enterprise-form">
          <el-form-item label="岗位模型" prop="positionModel">
            <el-input v-model.trim="form.positionModel" maxlength="128" placeholder="例如：高级 Java 后端工程师" />
          </el-form-item>
          <el-form-item label="岗位名称" prop="position">
            <el-input v-model.trim="form.position" maxlength="128" placeholder="例如：Java 后端开发工程师" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty">
                <el-option
                  v-for="option in DIFFICULTY_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="题目数量" prop="questionCount">
              <el-input-number v-model="form.questionCount" :min="1" :max="10" />
            </el-form-item>
          </div>
          <el-form-item label="关注方向">
            <el-select v-model="form.focusTags" multiple filterable allow-create default-first-option>
              <el-option v-for="tag in defaultTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-switch v-model="form.pressureMode" active-text="开启压力模式（倒计时 + 高频追问）" />
          </el-form-item>

          <div class="actions">
            <el-button :loading="templateLoading" @click="saveTemplate">保存为模板</el-button>
            <el-button type="primary" :loading="startLoading" @click="startEnterpriseInterview">
              开始企业模拟面试
            </el-button>
          </div>
        </el-form>
      </div>

      <div class="side-stack">
        <div class="panel scoring-panel">
          <h2>企业评分模型</h2>
          <div v-for="item in scoringItems" :key="item.key" class="weight-row">
            <span>{{ item.label }}</span>
            <el-progress :percentage="item.value" :show-text="false" />
            <strong>{{ item.value }}%</strong>
          </div>
        </div>

        <div class="panel fit-panel">
          <div class="panel-title-row">
            <h2>岗位匹配度</h2>
            <el-button link type="primary" :loading="fitLoading" @click="loadFitAnalysis">刷新</el-button>
          </div>
          <el-empty v-if="!fitAnalysis && !fitLoading" description="暂无匹配分析，完成面试后会更准确" />
          <template v-else-if="fitAnalysis">
            <div class="fit-score">{{ fitAnalysis.fitScore }}</div>
            <p>{{ fitAnalysis.recommendation }}</p>
            <div class="gap-tags">
              <el-tag v-for="area in fitAnalysis.gapAreas" :key="area" type="warning">{{ area }}</el-tag>
            </div>
          </template>
        </div>

        <div class="panel template-panel">
          <div class="panel-title-row">
            <h2>我的企业模板</h2>
            <el-button link type="primary" :loading="templateListLoading" @click="loadTemplates">刷新</el-button>
          </div>
          <el-empty v-if="templates.length === 0 && !templateListLoading" description="暂未保存模板" />
          <div v-for="template in templates" :key="template.id" class="template-item" @click="applyTemplate(template)">
            <strong>{{ template.positionModel }}</strong>
            <span>{{ companyLabel(template.companyType) }} · {{ template.difficulty }} · {{ template.questionCount }} 题</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

import {
  createInterview,
  createInterviewTemplate,
  getEnterpriseFitAnalysis,
  getInterviewTemplates
} from '@/api/ai-interview'
import type {
  CreateInterviewPayload,
  EnterpriseFitAnalysis,
  EnterpriseInterviewerType,
  InterviewTemplate,
  InterviewTemplatePayload
} from '@/types/ai-interview'
import { saveInterviewSession } from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'
import { DIFFICULTY_OPTIONS } from '@/utils/question'

const router = useRouter()
const formRef = ref<FormInstance>()
const startLoading = ref(false)
const templateLoading = ref(false)
const templateListLoading = ref(false)
const fitLoading = ref(false)
const templates = ref<InterviewTemplate[]>([])
const fitAnalysis = ref<EnterpriseFitAnalysis | null>(null)

const companyOptions: Array<{
  value: EnterpriseInterviewerType
  label: string
  shortName: string
  color: string
  description: string
}> = [
  { value: 'ALIBABA', label: 'Alibaba 严谨技术官', shortName: 'Ali', color: '#ff6a00', description: '深挖架构、稳定性和技术细节' },
  { value: 'TENCENT', label: 'Tencent 综合面试官', shortName: 'Ten', color: '#1d9bf0', description: '综合评估技术、协作和表达' },
  { value: 'BYTEDANCE', label: 'ByteDance 高压算法官', shortName: 'Byte', color: '#111827', description: '高频追问、压力测试和性能思维' },
  { value: 'STARTUP', label: 'Startup 实战面试官', shortName: 'Start', color: '#16a34a', description: '关注落地、速度和业务取舍' }
]

const defaultTags = ['Java 基础', 'Spring Boot', 'MySQL', 'Redis', '系统设计', '项目经验']
const scoringWeights = {
  java: 20,
  spring: 20,
  systemDesign: 25,
  project: 20,
  communication: 15
}
const form = reactive<CreateInterviewPayload>({
  position: 'Java 后端开发工程师',
  positionModel: '高级 Java 后端工程师',
  difficulty: 'MEDIUM',
  focusTags: ['Java 基础', 'Spring Boot', 'MySQL'],
  questionCount: 5,
  interviewerType: 'ALIBABA',
  pressureMode: true
})

const rules: FormRules<CreateInterviewPayload> = {
  position: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  positionModel: [{ required: true, message: '请输入岗位模型', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  questionCount: [{ required: true, message: '请选择题目数量', trigger: 'change' }]
}

const scoringItems = computed(() => [
  { key: 'java', label: 'Java 基础', value: scoringWeights.java },
  { key: 'spring', label: 'Spring', value: scoringWeights.spring },
  { key: 'systemDesign', label: '系统设计', value: scoringWeights.systemDesign },
  { key: 'project', label: '项目经验', value: scoringWeights.project },
  { key: 'communication', label: '表达能力', value: scoringWeights.communication }
])

onMounted(() => {
  loadTemplates()
  loadFitAnalysis()
})

async function startEnterpriseInterview() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  startLoading.value = true
  try {
    const interview = await createInterview(form)
    saveInterviewSession({
      interview: { ...interview, status: 'IN_PROGRESS' },
      answers: {}
    })
    await router.push(`/ai-interview/session/${interview.id}`)
  } catch (error) {
    ElMessage.error(errorMessage(error, '创建企业模拟面试失败'))
  } finally {
    startLoading.value = false
  }
}

async function saveTemplate() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  templateLoading.value = true
  try {
    const payload: InterviewTemplatePayload = {
      positionModel: form.positionModel || form.position,
      companyType: form.interviewerType || 'STARTUP',
      difficulty: form.difficulty,
      questionCount: form.questionCount,
      focusAreas: form.focusTags,
      scoringWeights
    }
    await createInterviewTemplate(payload)
    ElMessage.success('模板已保存')
    await loadTemplates()
  } catch (error) {
    ElMessage.error(errorMessage(error, '保存企业模板失败'))
  } finally {
    templateLoading.value = false
  }
}

async function loadTemplates() {
  templateListLoading.value = true
  try {
    templates.value = await getInterviewTemplates()
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载企业模板失败'))
  } finally {
    templateListLoading.value = false
  }
}

async function loadFitAnalysis() {
  fitLoading.value = true
  try {
    fitAnalysis.value = await getEnterpriseFitAnalysis({
      positionModel: form.positionModel,
      companyType: form.interviewerType
    })
  } catch {
    fitAnalysis.value = null
  } finally {
    fitLoading.value = false
  }
}

function applyTemplate(template: InterviewTemplate) {
  form.positionModel = template.positionModel
  form.position = template.positionModel
  form.interviewerType = template.companyType
  form.difficulty = template.difficulty
  form.questionCount = template.questionCount
  form.focusTags = [...template.focusAreas]
  ElMessage.success('已应用模板')
}

function companyLabel(type: EnterpriseInterviewerType) {
  return companyOptions.find((item) => item.value === type)?.label || type
}
</script>

<style scoped>
.enterprise-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
}

.setup-panel,
.side-stack .panel {
  padding: 22px;
}

.company-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 16px 0 22px;
}

.company-card {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 8px 12px;
  align-items: center;
  padding: 14px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.company-card.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.company-logo {
  grid-row: span 2;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  color: #fff;
  font-weight: 700;
}

.company-card strong,
.company-card small {
  min-width: 0;
}

.company-card small {
  color: #64748b;
}

.enterprise-form {
  margin-top: 10px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.side-stack {
  display: grid;
  gap: 18px;
}

.panel-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.weight-row {
  display: grid;
  grid-template-columns: 82px 1fr 42px;
  gap: 10px;
  align-items: center;
  margin: 14px 0;
}

.fit-score {
  font-size: 52px;
  font-weight: 800;
  color: #2563eb;
  line-height: 1;
}

.gap-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.template-item {
  display: grid;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
}

.template-item + .template-item {
  margin-top: 10px;
}

.template-item span {
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1080px) {
  .enterprise-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .company-grid,
  .form-row {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
