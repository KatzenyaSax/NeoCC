// 侧边栏组件
// 将渲染结果写入 #sidebar DOM 元素

import store from '../core/store.js';
import { navigate } from '../core/router.js';

export function renderSidebar() {
  const container = document.getElementById('sidebar');
  if (!container) return;

  const menuItems = store.get('menuItems') || getDefaultMenu();

  container.innerHTML = `
    <div class="sidebar-logo">
      <span>NeoCC</span>
    </div>
    <nav class="sidebar-nav">
      ${menuItems.map(item => `
        <div class="sidebar-group">
          ${item.children
            ? `<div class="sidebar-group-title">${item.label}</div>
               ${item.children.map(child => `
                 <a class="sidebar-link" data-path="${child.path}" href="#${child.path}">
                   ${child.label}
                 </a>
               `).join('')}`
            : `<a class="sidebar-link" data-path="${item.path}" href="#${item.path}">
                 ${item.label}
               </a>`
          }
        </div>
      `).join('')}
    </nav>
  `;

  // 绑定点击事件（事件委托）
  container.addEventListener('click', (e) => {
    const link = e.target.closest('.sidebar-link');
    if (link) {
      e.preventDefault();
      const path = link.dataset.path;
      if (path) navigate(path);
    }
  });
}

function getDefaultMenu() {
  return [
    { label: '工作台', path: '/dashboard' },
    {
      label: '系统管理',
      children: [
        { label: '用户管理', path: '/system/user' },
        { label: '角色管理', path: '/system/role' },
        { label: '权限管理', path: '/system/permission' },
        { label: '部门管理', path: '/system/department' },
        { label: '战区管理', path: '/system/zone' },
        { label: '数据字典', path: '/system/dict' },
        { label: '系统参数', path: '/system/param' },
        { label: '操作日志', path: '/system/log' },
      ],
    },
    {
      label: '销售管理',
      children: [
        { label: '客户管理', path: '/sales/customer' },
        { label: '合同管理', path: '/sales/contract' },
        { label: '工作日志', path: '/sales/worklog' },
      ],
    },
    {
      label: '金融审核',
      children: [
        { label: '银行管理', path: '/finance/bank' },
        { label: '金融产品', path: '/finance/product' },
        { label: '贷款审核', path: '/finance/loan-audit' },
        { label: '提成记录', path: '/finance/commission' },
        { label: '服务费记录', path: '/finance/service-fee' },
      ],
    },
  ];
}
