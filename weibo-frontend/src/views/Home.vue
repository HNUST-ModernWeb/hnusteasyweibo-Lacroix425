<template>
  <div class="home">
    <nav class="navbar">
      <h1>Weibo</h1>
      <div class="nav-right">
        <span v-if="isLoggedIn" @click="showNotifications = !showNotifications" class="notif-bell">🔔<span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span></span>
        <button v-if="!isLoggedIn" @click="$router.push('/login')">登录</button>
        <span v-else class="user-info">{{ currentUser }}</span>
        <button v-if="isLoggedIn" @click="logout" class="logout-btn">退出</button>
      </div>
      <div v-if="showNotifications" class="notif-panel">
        <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{unread: !n.isRead}" @click="goPost(n)">
          {{ n.content }}
        </div>
        <div v-if="notifications.length === 0" class="no-notif">暂无通知</div>
      </div>
    </nav>
    <div v-if="isLoggedIn" class="post-form">
      <textarea v-model="newPost" placeholder="今天有什么新鲜事？#话题"></textarea>
      <div class="tool-bar">
        <span @click="showEmoji = !showEmoji" class="icon-btn">😊</span>
        <label class="icon-btn">📷<input type="file" @change="onFileChange" accept="image/*" hidden /></label>
        <span class="char-count">{{ newPost.length }}/140</span>
      </div>
      <div v-if="showEmoji" class="emoji-panel">
        <span v-for="e in emojis" :key="e" @click="addEmoji(e)" class="emoji">{{ e }}</span>
      </div>
      <div v-if="preview" class="preview-box">
        <img :src="preview" @click="clearImage" />
      </div>
      <button @click="submitPost" :disabled="!newPost.trim()" class="submit-btn">发布</button>
    </div>
    <div class="tabs">
      <button @click="loadPosts()" :class="{active: tab==='latest'}">最新</button>
      <button @click="loadTrending()" :class="{active: tab==='trending'}">热门</button>
    </div>
    <div class="posts">
      <div v-for="post in posts" :key="post.id" class="post">
        <div class="post-user" @click="$router.push('/post/' + post.id)">
          <div class="post-header">
            <span class="nickname">{{ post.nickname || '用户' + post.userId }}</span>
            <span class="time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <div class="post-body" :id="'post-' + post.id">{{ post.content }}</div>
          <div v-if="post.images" class="post-imgs" @click.stop>
            <img v-for="(img, i) in post.images.split(',')" :key="i" :src="img" @click="$router.push('/post/' + post.id)" />
          </div>
          <div class="post-meta">
            <span @click.stop="likePost(post.id)">👍 {{ post.likeCount }}</span>
            <span @click.stop="$router.push('/post/' + post.id)">💬 {{ post.commentCount }}</span>
            <span v-if="Number(userId) === Number(post.userId)" @click.stop="deletePost(post.id)" class="delete-btn">🗑️</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const posts = ref([])
const newPost = ref('')
const isLoggedIn = ref(!!localStorage.getItem('token'))
const imageFile = ref(null)
const preview = ref('')
const showEmoji = ref(false)
const tab = ref('latest')
const currentUser = ref('')
const userId = ref(0)
const emojis = ['😀','😃','😂','🤣','😊','😍','🥰','😘','🤔','😅','😂','😭','💪','👋','👍','❤️','🔥','🎉','✨','🌟']
const notifications = ref([])
const unreadCount = ref(0)
const showNotifications = ref(false)

const loadNotifications = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const res = await axios.get('/api/notifications', { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } })
    notifications.value = res.data.data || []
    const res2 = await axios.get('/api/notifications/unread', { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } })
    unreadCount.value = res2.data.data || 0
  } catch (e) {
    notifications.value = []
    unreadCount.value = 0
  }
}

const goPost = async (n) => {
  showNotifications.value = false
  if (!n.isRead && localStorage.getItem('token')) {
    try {
      await axios.post('/api/notifications/read', {}, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } })
    } catch (e) {}
  }
  loadNotifications()
  if (n.postId) router.push('/post/' + n.postId)
}

const deletePost = async (id) => {
  if (!confirm('确定删除？')) return
  try {
    const token = localStorage.getItem('token')
    console.log('Delete token:', token)
    const res = await axios.delete('/api/posts/' + id, { headers: { Authorization: `Bearer ${token}` } })
    console.log('Delete result:', res.data)
    loadPosts()
  } catch (e) {
    console.error('Delete error:', e)
    const msg = e.response?.data?.message || e.response?.data || e.message
    alert('删除失败: ' + msg)
  }
}

const addEmoji = (e) => { newPost.value += e; showEmoji.value = false }
const clearImage = () => { imageFile.value = null; preview.value = '' }
const formatTime = (t) => t ? t.substring(5, 16) : ''
const onFileChange = (e) => { if (e.target.files[0]) { imageFile.value = e.target.files[0]; preview.value = URL.createObjectURL(e.target.files[0]) } }

const loadPosts = async () => {
  tab.value = 'latest'
  const res = await axios.get('/api/posts')
  posts.value = res.data.data.records
  nextTick(() => bindTagClicks())
}

const loadTrending = async () => {
  tab.value = 'trending'
  const res = await axios.get('/api/posts/trending')
  posts.value = res.data.data
  nextTick(() => bindTagClicks())
}

const bindTagClicks = () => {
  document.querySelectorAll('.post-body').forEach(el => {
    el.innerHTML = el.innerHTML.replace(/#[\u4e00-\u9fa5a-zA-Z]+/g, '<span class="tag" data-tag="$&">$&</span>')
  })
  document.querySelectorAll('.tag').forEach(el => {
    el.onclick = (e) => { e.stopPropagation(); router.push('/tag/' + el.innerText.replace('#','')) }
  })
}

const submitPost = async () => {
  if (!newPost.value.trim()) return
  const form = new FormData()
  form.append('content', newPost.value)
  if (imageFile.value) form.append('images', imageFile.value)
  await axios.post('/api/posts', form, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}`, 'Content-Type': 'multipart/form-data' } })
  newPost.value = ''
  imageFile.value = null
  preview.value = ''
  loadPosts()
}

const logout = () => { localStorage.removeItem('token'); localStorage.removeItem('user'); isLoggedIn.value = false; router.push('/login') }

onMounted(() => {
  loadPosts()
  loadNotifications()
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  currentUser.value = user.nickname || user.username || ''
  userId.value = user.userId
})
</script>

<style scoped>
.navbar { display: flex; justify-content: space-between; padding: 12px 16px; background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.navbar h1 { margin: 0; color: #ff8200; font-size: 20px; }
.post-form { padding: 16px; background: #f8f8f8; border-bottom: 1px solid #eee; }
.post-form textarea { width: 100%; height: 80px; padding: 10px; border: 1px solid #ddd; border-radius: 8px; resize: none; font-size: 14px; }
.tool-bar { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.icon-btn { font-size: 20px; cursor: pointer; }
.char-count { color: #999; font-size: 12px; margin-left: auto; }
.emoji-panel { display: flex; flex-wrap: wrap; gap: 5px; padding: 10px; background: #fff; border: 1px solid #ddd; border-radius: 8px; margin-top: 8px; }
.emoji { font-size: 20px; cursor: pointer; }
.preview-box img { max-width: 150px; border-radius: 8px; margin-top: 10px; }
.submit-btn { width: 100%; padding: 10px; margin-top: 10px; background: #ff8200; color: #fff; border: none; border-radius: 20px; cursor: pointer; }
.submit-btn:disabled { background: #ccc; }
.tabs { display: flex; border-bottom: 1px solid #eee; }
.tabs button { flex: 1; padding: 12px; background: none; border: none; color: #666; }
.tabs button.active { color: #ff8200; border-bottom: 2px solid #ff8200; }
.post { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.post-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.nickname { font-weight: bold; }
.time { color: #999; font-size: 12px; }
.post-body { font-size: 15px; line-height: 1.5; margin-bottom: 8px; white-space: pre-wrap; }
:deep(.tag) { color: #ff8200; cursor: pointer; }
.post-imgs { display: flex; gap: 5px; flex-wrap: wrap; margin-bottom: 8px; }
.post-imgs img { width: 90px; height: 90px; object-fit: cover; border-radius: 8px; }
.post-meta { display: flex; gap: 20px; color: #999; }
.delete-btn { cursor: pointer; }
.notif-bell { position: relative; cursor: pointer; font-size: 18px; margin-right: 12px; }
.badge { position: absolute; top: -5px; right: -5px; background: red; color: #fff; font-size: 10px; padding: 2px 5px; border-radius: 10px; }
.notif-panel { position: absolute; top: 50px; right: 10px; width: 250px; max-height: 300px; overflow-y: auto; background: #fff; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); z-index: 200; }
.notif-item { padding: 12px; border-bottom: 1px solid #eee; cursor: pointer; }
.notif-item:hover { background: #f8f8f8; }
.notif-item.unread { background: #fff8e8; }
.no-notif { padding: 20px; text-align: center; color: #999; }
</style>