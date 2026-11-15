import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')
const MODULE_404 = './../../views/error/404.vue'

const normalizeViewPath = (val) => String(val || '')
  .trim()
  .replace(/[?#].*$/, '')
  .replace(/\\/g, '/')
  .replace(/^\/+/, '')
  .replace(/^\.?\/+/, '')
  .replace(/^src\/views\//, '')
  .replace(/^views\//, '')
  .replace(/\.vue$/i, '')
  .replace(/\/+$/, '')

const moduleLoaderMap = (() => {
  const map = new Map()
  for (const path in modules) {
    const relative = path.split('views/')[1]
    if (!relative) continue
    const key = normalizeViewPath(relative)
    const loader = () => modules[path]()
    map.set(key, loader)
    if (key.endsWith('/index')) {
      const noIndexKey = key.slice(0, -'/index'.length)
      if (!map.has(noIndexKey)) {
        map.set(noIndexKey, loader)
      }
    }
  }
  return map
})()

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [],
      addRoutes: [],
      defaultRoutes: [],
      topbarRouters: [],
      sidebarRouters: []
    }),
    actions: {
      setRoutes(routes) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles) {
        return new Promise(resolve => {
          // 向后端请求路由数据
          getRouters().then(res => {
            const sdata = JSON.parse(JSON.stringify(res.data))
            const rdata = JSON.parse(JSON.stringify(res.data))
            const defaultData = JSON.parse(JSON.stringify(res.data))
            const sidebarRoutes = filterAsyncRouter(sdata)
            const rewriteRoutes = filterAsyncRouter(rdata, false, true)
            const defaultRoutes = filterAsyncRouter(defaultData)
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
            asyncRoutes.forEach(route => { router.addRoute(route) })
            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(defaultRoutes)
            resolve(rewriteRoutes)
          })
        })
      }
    }
  })

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  const normalizedView = normalizeViewPath(view)
  if (!normalizedView) {
    return modules[MODULE_404] ? () => modules[MODULE_404]() : () => import('@/views/error/404.vue')
  }

  const candidates = new Set([normalizedView])
  if (!normalizedView.endsWith('/index')) {
    candidates.add(`${normalizedView}/index`)
  } else {
    candidates.add(normalizedView.slice(0, -'/index'.length))
  }
  if (normalizedView.startsWith('modules/')) {
    candidates.add(normalizedView.slice('modules/'.length))
  }

  for (const candidate of candidates) {
    if (moduleLoaderMap.has(candidate)) {
      return moduleLoaderMap.get(candidate)
    }
  }
  for (const [moduleKey, loader] of moduleLoaderMap.entries()) {
    for (const candidate of candidates) {
      if (moduleKey.endsWith(`/${candidate}`) || candidate.endsWith(`/${moduleKey}`)) {
        return loader
      }
    }
  }
  console.warn('[permission] component not found for route view:', view)
  return modules[MODULE_404] ? () => modules[MODULE_404]() : () => import('@/views/error/404.vue')
}

export default usePermissionStore
