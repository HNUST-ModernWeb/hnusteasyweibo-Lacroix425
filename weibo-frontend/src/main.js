import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import App from './App.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('./views/Home.vue') },
    { path: '/login', component: () => import('./views/Login.vue') },
    { path: '/register', component: () => import('./views/Register.vue') },
    { path: '/post/:id', component: () => import('./views/PostDetail.vue') },
    { path: '/tag/:tag', component: () => import('./views/Tag.vue') }
  ]
})

axios.defaults.baseURL = 'http://localhost:8080'
const app = createApp(App)
app.use(router)
app.mount('#app')