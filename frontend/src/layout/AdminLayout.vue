<template>
  <div class="admin-layout">
    <header class="top-nav">
      <h1 class="app-title">闪借</h1>
      <div class="user-info">
        <span class="username">{{ adminName }}</span>
        <button class="logout-btn" type="button" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="layout-body">
      <aside class="sidebar">
        <nav aria-label="管理员菜单">
          <ul>
            <li :class="{ active: isDashboardPage('dashboard') }">
              <router-link :to="{ name: 'admin' }">
                <span class="nav-icon">⌂</span>
                <span class="nav-text">首页</span>
              </router-link>
            </li>
            <li :class="{ active: $route.name === 'admin-products' }">
              <router-link :to="{ name: 'admin-products' }">
                <span class="nav-icon">▣</span>
                <span class="nav-text">产品管理</span>
              </router-link>
            </li>
            <li :class="{ active: isDashboardPage('loans') }">
              <router-link :to="{ name: 'admin', query: { section: 'loans' } }">
                <span class="nav-icon">▤</span>
                <span class="nav-text">贷款申请</span>
              </router-link>
            </li>
            <li :class="{ active: ['admin-loan-applications', 'admin-loan-detail'].includes($route.name) }">
              <router-link :to="{ name: 'admin-loan-applications' }" aria-label="贷款申请管理">
                <span class="nav-icon">▤</span>
                <span class="nav-text">贷款申请管理</span>
              </router-link>
            </li>
            <li :class="{ active: isDashboardPage('statistics') }">
              <router-link :to="{ name: 'admin', query: { section: 'statistics' } }">
                <span class="nav-icon">◇</span>
                <span class="nav-text">风控报告</span>
              </router-link>
            </li>
            <li :class="{ active: isDashboardPage('settings') }">
              <router-link :to="{ name: 'admin', query: { section: 'settings' } }">
                <span class="nav-icon">⚙</span>
                <span class="nav-text">系统设置</span>
              </router-link>
            </li>
          </ul>
        </nav>
      </aside>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { logout } from '../api/admin'
import { clearAuth, getAdminUser } from '../api/client'

export default {
  name: 'AdminLayout',
  data() {
    return {
      adminName: '管理员'
    }
  },
  mounted() {
    const admin = getAdminUser()
    this.adminName = admin?.name || admin?.username || '管理员'
  },
  methods: {
    isDashboardPage(section) {
      if (this.$route.name !== 'admin') return false
      const currentSection = this.$route.query.section || 'dashboard'
      return currentSection === section
    },
    async handleLogout() {
      await logout()
      clearAuth()
      await this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  background-color: #d8b4fe;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-title {
  margin: 0;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-weight: 700;
}

.logout-btn {
  padding: 8px 16px;
  color: #fff;
  background-color: rgba(255, 255, 255, 0.2);
  border: 0;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.layout-body {
  min-height: calc(100vh - 60px);
  display: flex;
  background-color: #f5f5f5;
}

.sidebar {
  width: 220px;
  flex: 0 0 220px;
  padding: 20px 0;
  color: #fff;
  background: linear-gradient(to bottom, #d8b4fe, #c084fc);
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
}

.sidebar ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.sidebar li {
  margin-bottom: 5px;
}

.sidebar a {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #fff;
  text-decoration: none;
  border-left: 4px solid transparent;
  transition: background-color 0.2s, border-color 0.2s;
}

.sidebar a:hover,
.sidebar li.active a {
  background-color: rgba(255, 255, 255, 0.18);
  border-left-color: #fff;
}

.sidebar li.active a {
  font-weight: 700;
}

.nav-icon {
  width: 22px;
  margin-right: 10px;
  text-align: center;
}

.page-content {
  min-width: 0;
  flex: 1;
  overflow: auto;
}

@media (max-width: 768px) {
  .sidebar {
    width: 60px;
    flex-basis: 60px;
  }

  .sidebar a {
    justify-content: center;
    padding: 12px 8px;
  }

  .nav-icon {
    margin-right: 0;
  }

  .nav-text {
    display: none;
  }
}
</style>
