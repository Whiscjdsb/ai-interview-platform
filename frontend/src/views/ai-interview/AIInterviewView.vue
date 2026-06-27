<template>
  <div class="interview-setup-page">
    <section class="setup-hero">
      <div class="setup-hero__copy">
        <span class="setup-eyebrow">AI Interview Setup</span>
        <h1>AI 模拟面试</h1>
        <p>
          选择岗位、难度和面试模式，生成一场贴近真实面试的练习。支持 DeepSeek 智能评分、
          多轮追问、ScoreEngine 统一评分和面试报告沉淀。
        </p>
        <div class="setup-hero__actions">
          <el-button type="primary" size="large" :loading="loading" @click="startInterview">
            {{ loading ? '正在创建面试...' : '开始 AI 模拟面试' }}
          </el-button>
          <el-button size="large" @click="router.push('/ai-interview/history')">查看历史</el-button>
        </div>
      </div>

      <div class="setup-hero__preview">
        <div class="preview-card">
          <span>当前配置</span>
          <strong>{{ form.position || '待选择岗位' }}</strong>
          <p>{{ difficultyLabelText }} / {{ form.questionCount }} 道题 / {{ modeLabel }}</p>
        </div>
        <div class="preview-meta">
          <span>DeepSeek / Mock Provider</span>
          <span>多轮追问</span>
          <span>PDF 报告</span>
        </div>
      </div>
    </section>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section class="setup-grid">
      <div class="config-panel panel">
        <div class="section-heading">
          <div>
            <span>Configuration</span>
            <h2>面试配置</h2>
          </div>
          <p>字段保持与现有创建接口一致，配置完成后会进入面试会话页。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="setup-form">
          <el-form-item label="目标岗位" prop="position">
            <el-input v-model.trim="form.position" maxlength="128" placeholder="例如：Java 后端开发工程师" />
            <p class="field-help">用于生成贴近岗位要求的面试题。</p>
          </el-form-item>

          <div class="form-row">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="选择难度">
                <el-option
                  v-for="option in DIFFICULTY_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <p class="field-help">中等难度适合多数演示和日常练习。</p>
            </el-form-item>

            <el-form-item label="题目数量" prop="questionCount">
              <el-input-number v-model="form.questionCount" :min="1" :max="10" />
              <p class="field-help">建议 5 道题，能完整展示生成、追问和评分流程。</p>
            </el-form-item>
          </div>

          <el-form-item label="关注方向">
            <el-select
              v-model="form.focusTags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="例如：Java 基础、Spring Boot、MySQL、Redis"
            >
              <el-option v-for="tag in defaultTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
            <p class="field-help">会影响模拟题目方向，支持输入自定义知识点。</p>
          </el-form-item>

          <div class="form-row">
            <el-form-item label="面试官类型">
              <el-select v-model="form.interviewerType" clearable placeholder="默认普通面试官">
                <el-option
                  v-for="option in interviewerOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <p class="field-help">用于企业面试原型，不选择时走普通模拟面试。</p>
            </el-form-item>

            <el-form-item label="岗位模型">
              <el-input v-model.trim="form.positionModel" maxlength="128" placeholder="例如：后端工程师 / 平台开发" />
              <p class="field-help">可选字段，用于企业场景和岗位匹配演示。</p>
            </el-form-item>
          </div>

          <div class="pressure-row">
            <div>
              <strong>压力追问模式</strong>
              <p>开启后会把压力面试意图传给后端，适合企业面试原型演示。</p>
            </div>
            <el-switch v-model="form.pressureMode" />
          </div>

          <el-button type="primary" :loading="loading" class="start-button" @click="startInterview">
            {{ loading ? '正在创建面试...' : '开始 AI 模拟面试' }}
          </el-button>
        </el-form>
      </div>

      <aside class="setup-side">
        <section class="recommend-panel panel">
          <div class="section-heading section-heading--compact">
            <div>
              <span>Presets</span>
              <h2>推荐配置</h2>
            </div>
          </div>

          <button
            v-for="preset in presets"
            :key="preset.title"
            type="button"
            class="preset-card"
            @click="applyPreset(preset)"
          >
            <strong>{{ preset.title }}</strong>
            <p>{{ preset.desc }}</p>
            <span>{{ preset.position }} / {{ preset.questionCount }} 道题</span>
          </button>
        </section>

        <section class="mode-panel panel">
          <div class="section-heading section-heading--compact">
            <div>
              <span>Mode Guide</span>
              <h2>模式说明</h2>
            </div>
          </div>

          <div class="mode-list">
            <div v-for="item in modeGuides" :key="item.title">
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </section>
      </aside>
    </section>

    <section class="flow-panel panel">
      <div class="section-heading">
        <div>
          <span>Interview Flow</span>
          <h2>面试流程预览</h2>
        </div>
        <p>创建后会进入会话页，完成作答后生成结构化结果报告。</p>
      </div>

      <div class="flow-steps">
        <div v-for="(step, index) in flowSteps" :key="step.title" class="flow-step">
          <span>{{ index + 1 }}</span>
          <strong>{{ step.title }}</strong>
          <p>{{ step.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

import { createInterview } from '@/api/ai-interview'
import type { CreateInterviewPayload, EnterpriseInterviewerType } from '@/types/ai-interview'
import { saveInterviewSession } from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'
import { DIFFICULTY_OPTIONS, difficultyLabel } from '@/utils/question'

interface InterviewPreset {
  title: string
  desc: string
  position: string
  difficulty: CreateInterviewPayload['difficulty']
  focusTags: string[]
  questionCount: number
  interviewerType?: EnterpriseInterviewerType
  positionModel?: string
  pressureMode?: boolean
}

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const errorText = ref('')
const defaultTags = ['Java 基础', 'Spring Boot', 'MySQL', 'Redis', '并发编程', 'JVM']

const form = reactive<CreateInterviewPayload>({
  position: 'Java 后端开发工程师',
  difficulty: 'MEDIUM',
  focusTags: ['Java 基础', 'Spring Boot', 'MySQL'],
  questionCount: 5
})

const interviewerOptions: Array<{ label: string; value: EnterpriseInterviewerType }> = [
  { label: 'ALIBABA 严谨技术官', value: 'ALIBABA' },
  { label: 'TENCENT 综合能力面试官', value: 'TENCENT' },
  { label: 'BYTEDANCE 高压算法面试官', value: 'BYTEDANCE' },
  { label: 'STARTUP 灵活实战面试官', value: 'STARTUP' }
]

const presets: InterviewPreset[] = [
  {
    title: 'Java 后端标准面试',
    desc: '适合完整演示创建、追问、评分和报告流程。',
    position: 'Java 后端开发工程师',
    difficulty: 'MEDIUM',
    focusTags: ['Java 基础', 'Spring Boot', 'MySQL'],
    questionCount: 5
  },
  {
    title: 'Spring Boot 专项',
    desc: '聚焦自动配置、Web 开发、参数校验和项目落地。',
    position: 'Spring Boot 后端开发',
    difficulty: 'MEDIUM',
    focusTags: ['Spring Boot', 'Spring MVC', 'MyBatis-Plus'],
    questionCount: 5,
    interviewerType: 'TENCENT',
    positionModel: '后端工程师'
  },
  {
    title: 'MySQL + Redis 高频题',
    desc: '适合展示数据库、缓存和实战排障能力。',
    position: 'Java 中级后端工程师',
    difficulty: 'MEDIUM',
    focusTags: ['MySQL', 'Redis', '项目实战排障'],
    questionCount: 6,
    interviewerType: 'ALIBABA',
    positionModel: '平台开发'
  },
  {
    title: '企业高压模拟',
    desc: '开启压力追问，展示企业面试原型能力。',
    position: '后端平台研发工程师',
    difficulty: 'HARD',
    focusTags: ['并发编程', 'JVM', '系统设计'],
    questionCount: 6,
    interviewerType: 'BYTEDANCE',
    positionModel: '平台研发',
    pressureMode: true
  }
]

const modeGuides = [
  { title: '普通模拟面试', desc: '适合日常练习，不选择企业面试官时默认使用。' },
  { title: '企业面试原型', desc: '选择面试官类型和岗位模型后，可模拟不同企业风格。' },
  { title: 'Mock 模式', desc: '适合本地演示，不依赖真实 AI Key，流程稳定可控。' },
  { title: 'DeepSeek 模式', desc: '配置 Provider 与 API Key 后，可体验真实 AI 评分。' }
]

const flowSteps = [
  { title: '选择配置', desc: '设置岗位、难度、题数和关注方向' },
  { title: '生成题目', desc: '后端根据配置返回结构化面试题' },
  { title: '回答问题', desc: '逐题作答并保存当前面试进度' },
  { title: 'AI 追问', desc: '基于回答生成下一轮深入追问' },
  { title: '评分报告', desc: '查看总分、优点、不足和建议' },
  { title: '导出分享', desc: '生成 PDF 或公开分享链接' }
]

const rules: FormRules<CreateInterviewPayload> = {
  position: [{ required: true, message: '请输入目标岗位', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  questionCount: [{ required: true, message: '请选择题目数量', trigger: 'change' }]
}

const difficultyLabelText = computed(() => difficultyLabel(form.difficulty))
const modeLabel = computed(() => (form.interviewerType ? '企业面试原型' : '普通模拟面试'))

function applyPreset(preset: InterviewPreset) {
  form.position = preset.position
  form.difficulty = preset.difficulty
  form.focusTags = [...preset.focusTags]
  form.questionCount = preset.questionCount
  form.interviewerType = preset.interviewerType
  form.positionModel = preset.positionModel
  form.pressureMode = preset.pressureMode
  errorText.value = ''
}

async function startInterview() {
  if (!formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  errorText.value = ''
  try {
    const interview = await createInterview(form)
    saveInterviewSession({
      interview: { ...interview, status: 'IN_PROGRESS' },
      answers: {}
    })
    await router.push(`/ai-interview/session/${interview.id}`)
  } catch (error) {
    errorText.value = errorMessage(error, '创建 AI 模拟面试失败，请检查后端服务或稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.interview-setup-page {
  display: grid;
  gap: var(--ai-space-3);
}

.setup-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(300px, 0.75fr);
  gap: var(--ai-space-3);
  border: 1px solid rgba(191, 219, 254, 0.75);
  border-radius: var(--ai-radius-lg);
  background:
    radial-gradient(circle at 84% 18%, rgba(124, 58, 237, 0.24), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, var(--ai-color-primary) 100%);
  padding: 32px;
  color: #ffffff;
  box-shadow: 0 18px 44px rgba(37, 99, 235, 0.22);
}

.setup-hero__copy {
  align-self: center;
}

.setup-eyebrow,
.section-heading span {
  display: inline-flex;
  border-radius: var(--ai-radius-pill);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.setup-eyebrow {
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 11px;
}

.setup-hero h1 {
  margin: 16px 0 12px;
  color: #ffffff;
  font-size: 38px;
  line-height: 1.16;
}

.setup-hero p {
  margin: 0;
  max-width: 720px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 16px;
  line-height: 1.8;
}

.setup-hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.setup-hero__preview {
  align-self: stretch;
  display: grid;
  gap: 12px;
}

.preview-card,
.preview-meta span {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: var(--ai-radius-md);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
}

.preview-card {
  padding: 20px;
}

.preview-card span,
.preview-card p {
  color: rgba(255, 255, 255, 0.72);
}

.preview-card strong {
  display: block;
  margin: 10px 0;
  color: #ffffff;
  font-size: 26px;
  line-height: 1.25;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-meta span {
  padding: 8px 10px;
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--ai-font-size-sm);
}

.setup-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 0.8fr);
  gap: var(--ai-space-3);
}

.config-panel,
.recommend-panel,
.mode-panel,
.flow-panel {
  padding: 22px;
}

.setup-side {
  display: grid;
  gap: var(--ai-space-3);
  align-content: start;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--ai-space-2);
  margin-bottom: var(--ai-space-2);
}

.section-heading--compact {
  display: block;
}

.section-heading span {
  background: var(--ai-color-primary-soft);
  color: var(--ai-color-primary);
  padding: 4px 10px;
}

.section-heading h2 {
  margin: 8px 0 0;
}

.section-heading p {
  margin: 0;
  max-width: 500px;
  color: var(--ai-text-muted);
}

.setup-form {
  display: grid;
  gap: 6px;
}

.setup-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.field-help {
  margin: 6px 0 0;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
  line-height: 1.6;
}

.pressure-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-2);
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: var(--ai-bg-card-muted);
  padding: 16px;
  margin-bottom: 18px;
}

.pressure-row strong,
.pressure-row p {
  display: block;
}

.pressure-row p {
  margin: 5px 0 0;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
}

.start-button {
  width: 100%;
  min-height: 44px;
}

.preset-card {
  width: 100%;
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: linear-gradient(180deg, #ffffff 0%, var(--ai-bg-card-muted) 100%);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition:
    transform var(--ai-transition-fast),
    box-shadow var(--ai-transition-fast),
    border-color var(--ai-transition-fast);
}

.preset-card + .preset-card {
  margin-top: 12px;
}

.preset-card:hover {
  border-color: var(--ai-color-primary-subtle);
  box-shadow: var(--ai-shadow-hover);
  transform: translateY(-2px);
}

.preset-card strong,
.preset-card p,
.preset-card span,
.mode-list strong,
.mode-list p {
  display: block;
}

.preset-card p,
.mode-list p {
  margin: 6px 0 0;
  color: var(--ai-text-muted);
  line-height: 1.65;
}

.preset-card span {
  margin-top: 10px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-sm);
  font-weight: 700;
}

.mode-list {
  display: grid;
  gap: 12px;
}

.mode-list div {
  border-left: 3px solid var(--ai-color-primary);
  border-radius: var(--ai-radius-sm);
  background: var(--ai-bg-card-muted);
  padding: 12px 14px;
}

.flow-steps {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.flow-step {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: #ffffff;
  padding: 16px;
}

.flow-step span {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  margin-bottom: 12px;
  border-radius: var(--ai-radius-pill);
  background: var(--ai-color-primary);
  color: #ffffff;
  font-weight: 800;
}

.flow-step strong,
.flow-step p {
  display: block;
}

.flow-step p {
  margin: 8px 0 0;
  color: var(--ai-text-muted);
  line-height: 1.65;
}

.el-alert {
  margin-bottom: 0;
}

@media (max-width: 1360px) {
  .flow-steps {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .setup-hero,
  .setup-grid,
  .form-row {
    grid-template-columns: 1fr;
  }

  .section-heading {
    display: block;
  }
}

@media (max-width: 640px) {
  .setup-hero {
    padding: 20px;
  }

  .setup-hero h1 {
    font-size: 28px;
  }

  .flow-steps {
    grid-template-columns: 1fr;
  }

  .pressure-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
