<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, updateProfile, updatePassword } from '@/api/user'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)
const uploading = ref(false)
const profileFormRef = ref(null)
const pwdFormRef = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  signature: '',
  github: '',
  gitee: '',
  juejin: '',
  csdn: '',
  wechatQr: ''
})
const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度需在 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) =>
        value === pwdForm.newPassword ? callback() : callback(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  loading.value = true
  try {
    Object.assign(form, await getProfile())
  } finally {
    loading.value = false
  }
}

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  uploading.value = true
  try {
    const data = await uploadFile(options.file, 'avatar')
    form.avatar = data.url
    ElMessage.success('头像上传成功，点击「保存资料」生效')
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    uploading.value = false
  }
}

async function saveProfile() {
  try {
    await profileFormRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const data = await updateProfile({ ...form })
    Object.assign(form, data)
    userStore.setUser(data) // 右上角头像/昵称即时刷新
    ElMessage.success('保存成功')
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  try {
    await pwdFormRef.value.validate()
  } catch (e) {
    return
  }
  pwdSaving.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/admin/login')
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    pwdSaving.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <el-row :gutter="20">
    <el-col :span="14">
      <el-card v-loading="loading">
        <template #header>
          <span class="card-header">基础资料</span>
        </template>
        <el-form ref="profileFormRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="头像">
            <div class="avatar-row">
              <el-avatar :size="80" :src="form.avatar || ''">{{ (form.nickname || '博').slice(0, 1) }}</el-avatar>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button :loading="uploading">上传头像</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" maxlength="50" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" maxlength="100" />
          </el-form-item>
          <el-form-item label="个人签名" prop="signature">
            <el-input
              v-model="form.signature"
              type="textarea"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="记录学习 · 分享生活"
            />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="GitHub" prop="github">
                <el-input v-model="form.github" maxlength="255" placeholder="https://github.com/..." />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Gitee" prop="gitee">
                <el-input v-model="form.gitee" maxlength="255" placeholder="https://gitee.com/..." />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="掘金" prop="juejin">
                <el-input v-model="form.juejin" maxlength="255" placeholder="https://juejin.cn/user/..." />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="CSDN" prop="csdn">
                <el-input v-model="form.csdn" maxlength="255" placeholder="https://blog.csdn.net/..." />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="微信二维码图片" prop="wechatQr">
            <el-input v-model="form.wechatQr" maxlength="255" placeholder="图片URL（上传后可粘贴链接）" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="10">
      <el-card>
        <template #header>
          <span class="card-header">修改密码</span>
        </template>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <div class="pwd-tip">修改成功后当前登录会失效，需要重新登录。</div>
          <el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<style scoped>
.card-header {
  color: #303133;
  font-weight: 600;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.pwd-tip {
  margin-bottom: 18px;
  padding: 8px 12px;
  border-radius: 4px;
  background: #fdf6ec;
  color: #e6a23c;
  font-size: 13px;
  line-height: 1.6;
}
</style>