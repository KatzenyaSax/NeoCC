// 极简 pub/sub 状态管理
// 使用方式：
//   store.get('token')
//   store.set('token', 'abc')
//   store.subscribe(state => console.log(state))

const store = {
  state: {
    token: localStorage.getItem('token') || null,
    currentUser: null,          // 当前登录用户对象
    dicts: {},                   // { dictType: [ {dictLabel, dictValue}, ... ] }
    permissions: [],            // 当前用户权限码列表
    menuItems: [],               // 侧边栏菜单项
  },
  listeners: [],

  get(key) {
    return this.state[key];
  },

  set(key, value) {
    this.state[key] = value;
    this.listeners.forEach(fn => fn(this.state));
  },

  subscribe(fn) {
    this.listeners.push(fn);
    return () => {
      this.listeners = this.listeners.filter(f => f !== fn);
    };
  },
};

export default store;
