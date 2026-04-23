<template>
  <div class="login">
    <h2>登录</h2>
    <form @submit.prevent="login">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">登录</button>
    </form>
    <p>还没有账号？<router-link to="/register">注册</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')

const login = async () => {
  try {
    const res = await axios.post('/api/auth/login', { username: username.value, password: password.value })
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data))
      router.push('/')
    } else {
      alert(res.data.message)
    }
  } catch (e) {
    alert('登录失败')
  }
}
</script>

<style scoped>
.login { max-width: 300px; margin: 50px auto; text-align: center; }
input { display: block; width: 100%; padding: 10px; margin: 10px 0; }
button { padding: 10px 20px; background: #42b983; color: white; border: none; cursor: pointer; }
</style>