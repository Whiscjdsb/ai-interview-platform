<template>
  <div>
    <section v-if="result" class="result-panel panel">
      <div class="result-header">
        <div>
          <h1>{{ result.position }} 面试结果</h1>
          <p>{{ difficultyText(result.difficulty) }} · {{ result.modelName }} · {{ formatInterviewTime(result.createTime) }}</p>
        </div>
        <div class="score-box" :style="{ color: scoreColor(result.totalScore) }">
          <strong>{{ result.totalScore }}</strong>
          <span>{{ result.level }}</span>
        </div>
      </div>

      <el-alert :title="result.summary" type="success" show-icon :closable="false" />

      <div class="summary-grid">
        <div class="summary-card">
          <h3>优点</h3>
          <ul>
            <li v-for="item in result.advantages" :key="item">{{ item }}</li>
          </ul>
        </div>
        <div class="summary-card">
          <h3>不足</h3>
          <ul>
            <li v-for="item in result.improvements" :key="item">{{ item }}</li>
          </ul>
        </div>
        <div class="summary-card">
          <h3>改进建议</h3>
          <ul>
            <li v-for="item in result.suggestions" :key="item">{{ item }}</li>
          </ul>
        </div>
      </div>

      <div class="question-results">
        <h2>各题评分</h2>
        <div v-for="item in result.questionResults" :key="item.questionNo" class="question-result">
          <div class="question-result__header">
            <h3>第 {{ item.questionNo }} 题：{{ item.question }}</h3>
            <el-tag :color="scoreColor(item.score || 0)" effect="dark">{{ item.score ?? 0 }} 分</el-tag>
          </div>
          <p>{{ item.review }}</p>
          <el-collapse>
            <el-collapse-item title="我的回答与建议" :name="String(item.questionNo)">
              <el-descriptions border :column="1">
                <el-descriptions-item label="我的回答">
                  <pre class="plain-text">{{ item.answer || '未作答' }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="AI 回答建议">
                  <pre class="plain-text">{{ item.suggestedAnswer }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="优点">{{ item.advantages.join('；') }}</el-descriptions-item>
                <el-descriptions-item label="不足">{{ item.improvements.join('；') }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <div class="result-actions">
        <el-button @click="router.push('/ai-interview')">重新开始</el-button>
        <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
      </div>
    </section>

    <el-empty v-else description="未找到面试结果">
      <el-button type="primary" @click="router.push('/ai-interview')">重新开始</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import type { InterviewResult } from '@/types/ai-interview'
import { difficultyText, formatInterviewTime, getInterviewResult, scoreColor } from '@/utils/ai-interview'

const route = useRoute()
const router = useRouter()
const result = ref<InterviewResult | null>(null)

onMounted(() => {
  result.value = getInterviewResult(route.params.id as string)
})
</script>

<style scoped>
.result-panel {
  padding: 22px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.result-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.result-header p {
  margin: 0;
  color: #62748e;
}

.score-box {
  min-width: 110px;
  text-align: right;
}

.score-box strong {
  display: block;
  font-size: 42px;
  line-height: 1;
}

.score-box span {
  display: block;
  margin-top: 6px;
  color: #62748e;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 18px 0;
}

.summary-card,
.question-result {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.summary-card h3,
.question-results h2,
.question-result h3 {
  margin: 0;
}

.summary-card ul {
  margin: 12px 0 0;
  padding-left: 18px;
  color: #486581;
  line-height: 1.7;
}

.question-results h2 {
  margin-bottom: 12px;
  font-size: 20px;
}

.question-result + .question-result {
  margin-top: 12px;
}

.question-result__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.question-result p {
  color: #486581;
  line-height: 1.7;
}

.plain-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

@media (max-width: 860px) {
  .result-header,
  .question-result__header,
  .result-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .score-box {
    text-align: left;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
