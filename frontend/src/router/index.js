import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import PublishView from '../views/PublishView.vue'
import DetailView from '../views/DetailView.vue'
import ChatView from '../views/ChatView.vue'
import ChatListView from '../views/ChatListView.vue'
import ProfileView from '../views/ProfileView.vue'
import AdminView from '../views/AdminView.vue'
import StatisticsView from '../views/StatisticsView.vue'
import EditProductView from '../views/EditProductView.vue'
import RecommendView from '../views/RecommendView.vue'
import OrdersView from '../views/OrdersView.vue'

// 路由守卫：检查登录状态
const requireAuth = (to, from, next) => {
  const user = localStorage.getItem('user')
  if (!user) {
    next('/login')
  } else {
    next()
  }
}

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/',
    name: 'home',
    component: HomeView,
    beforeEnter: requireAuth
  },
  {
    path: '/publish',
    name: 'publish',
    component: PublishView,
    beforeEnter: requireAuth
  },
  {
    path: '/product/:id',
    name: 'detail',
    component: DetailView,
    beforeEnter: requireAuth
  },
  {
    path: '/chat/:id',
    name: 'chat',
    component: ChatView,
    beforeEnter: requireAuth
  },
  {
    path: '/chatlist',
    name: 'chatlist',
    component: ChatListView,
    beforeEnter: requireAuth
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    beforeEnter: requireAuth
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminView,
    beforeEnter: requireAuth
  },
  {
    path: '/statistics',
    name: 'statistics',
    component: StatisticsView,
    beforeEnter: requireAuth
  },
  {
    path: '/product/edit/:id',
    name: 'editProduct',
    component: EditProductView,
    beforeEnter: requireAuth
  },
  {
    path: '/recommend',
    name: 'recommend',
    component: RecommendView,
    beforeEnter: requireAuth
  },
  {
    path: '/orders',
    name: 'orders',
    component: OrdersView,
    beforeEnter: requireAuth
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router