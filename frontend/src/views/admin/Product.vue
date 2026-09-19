<template>
  <section class="product-page">
    <div class="page-header">
      <div>
        <h1>产品管理</h1>
        <p>维护贷款产品的额度、期限、利率和启用状态。</p>
      </div>
      <button class="primary-btn" type="button" :disabled="busy" @click="openCreateForm">新增产品</button>
    </div>

    <form class="filter-card" aria-label="产品筛选" @submit.prevent="applyFilters">
      <label class="filter-field name-filter">
        <span>产品名称</span>
        <input v-model="filters.name" type="search" placeholder="请输入产品名称" maxlength="100">
      </label>
      <label class="filter-field">
        <span>产品类型</span>
        <select v-model="filters.type">
          <option value="">全部</option>
          <option value="GENERAL">普通贷</option>
          <option value="CONSUME">消费贷</option>
        </select>
      </label>
      <label class="filter-field">
        <span>产品状态</span>
        <select v-model="filters.status">
          <option value="">全部</option>
          <option value="active">启用</option>
          <option value="inactive">禁用</option>
        </select>
      </label>
      <div class="filter-actions">
        <button class="primary-btn" type="submit" :disabled="busy">搜索</button>
        <button class="secondary-btn" type="button" :disabled="busy" @click="resetFilters">重置</button>
      </div>
    </form>

    <p v-if="actionError" class="feedback error-feedback" role="alert">{{ actionError }}</p>
    <p v-if="successMessage" class="feedback success-feedback" role="status">{{ successMessage }}</p>

    <section class="table-card" aria-labelledby="product-list-title" :aria-busy="loading">
      <div class="table-heading">
        <h2 id="product-list-title">产品列表</h2>
        <span v-if="!loading && !loadError" class="result-count" role="status">共 {{ filteredProducts.length }} 个产品</span>
      </div>
      <div class="table-scroll" tabindex="0" role="region" aria-label="产品列表表格">
        <table>
          <thead>
            <tr>
              <th scope="col">产品名称</th>
              <th scope="col">产品类型</th>
              <th scope="col">额度范围</th>
              <th scope="col">期限</th>
              <th scope="col">利率</th>
              <th scope="col">状态</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="state-message" role="status">正在加载产品...</td>
            </tr>
            <tr v-else-if="loadError">
              <td colspan="7" class="state-message">
                <div class="error-message" role="alert">
                  <span>{{ loadError }}</span>
                  <button type="button" class="text-btn" :disabled="busy" @click="loadProducts">重新加载</button>
                </div>
              </td>
            </tr>
            <tr v-else-if="filteredProducts.length === 0">
              <td colspan="7" class="empty-cell">{{ products.length ? '没有符合筛选条件的产品，请调整筛选条件。' : '暂无产品数据，可点击“新增产品”创建。' }}</td>
            </tr>
            <template v-else>
            <tr v-for="product in filteredProducts" :key="product.id">
              <td class="product-name">{{ product.productName || '—' }}</td>
              <td>{{ productTypeText(product.productType) }}</td>
              <td>{{ formatMoney(product.minAmount) }} - {{ formatMoney(product.maxAmount) }}</td>
              <td>{{ product.minTerm }} - {{ product.maxTerm }} 个月</td>
              <td>{{ product.interestRate }}%</td>
              <td>
                <span :class="['status-tag', { enabled: normalizeStatus(product.status) === 'active', disabled: normalizeStatus(product.status) === 'inactive' }]">
                  {{ statusText(product.status) }}
                </span>
              </td>
              <td>
                <div class="actions">
                  <button type="button" class="text-btn" :disabled="busy" @click="openEditForm(product, $event)">编辑</button>
                  <button type="button" class="text-btn danger" :disabled="busy" @click="removeProduct(product)">{{ deletingId === product.id ? '删除中...' : '删除' }}</button>
                </div>
              </td>
            </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div ref="formDialog" class="modal" role="dialog" aria-modal="true" :aria-labelledby="formTitleId" @keydown.esc.prevent.stop="closeForm" @keydown.tab="trapFocus">
        <div class="modal-header">
          <h3 :id="formTitleId">{{ editingId === null ? '新增产品' : '编辑产品' }}</h3>
          <button class="close-btn" type="button" aria-label="关闭" :disabled="saving" @click="closeForm">×</button>
        </div>

        <form novalidate @submit.prevent="submitProduct">
          <fieldset class="form-grid" :disabled="saving">
            <label class="form-field full-width">
              <span>产品名称</span>
              <input ref="productNameInput" v-model.trim="form.productName" type="text" placeholder="请输入产品名称" required maxlength="100">
            </label>

            <label class="form-field full-width">
              <span>产品类型</span>
              <select v-model="form.productType" required>
                <option value="GENERAL">普通贷</option>
                <option value="CONSUME">消费贷</option>
              </select>
            </label>

            <label class="form-field">
              <span>最低额度（元）</span>
              <input v-model.number="form.minAmount" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>最高额度（元）</span>
              <input v-model.number="form.maxAmount" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>最短期限（月）</span>
              <input v-model.number="form.minTerm" type="number" min="1" step="1" required>
            </label>

            <label class="form-field">
              <span>最长期限（月）</span>
              <input v-model.number="form.maxTerm" type="number" min="1" step="1" required>
            </label>

            <label class="form-field">
              <span>年利率（%）</span>
              <input v-model.number="form.interestRate" type="number" min="0" step="0.01" required>
            </label>

            <label class="form-field">
              <span>状态</span>
              <select v-model="form.status" required>
                <option value="active">启用</option>
                <option value="inactive">禁用</option>
              </select>
            </label>
          </fieldset>

          <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>

          <div class="modal-footer">
            <button type="button" class="secondary-btn" :disabled="saving" @click="closeForm">取消</button>
            <button type="submit" class="primary-btn" :disabled="saving">
              {{ saving ? '提交中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script>
import {
  getProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  normalizeProductStatus
} from '../../api/product'

function createEmptyForm() {
  return {
    productName: '',
    productType: 'GENERAL',
    minAmount: 1000,
    maxAmount: 100000,
    minTerm: 1,
    maxTerm: 12,
    interestRate: 8.5,
    status: 'active'
  }
}

function createEmptyFilters() {
  return { name: '', type: '', status: '' }
}

function isValidNumber(value) {
  return (typeof value === 'number' || typeof value === 'string') &&
    String(value).trim() !== '' && Number.isFinite(Number(value))
}

export default {
  name: 'AdminProduct',
  data() {
    return {
      products: [],
      filters: createEmptyFilters(),
      appliedFilters: createEmptyFilters(),
      loading: false,
      loadError: '',
      actionError: '',
      successMessage: '',
      deletingId: null,
      showForm: false,
      editingId: null,
      form: createEmptyForm(),
      formError: '',
      saving: false,
      formTrigger: null,
      formTitleId: 'product-form-title'
    }
  },
  computed: {
    busy() {
      return this.loading || this.saving || this.deletingId !== null
    },
    filteredProducts() {
      // API 保持不变；名称、类型、状态均在已加载的列表中筛选。
      const { name, type, status } = this.appliedFilters
      const keyword = name.trim().toLocaleLowerCase()
      return this.products.filter(product =>
        (!keyword || String(product.productName || '').toLocaleLowerCase().includes(keyword)) &&
        (!type || this.normalizeProductType(product.productType) === type) &&
        (!status || this.normalizeStatus(product.status) === status)
      )
    }
  },
  mounted() {
    this.loadProducts()
  },
  methods: {
    applyFilters() {
      this.appliedFilters = { ...this.filters, name: this.filters.name.trim() }
    },
    resetFilters() {
      this.filters = createEmptyFilters()
      this.appliedFilters = createEmptyFilters()
    },
    async loadProducts() {
      if (this.loading) return false
      this.loading = true
      this.loadError = ''
      try {
        const products = await getProducts()
        if (!Array.isArray(products)) throw new Error('产品列表返回格式异常')
        this.products = products
        return true
      } catch (error) {
        this.loadError = error?.message || '产品列表加载失败，请稍后重试'
        return false
      } finally {
        this.loading = false
      }
    },
    openCreateForm(event) {
      if (this.busy) return
      this.editingId = null
      this.form = createEmptyForm()
      this.formError = ''
      this.openForm(event)
    },
    openEditForm(product, event) {
      if (this.busy) return
      this.editingId = product.id
      this.form = {
        productName: product.productName || '',
        productType: this.normalizeProductType(product.productType),
        minAmount: Number(product.minAmount),
        maxAmount: Number(product.maxAmount),
        minTerm: Number(product.minTerm),
        maxTerm: Number(product.maxTerm),
        interestRate: Number(product.interestRate),
        status: this.normalizeStatus(product.status)
      }
      this.formError = ''
      this.openForm(event)
    },
    openForm(event) {
      this.formTrigger = event?.currentTarget || null
      this.actionError = ''
      this.successMessage = ''
      this.showForm = true
      this.$nextTick(() => this.$refs.productNameInput?.focus())
    },
    closeForm() {
      if (this.saving) return
      this.showForm = false
      this.formError = ''
      this.$nextTick(() => this.formTrigger?.focus())
    },
    trapFocus(event) {
      const controls = this.$refs.formDialog.querySelectorAll('button:not(:disabled), input:not(:disabled), select:not(:disabled)')
      if (!controls.length) {
        event.preventDefault()
        return
      }
      const first = controls[0]
      const last = controls[controls.length - 1]
      if (event.shiftKey && event.target === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && event.target === last) {
        event.preventDefault()
        first.focus()
      }
    },
    validateForm() {
      const form = this.form
      if (!String(form.productName || '').trim()) return '产品名称不能为空'
      if (form.productName.trim().length > 100) return '产品名称不能超过100个字符'
      if (!['GENERAL', 'CONSUME'].includes(form.productType)) return '请选择有效的产品类型'
      if (![form.minAmount, form.maxAmount].every(value => isValidNumber(value) && Number(value) >= 0)) {
        return '额度必须为大于或等于0的有效数字'
      }
      if (Number(form.minAmount) > Number(form.maxAmount)) {
        return '最低额度不能大于最高额度'
      }
      if (![form.minTerm, form.maxTerm].every(value => isValidNumber(value) && Number.isInteger(Number(value)) && Number(value) > 0)) {
        return '期限必须为大于0的整数（月）'
      }
      if (Number(form.minTerm) > Number(form.maxTerm)) {
        return '最短期限不能大于最长期限'
      }
      if (!isValidNumber(form.interestRate) || Number(form.interestRate) < 0) return '利率必须为大于或等于0的有效数字'
      if (!['active', 'inactive'].includes(form.status)) return '请选择有效的产品状态'
      return ''
    },
    async submitProduct() {
      if (this.saving) return
      this.formError = this.validateForm()
      if (this.formError) return

      this.saving = true
      try {
        // 沿用现有 API 字段与 active/inactive 状态提交约定。
        const payload = {
          ...this.form,
          productName: this.form.productName.trim(),
          minAmount: Number(this.form.minAmount),
          maxAmount: Number(this.form.maxAmount),
          minTerm: Number(this.form.minTerm),
          maxTerm: Number(this.form.maxTerm),
          interestRate: Number(this.form.interestRate)
        }
        if (this.editingId === null) {
          await createProduct(payload)
        } else {
          await updateProduct(this.editingId, payload)
        }
        const refreshed = await this.loadProducts()
        const action = this.editingId === null ? '新增' : '更新'
        this.successMessage = refreshed ? `产品${action}成功` : `产品${action}成功，但列表刷新失败，请重新加载。`
        this.showForm = false
        this.$nextTick(() => this.formTrigger?.focus())
      } catch (error) {
        this.formError = error?.message || '产品保存失败，请稍后重试'
      } finally {
        this.saving = false
      }
    },
    async removeProduct(product) {
      if (this.busy) return
      if (!window.confirm(`确定删除产品“${product.productName}”吗？`)) return
      this.deletingId = product.id
      this.actionError = ''
      this.successMessage = ''
      try {
        await deleteProduct(product.id)
        const refreshed = await this.loadProducts()
        this.successMessage = refreshed ? '产品删除成功' : '产品删除成功，但列表刷新失败，请重新加载。'
      } catch (error) {
        this.actionError = error?.message || '产品删除失败，请稍后重试'
      } finally {
        this.deletingId = null
      }
    },
    normalizeStatus(status) {
      return normalizeProductStatus(status)
    },
    statusText(status) {
      const normalized = this.normalizeStatus(status)
      return normalized === 'active' ? '启用' : normalized === 'inactive' ? '禁用' : '未知状态'
    },
    productTypeText(type) {
      const labels = {
        GENERAL: '普通贷',
        CONSUME: '消费贷',
        个人消费贷款: '消费贷',
        个人消费贷: '消费贷'
      }
      return labels[type] || type || '-'
    },
    normalizeProductType(type) {
      if (type === 'CONSUME' || String(type || '').includes('消费')) {
        return 'CONSUME'
      }
      if (type === 'GENERAL' || String(type || '').includes('普通')) return 'GENERAL'
      return type || ''
    },
    formatMoney(value) {
      return `¥${Number(value || 0).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })}`
    }
  }
}
</script>

<style scoped>
.product-page {
  --product-blue: #1677ff;
  --product-text: #142239;
  --product-muted: #687991;
  --product-border: #e9eff7;
  min-height: 100%;
  padding: 36px;
  color: var(--product-text);
  background: #f2f7fc;
  font-family: Arial, 'Microsoft YaHei', sans-serif;
  line-height: 1.5;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 700;
}

.page-header p {
  margin: 0;
  color: var(--product-muted);
  font-size: 14px;
}

.filter-card,
.table-card {
  overflow: hidden;
  background: #fff;
  border: 1px solid #edf2f8;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(31, 73, 125, .035);
}

.filter-card {
  display: grid;
  grid-template-columns: minmax(180px, 2fr) repeat(2, minmax(130px, 1fr)) auto;
  align-items: end;
  gap: 18px;
  padding: 24px;
  margin-bottom: 24px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  font-size: 14px;
}

.filter-actions { display: flex; gap: 10px; }
.table-heading { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; padding: 20px 24px; }
.table-heading h2 { margin: 0; font-size: 18px; }
.result-count { color: var(--product-muted); font-size: 13px; }
.feedback { margin: 0 0 20px; padding: 12px 16px; border-radius: 8px; font-size: 14px; overflow-wrap: anywhere; }
.error-feedback { color: #b91c1c; background: #fff1f0; border: 1px solid #ffccc7; }
.success-feedback { color: #08794a; background: #eaf8ef; border: 1px solid #c9ebd6; }
.product-name { font-weight: 600; max-width: 240px; white-space: normal; overflow-wrap: anywhere; }
button:focus-visible, .table-scroll:focus-visible { outline: 2px solid var(--product-blue); outline-offset: 3px; }
.primary-btn, .secondary-btn { white-space: nowrap; }

input, select { font-family: inherit; font-size: 14px; }
button { font-family: inherit; font-size: 14px; }
fieldset { min-width: 0; }
fieldset:disabled { opacity: .7; }

.table-scroll {
  outline-offset: -3px;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 850px;
  border-collapse: collapse;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

th,
td {
  padding: 14px 16px;
  text-align: left;
  white-space: nowrap;
  border-bottom: 1px solid var(--product-border);
}

th {
  color: #4a5e79;
  font-size: 14px;
  background-color: #f2f6fb;
}

tbody tr:hover {
  background-color: #fafcff;
}

.actions {
  display: flex;
  gap: 12px;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 13px;
  border-radius: 999px;
  color: var(--product-muted);
  background: #f2f6fb;
}

.status-tag.enabled {
  color: #08794a;
  background: #e0f5e9;
}

.status-tag.disabled {
  color: #58677b;
  background: #eef1f5;
}

.primary-btn,
.secondary-btn,
.text-btn,
.close-btn {
  border: 0;
  cursor: pointer;
}

.primary-btn,
.secondary-btn {
  padding: 9px 18px;
  border-radius: 6px;
  min-height: 40px;
}

.primary-btn {
  color: #fff;
  background-color: var(--product-blue);
}

.primary-btn:hover:not(:disabled) {
  background-color: #0965d9;
}

.secondary-btn {
  color: #4a5e79;
  background-color: #f2f6fb;
  border: 1px solid #dce5f0;
}

.secondary-btn:hover:not(:disabled) { background: #e7eff8; }

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.text-btn {
  padding: 0;
  color: #0965d9;
  background: transparent;
}

.text-btn.danger {
  color: #dc2626;
}

.state-message,
.empty-cell {
  padding: 40px 20px;
  color: var(--product-muted);
  text-align: center;
}

.error-message {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  color: #b91c1c;
  white-space: normal;
  overflow-wrap: anywhere;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(20, 34, 57, .4);
}

.modal {
  width: min(620px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.modal-header,
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid var(--product-border);
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  color: #777;
  font-size: 26px;
  line-height: 1;
  background: transparent;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  padding: 22px;
  margin: 0;
  border: 0;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--product-text);
  font-size: 14px;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field input,
.form-field select,
.filter-field input,
.filter-field select {
  box-sizing: border-box;
  width: 100%;
  min-height: 42px;
  padding: 10px 12px;
  color: var(--product-text);
  background: #fff;
  border: 1px solid #dce5f0;
  border-radius: 6px;
  outline: none;
}

.form-field input:focus,
.form-field select:focus,
.filter-field input:focus,
.filter-field select:focus {
  border-color: var(--product-blue);
  box-shadow: 0 0 0 2px rgba(22, 119, 255, .12);
}

.form-error {
  margin: -8px 22px 16px;
  color: #dc2626;
  font-size: 14px;
}

.modal-footer {
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid var(--product-border);
  border-bottom: 0;
}

@media (max-width: 1100px) {
  .product-page { padding: 28px 24px; }
  .filter-card { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 640px) {
  .product-page {
    padding: 16px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .page-header h1 { font-size: 25px; }
  .filter-card { grid-template-columns: minmax(0, 1fr); padding: 18px; gap: 14px; }
  .table-heading { padding: 18px; }
  .filter-actions button { flex: 1; }
  .modal-overlay { padding: 12px; }
  .modal { max-height: calc(100dvh - 24px); }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-field.full-width {
    grid-column: auto;
  }
}
</style>
