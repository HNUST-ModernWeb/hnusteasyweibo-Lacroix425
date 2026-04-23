<template>
  <div class="post-detail">
    <button @click="$router.push('/')" class="back-btn">← 返回</button>
    <div class="post-content">{{ post?.content }}</div>
    <div v-if="post?.images" class="post-imgs">
      <img v-for="(img, i) in post.images.split(',')" :key="i" :src="img" />
    </div>
    <div class="actions">
      <span @click="toggleLike" :class="{active: liked}">👍 {{ likeCount }}</span>
      <span>💬 {{ commentCount }}</span>
    </div>
    <div class="comments">
      <h3>评论 {{ commentCount }}</h3>
      <div v-for="c in comments" :key="c.id" class="comment">
        <span class="comment-user">{{ c.nickname || '用户' + c.userId }}</span>
        <span class="comment-content">{{ c.content }}</span>
      </div>
      <div v-if="comments.length === 0" class="no-comment">暂无评论，快来抢沙发</div>
    </div>
    <div class="comment-form">
      <div class="emoji-bar">
        <span @click="showEmoji = !showEmoji" class="emoji-btn">😊</span>
      </div>
      <div v-if="showEmoji" class="emoji-panel">
        <span v-for="e in emojis" :key="e" @click="addEmoji(e)" class="emoji">{{ e }}</span>
      </div>
      <div class="input-row">
        <input v-model="newComment" placeholder="说点什么..." />
        <button @click="addComment" :disabled="!newComment.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const post = ref(null)
const comments = ref([])
const liked = ref(false)
const likeCount = ref(0)
const commentCount = ref(0)
const newComment = ref('')
const showEmoji = ref(false)
const emojis = ['😀','😃','😂','🤣','😊','😍','🥰','😘','🤔','😅','😂','😭','💪','👋','👍','❤️','🔥','🎉','✨','🌟']

const addEmoji = (e) => { newComment.value += e; showEmoji.value = false }

const loadPost = async () => {
  const res = await axios.get('/api/posts/' + route.params.id)
  post.value = res.data.data.post
  likeCount.value = post.value.likeCount
  commentCount.value = post.value.commentCount
}

const loadComments = async () => {
  const res = await axios.get('/api/comments/post/' + route.params.id)
  comments.value = res.data.data
}

const toggleLike = async () => {
  await axios.post('/api/likes', { postId: route.params.id }, {
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
  })
  liked.value = !liked.value
  loadPost()
}

const addComment = async () => {
  if (!newComment.value.trim()) return
  await axios.post('/api/comments', { postId: route.params.id, content: newComment.value }, {
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
  })
  newComment.value = ''
  showEmoji.value = false
  loadComments()
  loadPost()
}

onMounted(() => { loadPost(); loadComments() })
</script>

<style scoped>
.post-detail { padding: 16px; max-width: 600px; margin: 0 auto; }
.back-btn { background: none; border: none; font-size: 16px; cursor: pointer; margin-bottom: 16px; }
.post-content { font-size: 18px; line-height: 1.6; margin-bottom: 16px; }
.post-imgs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.post-imgs img { max-width: 150px; border-radius: 8px; }
.actions { display: flex; gap: 24px; padding: 12px 0; border-bottom: 1px solid #eee; }
.actions span { cursor: pointer; font-size: 14px; }
.actions .active { color: #ff8200; }
.comments { margin-top: 16px; }
.comments h3 { font-size: 16px; margin-bottom: 12px; }
.comment { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.comment-user { font-weight: bold; margin-right: 8px; }
.no-comment { color: #999; text-align: center; padding: 20px; }
.comment-form { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1px solid #eee; padding: 12px; }
.emoji-bar { margin-bottom: 8px; }
.emoji-btn { font-size: 20px; cursor: pointer; }
.emoji-panel { display: flex; flex-wrap: wrap; gap: 5px; padding: 10px; background: #f8f8f8; border-radius: 8px; margin-bottom: 8px; }
.emoji { font-size: 20px; cursor: pointer; }
.input-row { display: flex; gap: 10px; }
.input-row input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 20px; }
.input-row button { padding: 8px 16px; background: #ff8200; color: #fff; border: none; border-radius: 20px; }
.input-row button:disabled { background: #ccc; }
</style>