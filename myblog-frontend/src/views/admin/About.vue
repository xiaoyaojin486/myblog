<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminAbout, saveAbout } from '@/api/about'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const loading = ref(false)
const saving = ref(false)
const skills = ref([])
const experiences = ref([])
const form = reactive({ content: '', resumeUrl: '' })

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminAbout()
    form.content = data.content || ''
    form.resumeUrl = data.resumeUrl || ''
    skills.value = data.skills || []
    experiences.value = data.experiences || []
  } finally {
    loading.value = false
  }
}

function addSkill() {
  skills.value.push({ name: '', levelLabel: '', percent: 60 })
}

function removeSkill(index) {
  skills.value.splice(index, 1)
}

function addExperience() {
  experiences.value.push({ title: '', organization: '', startDate: '', endDate: '', description: '' })
}

function removeExperience(index) {
  experiences.value.splice(index, 1)
}

async function handleSave() {
  if (skills.value.some((s) => !s.name || !s.name.trim())) {
    ElMessage.warning('技能名称不能为空')
    return
  }
  if (experiences.value.some((e) => !e.title || !e.title.trim())) {
    ElMessage.warning('经历标题不能为空')
    return
  }
  saving.value = true
  try {
    await saveAbout({
      content: form.content,
      resumeUrl: form.resumeUrl,
      skills: skills.value,
      experiences: experiences.value
    })
    ElMessage.success('保存成功')
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading">
    <el-card class="head-card">
      <div class="head">
        <div>
          <div class="page-title">关于我</div>
          <div class="page-sub">维护个人介绍、技能栈与经历</div>
        </div>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>
            <span class="sec-title">自我介绍（Markdown）</span>
          </template>
          <MarkdownEditor v-model="form.content" />
          <div class="field-tip">简历文件地址（留空表示不提供下载）</div>
          <el-input v-model="form.resumeUrl" placeholder="/uploads/2026/09/resume.pdf" maxlength="255" />
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="sec-card">
          <template #header>
            <div class="sec-head">
              <span class="sec-title">技能栈</span>
              <el-button size="small" @click="addSkill">添加</el-button>
            </div>
          </template>
          <el-empty v-if="skills.length === 0" description="暂无技能，点击「添加」" :image-size="60" />
          <div v-for="(item, index) in skills" :key="index" class="skill-item">
            <el-input v-model="item.name" placeholder="技能名称，如 Java" maxlength="50" class="skill-name" />
            <el-input v-model="item.levelLabel" placeholder="分类" maxlength="20" class="skill-label" />
            <el-input-number
              v-model="item.percent"
              :min="0"
              :max="100"
              :step="5"
              size="small"
              controls-position="right"
              class="skill-percent"
            />
            <el-button link type="danger" @click="removeSkill(index)">删除</el-button>
          </div>
        </el-card>

        <el-card class="sec-card">
          <template #header>
            <div class="sec-head">
              <span class="sec-title">经历</span>
              <el-button size="small" @click="addExperience">添加</el-button>
            </div>
          </template>
          <el-empty v-if="experiences.length === 0" description="暂无经历，点击「添加」" :image-size="60" />
          <div v-for="(item, index) in experiences" :key="index" class="exp-item">
            <el-input v-model="item.title" placeholder="经历标题，如 学生会技术部负责人" maxlength="100" />
            <el-input v-model="item.organization" placeholder="组织/机构" maxlength="100" class="mt-8" />
            <div class="exp-dates">
              <el-input v-model="item.startDate" placeholder="2023-09" maxlength="20" />
              <span class="sep">~</span>
              <el-input v-model="item.endDate" placeholder="至今（可留空）" maxlength="20" />
            </div>
            <el-input
              v-model="item.description"
              type="textarea"
              :rows="3"
              maxlength="500"
              placeholder="经历描述"
              class="mt-8"
            />
            <div class="exp-actions">
              <el-button link type="danger" @click="removeExperience(index)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.head-card {
  margin-bottom: 20px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.sec-card {
  margin-bottom: 20px;
}
.sec-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sec-title {
  font-weight: 600;
  color: #303133;
}
.field-tip {
  margin: 16px 0 8px;
  font-size: 13px;
  color: #909399;
}
.skill-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}
.skill-item:last-child {
  border-bottom: none;
}
.skill-name {
  flex: 1;
}
.skill-label {
  width: 90px;
}
.skill-percent {
  width: 110px;
}
.exp-item {
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}
.exp-item:last-child {
  border-bottom: none;
}
.exp-dates {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.sep {
  color: #909399;
}
.mt-8 {
  margin-top: 8px;
}
.exp-actions {
  margin-top: 6px;
  text-align: right;
}
</style>