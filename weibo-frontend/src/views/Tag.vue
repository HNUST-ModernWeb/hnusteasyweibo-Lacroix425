<template>
  <div class="tag-page">
    <button @click="$router.push('/')">返回</button>
    <h2>#{{ tag }} 话题</h2>
    <div v-for="post in posts" :key="post.id" class="post" @click="$router.push('/post/' + post.id)">
      <div class="content">{{ post.content }}</div>
      <div class="meta">
        <span>👍 {{ post.likeCount }}</span>
        <span>💬 {{ post.commentCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const posts = ref([])
const tag = route.params.tag

const loadPosts = async () => {
  const res = await axios.get('/api/posts/tag/' + tag)
  posts.value = res.data.data
}

onMounted(loadPosts)
</script>

<style scoped>
.tag-page { padding: 15px; }
.post { padding: 15px; border-bottom: 1px solid #eee; cursor: pointer; }
.meta { color: #888; font-size: 12px; }
</style>