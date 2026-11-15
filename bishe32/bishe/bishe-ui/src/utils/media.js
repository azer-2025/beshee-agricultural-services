export function resolveMediaUrl(path) {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }
  if (path.startsWith('/')) {
    return import.meta.env.VITE_APP_BASE_API + path
  }
  return import.meta.env.VITE_APP_BASE_API + '/' + path
}
