<template>
  <div>
    <div class="page-header">
      <div>
        <h1>{{ isEdit ? '编辑题目' : '新增题目' }}</h1>
        <p>维护题目内容、参考答案、解析和知识标签。</p>
      </div>
      <el-button @click="router.push('/admin/questions')">返回列表</el-button>
    </div>

    <section v-loading="loading" class="form-panel panel">
      <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model.trim="form.title" maxlength="255" />
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="form.questionType">
            <el-option v-for="option in QUESTION_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty">
            <el-option v-for="option in DIFFICULTY_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="选项">
          <el-input v-model="form.optionsText" type="textarea" :rows="4" placeholder="选择题可填写：A. ...&#10;B. ..." />
        </el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-input v-model="form.correctAnswer" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple filterable placeholder="选择标签">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
          </el-select>
        </el-form-item>
        <div class="form-actions">
          <el-button @click="router.push('/admin/questions')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

import { createAdminQuestionApi, getAdminQuestionApi, updateAdminQuestionApi } from '@/api/admin'
import { listTagsApi } from '@/api/question'
import type { AdminQuestionFormModel, AdminQuestionPayload, AdminTag } from '@/types/admin'
import { errorMessage } from '@/utils/error'
import { DIFFICULTY_OPTIONS, QUESTION_TYPE_OPTIONS } from '@/utils/question'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const errorText = ref('')
const tags = ref<AdminTag[]>([])
const isEdit = computed(() => Boolean(route.params.id))
const form = reactive<AdminQuestionFormModel>({
  title: '',
  content: '',
  optionsText: '',
  questionType: 'SHORT_ANSWER',
  difficulty: 'MEDIUM',
  correctAnswer: '',
  analysis: '',
  tagIds: []
})

const rules: FormRules<AdminQuestionFormModel> = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  correctAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
}

onMounted(async () => {
  await Promise.all([loadTags(), loadQuestion()])
})

async function loadTags() {
  tags.value = await listTagsApi()
}

async function loadQuestion() {
  if (!isEdit.value) {
    return
  }
  loading.value = true
  errorText.value = ''
  try {
    const question = await getAdminQuestionApi(route.params.id as string)
    form.title = question.title
    form.content = question.content
    form.optionsText = ''
    form.questionType = question.questionType
    form.difficulty = question.difficulty
    form.correctAnswer = question.correctAnswer || ''
    form.analysis = question.analysis || ''
    form.tagIds = question.tags.map((tag) => tag.id)
  } catch (error) {
    errorText.value = errorMessage(error, '加载题目失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  saving.value = true
  try {
    const payload: AdminQuestionPayload = {
      title: form.title,
      content: [form.content, form.optionsText].filter(Boolean).join('\n'),
      questionType: form.questionType,
      difficulty: form.difficulty,
      correctAnswer: form.correctAnswer,
      analysis: form.analysis,
      tagIds: form.tagIds
    }
    if (isEdit.value) {
      await updateAdminQuestionApi(route.params.id as string, payload)
    } else {
      await createAdminQuestionApi(payload)
    }
    ElMessage.success('保存成功')
    await router.push('/admin/questions')
  } catch (error) {
    ElMessage.error(errorMessage(error, '保存题目失败'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-panel {
  padding: 22px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-alert {
  margin-bottom: 16px;
}
</style>
