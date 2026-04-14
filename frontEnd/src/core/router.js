// Hash 路由实现
// 路由表：path → render 函数
// 使用方式：navigate('/dashboard')

const routes = {};

// 外部注册路由
export function registerRoute(path, fn) {
  routes[path] = fn;
}

// 路由跳转
export function navigate(path) {
  location.hash = '#' + path;
}

// 初始化路由监听
export function initRouter() {
  window.addEventListener('hashchange', handleRoute);
  // 触发首次路由
  handleRoute();
}

// 处理路由
function handleRoute() {
  const hash = location.hash.slice(1) || '/login';

  // 1. 精确匹配
  if (routes[hash]) {
    clearApp();
    routes[hash]();
    return;
  }

  // 2. 带参数匹配（如 /sales/customer/:id）
  for (const pattern of Object.keys(routes)) {
    const { regex, paramNames } = buildRegex(pattern);
    const match = hash.match(regex);
    if (match) {
      clearApp();
      const params = paramNames.map((_, i) => match[i + 1]);
      routes[pattern](...params);
      return;
    }
  }

  // 3. 404
  clearApp();
  document.getElementById('app').innerHTML = '<p>404 Not Found</p>';
}

function clearApp() {
  document.getElementById('app').innerHTML = '';
}

// 缓存正则表达式，避免重复构建
const regexCache = new Map();

function buildRegex(pattern) {
  if (regexCache.has(pattern)) return regexCache.get(pattern);

  const paramNames = [];
  const regexStr = pattern.replace(/:([^/]+)/g, (_, name) => {
    paramNames.push(name);
    return '([^/]+)';
  });

  const result = { regex: new RegExp(`^${regexStr}$`), paramNames };
  regexCache.set(pattern, result);
  return result;
}
