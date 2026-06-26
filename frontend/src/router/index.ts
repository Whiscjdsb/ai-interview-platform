import { createRouter, createWebHistory } from 'vue-router'

import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/share/:token',
      name: 'share-interview',
      component: () => import('@/views/share/ShareInterviewView.vue')
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue')
        },
        {
          path: 'questions',
          name: 'questions',
          component: () => import('@/views/question/QuestionListView.vue'),
          meta: { title: '题库' }
        },
        {
          path: 'questions/:id',
          name: 'question-detail',
          component: () => import('@/views/question/QuestionDetailView.vue'),
          meta: { title: '题目详情' }
        },
        {
          path: 'practice/:id',
          name: 'practice-question',
          component: () => import('@/views/question/PracticeQuestionView.vue'),
          meta: { title: '刷题练习' }
        },
        {
          path: 'favorites',
          name: 'favorites',
          component: () => import('@/views/favorite/FavoriteListView.vue'),
          meta: { title: '我的收藏' }
        },
        {
          path: 'wrong-questions',
          name: 'wrong-questions',
          component: () => import('@/views/wrong-question/WrongQuestionListView.vue'),
          meta: { title: '错题本' }
        },
        {
          path: 'answer-history',
          name: 'answer-history',
          component: () => import('@/views/answer/AnswerHistoryView.vue'),
          meta: { title: '答题历史' }
        },
        {
          path: 'answer-history/:id',
          name: 'answer-history-detail',
          component: () => import('@/views/answer/AnswerHistoryDetailView.vue'),
          meta: { title: '答题记录详情' }
        },
        {
          path: 'ai-interview',
          name: 'ai-interview',
          component: () => import('@/views/ai-interview/AIInterviewView.vue'),
          meta: { title: 'AI 面试助手' }
        },
        {
          path: 'enterprise-interview',
          name: 'enterprise-interview',
          component: () => import('@/views/ai-interview/EnterpriseInterviewView.vue'),
          meta: { title: '企业级模拟面试' }
        },
        {
          path: 'ai-interview/session/:id',
          name: 'ai-interview-session',
          component: () => import('@/views/ai-interview/AIInterviewSessionView.vue'),
          meta: { title: 'AI 模拟面试' }
        },
        {
          path: 'ai-interview/result/:id',
          name: 'ai-interview-result',
          component: () => import('@/views/ai-interview/AIInterviewResultView.vue'),
          meta: { title: 'AI 面试结果' }
        },
        {
          path: 'ai-interview/history',
          name: 'ai-interview-history',
          component: () => import('@/views/ai-interview/AIInterviewHistoryView.vue'),
          meta: { title: 'AI 面试历史' }
        },
        {
          path: 'ai-interview/history/:id',
          name: 'ai-interview-history-detail',
          component: () => import('@/views/ai-interview/AIInterviewDetailView.vue'),
          meta: { title: 'AI 面试详情' }
        },
        {
          path: 'user/growth',
          name: 'user-growth',
          component: () => import('@/views/user/UserGrowthView.vue'),
          meta: { title: '能力成长' }
        },
        {
          path: 'profile',
          name: 'profile-coming',
          component: () => import('@/views/dashboard/ComingSoonView.vue'),
          meta: { title: '个人中心（开发中）' }
        },
        {
          path: 'admin',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/AdminDashboardView.vue'),
          meta: { title: '管理后台', adminOnly: true }
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: () => import('@/views/admin/AdminUserListView.vue'),
          meta: { title: '用户管理', adminOnly: true }
        },
        {
          path: 'admin/questions',
          name: 'admin-questions',
          component: () => import('@/views/admin/AdminQuestionListView.vue'),
          meta: { title: '题库管理', adminOnly: true }
        },
        {
          path: 'admin/questions/create',
          name: 'admin-question-create',
          component: () => import('@/views/admin/AdminQuestionFormView.vue'),
          meta: { title: '新增题目', adminOnly: true }
        },
        {
          path: 'admin/questions/:id/edit',
          name: 'admin-question-edit',
          component: () => import('@/views/admin/AdminQuestionFormView.vue'),
          meta: { title: '编辑题目', adminOnly: true }
        },
        {
          path: 'admin/favorites',
          name: 'admin-favorites',
          component: () => import('@/views/admin/AdminFavoriteListView.vue'),
          meta: { title: '收藏记录', adminOnly: true }
        },
        {
          path: 'admin/wrong-questions',
          name: 'admin-wrong-questions',
          component: () => import('@/views/admin/AdminWrongQuestionListView.vue'),
          meta: { title: '错题记录', adminOnly: true }
        },
        {
          path: 'admin/answers',
          name: 'admin-answers',
          component: () => import('@/views/admin/AdminAnswerListView.vue'),
          meta: { title: '答题记录', adminOnly: true }
        },
        {
          path: 'admin/ai-interviews',
          name: 'admin-ai-interviews',
          component: () => import('@/views/admin/AdminAIInterviewListView.vue'),
          meta: { title: 'AI 面试记录', adminOnly: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/dashboard/NotFoundView.vue')
    }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const guestOnly = Boolean(to.meta.guestOnly)

  if (requiresAuth && !userStore.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (requiresAuth) {
    try {
      await userStore.ensureCurrentUser()
    } catch {
      userStore.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  if (to.matched.some((record) => record.meta.adminOnly) && !userStore.isAdmin) {
    return '/dashboard'
  }

  if (guestOnly && userStore.isAuthenticated) {
    return '/dashboard'
  }

  return true
})

export default router
