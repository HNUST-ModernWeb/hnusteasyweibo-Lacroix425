<template>
  <div class="register">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <input v-model="nickname" placeholder="昵称" />
      <button type="submit">注册</button>
    </form>
    <p>已有账号？<router-link to="/login">登录</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const nickname = ref('')

const register = async () => {
  try {
    const res = await axios.post('/api/auth/register', { 
      username: username.value, 
      password: password.value,
      nickname: nickname.value 
    })
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data.user))
      router.push('/')
    } else {
      alert(res.data.message)
    }
  } catch (e) {
    alert('注册失败')
  }
}
</script>

<style scoped>
.register { max-width: 300px; margin: 50px auto; text-align: center; }
input { display: block; width: 100%; padding: 10px; margin: 10px 0; }
button { padding: 10px 20px; background: #42b983; color: white; border: none; cursor: pointer; }
</style>