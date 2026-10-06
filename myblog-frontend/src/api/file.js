import request from './request'

/**
 * 上传图片到 OSS
 * @param {File} file 图片文件
 * @param {string} bizType avatar / article / project / moment / other
 */
export function uploadFile(file, bizType = 'article') {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/admin/file/upload?bizType=${bizType}`, formData)
}

export function removeFile(objectKey) {
  return request.delete('/admin/file', { params: { objectKey } })
}

/** 分页查询 OSS 文件（bizType / keyword / pageNum / pageSize） */
export function pageAdminFiles(params) {
  return request.get('/admin/file/page', { params })
}

/** 重命名 / 移动文件 */
export function renameFile(data) {
  return request.put('/admin/file/rename', data)
}

/** 批量删除文件 */
export function removeFiles(objectKeys) {
  return request.delete('/admin/file/batch', { data: objectKeys })
}