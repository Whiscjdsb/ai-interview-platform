import type { OptionItem, QuestionDifficulty, QuestionType } from '@/types/question'

export const QUESTION_TYPE_OPTIONS: Array<{ label: string; value: QuestionType }> = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'JUDGE' },
  { label: '简答题', value: 'SHORT_ANSWER' },
  { label: '编程题', value: 'CODING' }
]

export const DIFFICULTY_OPTIONS: Array<{ label: string; value: QuestionDifficulty }> = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

export const WRONG_STATUS_OPTIONS = [
  { label: '仍是错题', value: 'ACTIVE' },
  { label: '已攻克', value: 'RESOLVED' }
] as const

const typeText: Record<QuestionType, string> = {
  SINGLE_CHOICE: '单选题',
  MULTIPLE_CHOICE: '多选题',
  JUDGE: '判断题',
  SHORT_ANSWER: '简答题',
  CODING: '编程题'
}

const difficultyText: Record<QuestionDifficulty, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

const difficultyTagType: Record<QuestionDifficulty, 'success' | 'warning' | 'danger'> = {
  EASY: 'success',
  MEDIUM: 'warning',
  HARD: 'danger'
}

export function questionTypeText(type: QuestionType) {
  return typeText[type] || type
}

export function difficultyLabel(difficulty: QuestionDifficulty) {
  return difficultyText[difficulty] || difficulty
}

export function difficultyType(difficulty: QuestionDifficulty) {
  return difficultyTagType[difficulty] || 'info'
}

export function formatDateTime(value?: string) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ').slice(0, 19)
}

export function wrongStatusText(status: 'ACTIVE' | 'RESOLVED') {
  return status === 'RESOLVED' ? '已攻克' : '仍是错题'
}

export function wrongStatusType(status: 'ACTIVE' | 'RESOLVED') {
  return status === 'RESOLVED' ? 'success' : 'danger'
}

export function parseChoiceOptions(content: string): OptionItem[] {
  const pattern = /(?:^|\s)([A-Z])[\.\、．]\s*/g
  const markers: Array<{ label: string; start: number; end: number }> = []
  let marker: RegExpExecArray | null
  while ((marker = pattern.exec(content)) !== null) {
    markers.push({
      label: marker[1],
      start: marker.index,
      end: pattern.lastIndex
    })
  }

  const options: OptionItem[] = []
  markers.forEach((item, index) => {
    const next = markers[index + 1]
    options.push({
      label: item.label,
      text: content.slice(item.end, next ? next.start : content.length).trim()
    })
  })
  return options
}

export function removeChoiceOptions(content: string) {
  const firstOptionIndex = content.search(/(?:^|\s)[A-Z][\.\、．]\s*/)
  if (firstOptionIndex < 0) {
    return content
  }
  return content.slice(0, firstOptionIndex).trim()
}
