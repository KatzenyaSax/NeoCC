<template>
  <div class="app-container neocc-home">

    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-left">
        <div class="banner-title">
          <span class="brand">大富翁</span>
          <span class="subtitle">信贷管理系统</span>
        </div>
        <div class="banner-desc">欢迎回来，<strong>{{ nickName }}</strong> &nbsp;·&nbsp; {{ currentDate }}</div>
      </div>
      <div class="banner-right">
        <el-icon class="banner-icon"><DataAnalysis /></el-icon>
      </div>
    </div>

    <!-- 统计卡片行 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card" :class="'stat-card--' + card.color" @click="goPage(card.path)">
          <div class="stat-icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              <span v-if="card.loading">—</span>
              <span v-else>{{ card.value }}</span>
            </div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <div class="stat-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 中间区域：快捷功能 + 系统概览 -->
    <el-row :gutter="16" class="middle-row">

      <!-- 快捷功能入口 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <div class="card-header">
              <el-icon><Grid /></el-icon>
              <span>快捷功能</span>
            </div>
          </template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in quickLinks" :key="item.path">
              <div class="quick-item" @click="goPage(item.path)">
                <div class="quick-icon" :style="{ background: item.bg }">
                  <el-icon><component :is="item.icon" /></el-icon>
                </div>
                <div class="quick-name">{{ item.name }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 系统概览 + 技术栈 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="sys-card">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>系统概览</span>
            </div>
          </template>
          <div class="sys-info-list">
            <div class="sys-info-item" v-for="item in sysInfo" :key="item.label">
              <span class="sys-label">{{ item.label }}</span>
              <span class="sys-val">
                <el-tag v-if="item.tagType" :type="item.tagType" size="small">{{ item.value }}</el-tag>
                <span v-else>{{ item.value }}</span>
              </span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="tech-card">
          <template #header>
            <div class="card-header">
              <el-icon><SetUp /></el-icon>
              <span>技术栈</span>
            </div>
          </template>
          <div class="tech-tags">
            <el-tag v-for="t in techStack" :key="t.name" :type="t.type" effect="plain" class="tech-tag">{{ t.name }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部：业务模块卡片 -->
    <el-row :gutter="16" class="module-row">
      <el-col :xs="24" :sm="8" v-for="mod in modules" :key="mod.title">
        <el-card shadow="never" class="module-card" @click="goPage(mod.firstPath)">
          <div class="module-header" :style="{ background: mod.gradient }">
            <el-icon class="module-icon"><component :is="mod.icon" /></el-icon>
            <span class="module-title">{{ mod.title }}</span>
          </div>
          <div class="module-items">
            <div class="module-item" v-for="sub in mod.items" :key="sub">
              <el-icon><Check /></el-icon>
              <span>{{ sub }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup name="Index">
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { countCustomer, countContract, countContractByStatus } from '@/api/sales/statistics'
import { countUser } from '@/api/system/statistics'

const router = useRouter()
const userStore = useUserStore()

// 当前用户昵称
const nickName = computed(() => userStore.nickName || userStore.name || '管理员')

// 当前日期
const currentDate = computed(() => {
  const d = new Date()
  const weekArr = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekArr[d.getDay()]}`
})

// 统计卡片
const statCards = reactive([
  { key: 'customer', label: '客户总数', value: 0, loading: true, icon: 'User', color: 'rose', path: '/customer-list' },
  { key: 'contract', label: '合同总数', value: 0, loading: true, icon: 'Document', color: 'brick', path: '/contract-list' },
  { key: 'loan', label: '待审贷款', value: 0, loading: true, icon: 'Finished', color: 'terra', path: '/loan-audit' },
  { key: 'role', label: '员工数量', value: 0, loading: true, icon: 'Avatar', color: 'mauve', path: '/user' },
])

// 加载统计数据
function loadStats() {
  countCustomer().then(res => {
    const card = statCards.find(c => c.key === 'customer')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'customer').loading = false })

  countContract().then(res => {
    const card = statCards.find(c => c.key === 'contract')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'contract').loading = false })

  countContractByStatus(4).then(res => {
    const card = statCards.find(c => c.key === 'loan')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'loan').loading = false })

  countUser().then(res => {
    const card = statCards.find(c => c.key === 'role')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'role').loading = false })
}

// 快捷功能
const quickLinks = [
  { name: '客户管理', path: '/customer-list', icon: 'User', bg: 'linear-gradient(135deg,#667eea,#764ba2)' },
  { name: '合同管理', path: '/contract-list', icon: 'Document', bg: 'linear-gradient(135deg,#f093fb,#f5576c)' },
  { name: '工作日志', path: '/log', icon: 'Notebook', bg: 'linear-gradient(135deg,#4facfe,#00f2fe)' },
  { name: '贷款审核', path: '/loan-audit', icon: 'Finished', bg: 'linear-gradient(135deg,#43e97b,#38f9d7)' },
  { name: '佣金记录', path: '/commission', icon: 'Money', bg: 'linear-gradient(135deg,#fa709a,#fee140)' },
  { name: '用户管理', path: '/user', icon: 'Setting', bg: 'linear-gradient(135deg,#a18cd1,#fbc2eb)' },
]

// 系统信息
const sysInfo = [
  { label: '系统名称', value: '大富翁管理系统' },
  { label: '当前版本', value: 'v1.0.0' },
  { label: '后端框架', value: 'Spring Boot 3 + Spring Cloud' },
  { label: '前端框架', value: 'Vue 3 + Element Plus' },
  { label: '数据库', value: 'MySQL 8 + MyBatis-Plus' },
  { label: '系统状态', value: '运行中', tagType: 'success' },
]

// 技术栈
const techStack = [
  { name: 'Spring Boot 3', type: 'primary' },
  { name: 'Spring Security', type: 'primary' },
  { name: 'Spring Cloud Gateway', type: 'info' },
  { name: 'MyBatis-Plus', type: 'success' },
  { name: 'MySQL 8', type: 'success' },
  { name: 'Redis', type: 'danger' },
  { name: 'Docker', type: 'info' },
  { name: 'RabbitMQ', type: 'warning' },
  { name: 'Vue 3', type: 'success' },
  { name: 'Element Plus', type: 'info' },
  { name: 'Vite', type: 'warning' },
  { name: 'Nginx', type: 'success' },
]

// 业务模块
const modules = [
  {
    title: '销售管理',
    icon: 'ShoppingCart',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    firstPath: '/sales/customer',
    items: ['客户管理', '合同管理', '跟进记录', '工作日志', '业绩记录', '客户转移'],
  },
  {
    title: '财务管理',
    icon: 'Money',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    firstPath: '/finance/loan-audit',
    items: ['贷款审核', '佣金记录', '服务费记录', '银行管理', '金融产品'],
  },
  {
    title: '系统管理',
    icon: 'Setting',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    firstPath: '/system/user',
    items: ['用户管理', '角色管理', '菜单管理', '部门管理', '区域管理'],
  },
]

function goPage(path) {
  router.push(path)
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.neocc-home {
  min-height: calc(100vh - 130px);
  background: #f4f6fb;
  padding: 16px !important;

  /* ===== 欢迎横幅 ===== */
  .welcome-banner {
    background: linear-gradient(135deg, #6b3a3a 0%, #8b5e5e 60%, #a07878 100%);
    border-radius: 12px;
    padding: 28px 32px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 4px 20px rgba(139, 94, 94, 0.3);
    color: #fff;

    .banner-title {
      display: flex;
      align-items: baseline;
      gap: 12px;
      margin-bottom: 8px;
      .brand {
        font-size: 32px;
        font-weight: 800;
        letter-spacing: 2px;
        color: #fff;
      }
      .subtitle {
        font-size: 16px;
        color: rgba(255,255,255,0.75);
        font-weight: 400;
      }
    }
    .banner-desc {
      font-size: 14px;
      color: rgba(255,255,255,0.8);
      strong { color: #fff; font-weight: 600; }
    }
    .banner-icon {
      font-size: 72px;
      color: rgba(255,255,255,0.15);
    }
  }

  /* ===== 统计卡片 ===== */
  .stat-row {
    margin-bottom: 16px;

    .stat-card {
      border-radius: 10px;
      padding: 20px 20px 16px;
      display: flex;
      align-items: center;
      gap: 14px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      position: relative;
      overflow: hidden;
      margin-bottom: 4px;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 24px rgba(0,0,0,0.14);
      }

      .stat-icon {
        width: 52px;
        height: 52px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 26px;
        flex-shrink: 0;
        background: rgba(255,255,255,0.25);
        color: #fff;
      }
      .stat-info {
        flex: 1;
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #fff;
          line-height: 1.2;
        }
        .stat-label {
          font-size: 13px;
          color: rgba(255,255,255,0.85);
          margin-top: 3px;
        }
      }
      .stat-arrow {
        color: rgba(255,255,255,0.5);
        font-size: 18px;
      }

      &--rose  { background: linear-gradient(135deg, #c99e9e 0%, #ae6e6e 100%); box-shadow: 0 4px 15px rgba(174,110,110,0.3); }
      &--brick { background: linear-gradient(135deg, #bf9688 0%, #a07260 100%); box-shadow: 0 4px 15px rgba(160,114,96,0.3); }
      &--terra { background: linear-gradient(135deg, #c49a8a 0%, #a87060 100%); box-shadow: 0 4px 15px rgba(168,112,96,0.3); }
      &--mauve { background: linear-gradient(135deg, #c09ca0 0%, #a07078 100%); box-shadow: 0 4px 15px rgba(160,112,120,0.3); }
    }
  }

  /* ===== 中间卡片 ===== */
  .middle-row {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      .el-icon { color: var(--el-color-primary, #c2410c); font-size: 17px; }
    }

    /* 快捷功能 */
    .quick-card {
      height: 100%;
      border-radius: 10px;
      border: none;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);

      .quick-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px 8px;
        border-radius: 10px;
        cursor: pointer;
        transition: background 0.2s, transform 0.2s;
        margin-bottom: 8px;

        &:hover {
          background: #f0f4ff;
          transform: translateY(-2px);
          .quick-icon { transform: scale(1.08); }
        }

        .quick-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 22px;
          color: #fff;
          margin-bottom: 8px;
          transition: transform 0.2s;
          box-shadow: 0 3px 10px rgba(0,0,0,0.15);
        }
        .quick-name {
          font-size: 13px;
          color: #606266;
          font-weight: 500;
          text-align: center;
        }
      }
    }

    /* 系统信息 */
    .sys-card {
      border-radius: 10px;
      border: none;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);
      margin-bottom: 16px;

      .sys-info-list {
        .sys-info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 9px 0;
          border-bottom: 1px solid #f0f0f0;
          &:last-child { border-bottom: none; }
        }
        .sys-label { font-size: 13px; color: #909399; }
        .sys-val { font-size: 13px; color: #303133; font-weight: 500; }
      }
    }

    /* 技术栈 */
    .tech-card {
      border-radius: 10px;
      border: none;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);

      .tech-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        .tech-tag { border-radius: 6px; cursor: default; }
      }
    }
  }

  /* ===== 业务模块 ===== */
  .module-row {
    .module-card {
      border-radius: 10px;
      border: none;
      box-shadow: 0 2px 12px rgba(0,0,0,0.06);
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      overflow: hidden;
      margin-bottom: 4px;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0,0,0,0.12);
      }

      :deep(.el-card__body) { padding: 0 !important; }

      .module-header {
        padding: 20px 24px;
        display: flex;
        align-items: center;
        gap: 12px;
        .module-icon { font-size: 28px; color: #fff; }
        .module-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: 1px; }
      }

      .module-items {
        padding: 16px 24px;
        background: #fff;
        display: flex;
        flex-wrap: wrap;
        gap: 8px 0;

        .module-item {
          width: 50%;
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: #606266;
          padding: 4px 0;
          .el-icon { color: #67c23a; font-size: 14px; }
        }
      }
    }
  }
}
</style>
