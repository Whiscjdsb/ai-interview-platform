export function answerResultText(isCorrect: boolean | null) {
  if (isCorrect === true) {
    return '正确'
  }
  if (isCorrect === false) {
    return '错误'
  }
  return '待 AI 点评或人工评估'
}

export function answerResultType(isCorrect: boolean | null) {
  if (isCorrect === true) {
    return 'success'
  }
  if (isCorrect === false) {
    return 'danger'
  }
  return 'info'
}

export function formatDuration(seconds?: number | null) {
  if (seconds === null || seconds === undefined) {
    return '-'
  }
  if (seconds < 60) {
    return `${seconds} 秒`
  }
  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60
  return rest ? `${minutes} 分 ${rest} 秒` : `${minutes} 分`
}

export function formatScore(score?: number | null) {
  return score === null || score === undefined ? '待评估' : score
}
