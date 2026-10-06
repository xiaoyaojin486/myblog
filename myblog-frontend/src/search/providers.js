import { searchArticles } from '@/api/article'
import { listProjects } from '@/api/project'

/**
 * 全站搜索提供者注册表
 *
 * 每个可搜索模块实现一个 provider：
 *   {
 *     key:     唯一标识（也用作页签 key）
 *     label:   展示名（页签 / 分组标题）
 *     search(keyword, { pageNum, pageSize }) => Promise<{ records, total }>
 *     normalize(item) => SearchItem
 *   }
 *
 * SearchItem 统一结构（前端各模块字段不同，统一在这里归一化）：
 *   { type, id, title, summary, cover, tag, meta: string[], to }
 *
 * 新增模块（如「游戏」）：实现一个 provider 并加入 searchProviders 即可，
 * 搜索页与搜索框无需改动。
 */
export const searchProviders = [
  {
    key: 'article',
    label: '文章',
    async search(keyword, { pageNum, pageSize }) {
      const data = await searchArticles({ keyword, pageNum, pageSize })
      return { records: data.records || [], total: data.total || 0 }
    },
    normalize(item) {
      return {
        type: 'article',
        id: item.id,
        title: item.title,
        summary: item.summary,
        cover: item.coverImage,
        tag: item.categoryName || '未分类',
        meta: [
          (item.createTime || '').slice(5, 16),
          `${item.wordCount || 0} 字`,
          `${item.viewCount || 0} 阅读`
        ],
        to: `/article/${item.id}`
      }
    }
  },
  {
    key: 'project',
    label: '项目',
    // 项目数量少：一次性取全量后本地过滤。日后项目变多可换成服务端搜索，仅需改这里。
    async search(keyword, { pageNum, pageSize }) {
      const all = await listProjects()
      const kw = keyword.trim().toLowerCase()
      const matched = (all || []).filter((p) =>
        `${p.name || ''} ${p.description || ''} ${p.techStack || ''}`.toLowerCase().includes(kw)
      )
      const start = (pageNum - 1) * pageSize
      return { records: matched.slice(start, start + pageSize), total: matched.length }
    },
    normalize(item) {
      const tech = (item.techStack || '')
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean)
        .slice(0, 3)
        .join(' · ')
      return {
        type: 'project',
        id: item.id,
        title: item.name,
        summary: item.description,
        cover: item.coverImage,
        tag: '项目',
        meta: tech ? [tech] : [],
        to: `/project/${item.id}`
      }
    }
  }
]

/** 按 key 取 provider（供页签切换使用） */
export function getProvider(key) {
  return searchProviders.find((p) => p.key === key)
}