<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { pageAdminArticles } from '@/api/article'
import { listCategories } from '@/api/category'
import { listTags } from '@/api/tag'

echarts.use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const stats = ref({ total: 0, published: 0, draft: 0, views: 0, categories: 0, tags: 0 })
const barRef = ref(null)
const pieRef = ref(null)
let barChart = null
let pieChart = null

const statItems = computed(() => [
  { label: '文章总数', value: stats.value.total },
  { label: '已发布', value: stats.value.published },
  { label: '草稿', value: stats.value.draft },
  { label: '总浏览量', value: stats.value.views },
  { label: '分类数', value: stats.value.categories },
  { label: '标签数', value: stats.value.tags }
])

function renderCharts(articles) {
  // 浏览量 TOP10（柱状图）
  const top = [...articles].sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0)).slice(0, 10)
  barChart = echarts.init(barRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, top: 30, bottom: 70 },
    xAxis: {
      type: 'category',
      data: top.map((a) => (a.title.length > 10 ? a.title.slice(0, 10) + '…' : a.title)),
      axisLabel: { rotate: 30, fontSize: 11 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: top.map((a) => a.viewCount || 0),
        itemStyle: { color: '#409eff' },
        barMaxWidth: 28
      }
    ]
  })

  // 分类文章分布（饼图）
  const countMap = {}
  articles.forEach((a) => {
    const key = a.categoryName || '未分类'
    countMap[key] = (countMap[key] || 0) + 1
  })
  pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        data: Object.entries(countMap).map(([name, value]) => ({ name, value })),
        label: { formatter: '{b}: {c}' }
      }
    ]
  })
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  try {
    const [page, cats, tags] = await Promise.all([
      pageAdminArticles({ pageNum: 1, pageSize: 100 }),
      listCategories(),
      listTags()
    ])
    const articles = page.records || []
    stats.value = {
      total: page.total || 0,
      published: articles.filter((a) => a.status === 1).length,
      draft: articles.filter((a) => a.status === 0).length,
      views: articles.reduce((sum, a) => sum + (a.viewCount || 0), 0),
      categories: cats.length,
      tags: tags.length
    }
    renderCharts(articles)
    window.addEventListener('resize', handleResize)
  } catch (e) {
    // 错误提示已由拦截器处理
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col v-for="item in statItems" :key="item.label" :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts">
      <el-col :span="14">
        <el-card>
          <template #header>文章浏览量 TOP10</template>
          <div ref="barRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>分类文章分布</template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}
.stat-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}
.charts {
  margin-top: 16px;
}
.chart {
  height: 320px;
}
</style>