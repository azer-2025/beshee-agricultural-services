import { ElMessage, ElMessageBox } from 'element-plus'

export function showSuccessToast(message = '操作成功') {
  return ElMessage.success(message)
}

export function showFailToast(message = '操作失败') {
  return ElMessage.error(message)
}

export function showConfirmDialog(options = {}) {
  const title = options.title || '提示'
  const message = options.message || '确认执行该操作吗？'
  return ElMessageBox.confirm(message, title, {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
}
