// 仪表盘页
// 路由：/#/dashboard

import { requireAuth } from '../core/auth.js';
import { get } from '../api/client.js';
import { renderHeader } from '../component/header.js';

export function renderDashboardPage() {
  if (!requireAuth()) return;

  renderHeader();

  const app = document.getElementById('app');
  app.innerHTML = `
    <div class="page-header">
      <h2>工作台</h2>
    </div>
    <div class="dashboard-loading" id="dashboardLoading">加载中...</div>
    <div class="dashboard-content" id="dashboardContent" style="display:none;">
      <div class="stat-cards" id="statCards"></div>
      <div class="dashboard-recent" id="recentLists"></div>
    </div>
    <div class="dashboard-error" id="dashboardError" style="display:none;"></div>
  `;

  loadDashboardData();
}

async function loadDashboardData() {
  const loadingEl = document.getElementById('dashboardLoading');
  const contentEl = document.getElementById('dashboardContent');
  const errorEl = document.getElementById('dashboardError');

  try {
    // 并行请求多个数据
    const [customerStats, contractStats] = await Promise.all([
      get('/customer/listByStatus').catch(() => []),
      get('/contract/listByStatus').catch(() => []),
    ]);

    loadingEl.style.display = 'none';
    contentEl.style.display = 'block';

    renderStatCards(customerStats, contractStats);

  } catch (err) {
    loadingEl.style.display = 'none';
    errorEl.style.display = 'block';
    errorEl.textContent = '数据加载失败：' + err.message;
  }
}

function renderStatCards(customerStats, contractStats) {
  const container = document.getElementById('statCards');
  if (!container) return;

  // 计算合计
  const customerTotal = Array.isArray(customerStats)
    ? customerStats.reduce((sum, s) => sum + (s.count || 0), 0)
    : 0;
  const contractTotal = Array.isArray(contractStats)
    ? contractStats.reduce((sum, s) => sum + (s.count || 0), 0)
    : 0;

  container.innerHTML = `
    <div class="stat-card">
      <div class="stat-label">客户总数</div>
      <div class="stat-value">${customerTotal}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">合同总数</div>
      <div class="stat-value">${contractTotal}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">今日日期</div>
      <div class="stat-value">${new Date().toLocaleDateString()}</div>
    </div>
  `;
}
