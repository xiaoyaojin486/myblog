<script setup>
import { ref, computed, onMounted } from "vue";
import { useConfigStore } from "@/store/config";
import { getPublicProfile } from "@/api/user";

const configStore = useConfigStore();
const profile = ref(null);
const year = new Date().getFullYear();
const ICP_QUERY_URL = "https://beian.miit.gov.cn/";
const POLICE_QUERY_URL = "https://beian.mps.gov.cn/#/query/webSearch";
const POLICE_ICON = "/beian.png"; // 官方图标放 public/ 下，构建时原样拷贝到根目录

// 公安备案跳转只认纯数字 code，正文中的「皖公网安备 / 号 / 空格」不能带入 URL
const policeCode = computed(() =>
  (configStore.config.police_icp || "").replace(/\D/g, ""),
);
const policeHref = computed(
  () => `${POLICE_QUERY_URL}?code=${policeCode.value}`,
);

onMounted(async () => {
  try {
    profile.value = await getPublicProfile(); // 联系方式取「个人资料」
  } catch (e) {
    // 错误提示已由拦截器处理
  }
});
</script>

<template>
  <footer class="the-footer">
    <div class="inner">
      <div class="col brand">
        <div class="site">{{ configStore.siteName }}</div>
        <p class="slogan">{{ configStore.siteDescription }}</p>
        <p class="copyright">
          © {{ year }} {{ configStore.siteName }} · Powered by Vue3 + Spring
          Boot
        </p>
      </div>
      <div class="col">
        <div class="col-title">快捷导航</div>
        <router-link to="/" class="link">首页</router-link>
        <router-link to="/projects" class="link">项目</router-link>
        <router-link to="/archive" class="link">归档</router-link>
        <router-link to="/about" class="link">关于</router-link>
      </div>
      <div class="col">
        <div class="col-title">联系我</div>
        <a
          v-if="profile?.github"
          :href="profile.github"
          target="_blank"
          rel="noopener"
          class="link"
          >GitHub</a
        >
        <a v-if="profile?.email" :href="`mailto:${profile.email}`" class="link"
          >邮箱</a
        >
        <span v-if="!profile?.github && !profile?.email" class="link"
          >联系方式待配置</span
        >
      </div>
    </div>
    <div
      v-if="configStore.config.icp || configStore.config.police_icp"
      class="bottom"
    >
      <a
        v-if="configStore.config.icp"
        :href="ICP_QUERY_URL"
        target="_blank"
        rel="noopener noreferrer"
        class="beian-link"
      >
        {{ configStore.config.icp }}
      </a>
      <a
        v-if="configStore.config.police_icp"
        :href="policeHref"
        target="_blank"
        rel="noopener noreferrer"
        class="beian-link"
      >
        <img :src="POLICE_ICON" alt="公安备案图标" class="beian-icon" />
        <span>{{ configStore.config.police_icp }}</span>
      </a>
    </div>
  </footer>
</template>

<style scoped>
.the-footer {
  position: relative;
  z-index: 1;
  background: var(--blog-card-bg);
  border-top: 1px solid var(--blog-border);
  backdrop-filter: blur(var(--blog-glass-blur));
  -webkit-backdrop-filter: blur(var(--blog-glass-blur));
  color: var(--blog-text-light);
  font-size: 13px;
  transition: background-color 0.3s;
}
.inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 16px 16px;
  display: flex;
  gap: 64px;
}
.brand {
  flex: 1;
}
.site {
  font-size: 16px;
  font-weight: 700;
  color: var(--blog-text);
}
.slogan {
  margin: 8px 0;
}
.col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.col-title {
  color: var(--blog-text);
  font-weight: 600;
  margin-bottom: 4px;
}
.link {
  color: var(--blog-text-light);
  text-decoration: none;
  transition: color 0.2s;
}
.link:hover {
  color: var(--blog-primary);
}
.bottom {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-top: 1px solid var(--blog-divider);
}
.beian-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: inherit;
  text-decoration: none;
  transition: color 0.2s;
}
.beian-link:hover {
  color: var(--blog-primary);
}
.beian-icon {
  width: 16px;
  height: 16px;
  display: block;
}
</style>
