const ABSOLUTE_PATTERN = /^(https?:)?\/\//i

export function resolveMediaUrl(url) {
  if (!url) return ''
  if (ABSOLUTE_PATTERN.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }
  const prefix = import.meta.env.DEV ? '/dev-api' : ''
  if (url.startsWith('/')) {
    return `${prefix}${url}`
  }
  return `${prefix}/${url}`
}
