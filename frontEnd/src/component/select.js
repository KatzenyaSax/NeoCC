// 字典下拉选择框
// 用法：renderDictSelect(el, 'customer_status', { value: '1', onChange })
// el: DOM 元素或选择器字符串

import store from '../core/store.js';

export function renderDictSelect(el, dictType, { value = '', placeholder = '请选择', onChange } = {}) {
  if (typeof el === 'string') {
    el = document.querySelector(el);
  }
  if (!el) return;

  const items = store.get('dicts')[dictType] || [];

  el.innerHTML = `
    <select class="dict-select" data-dict-type="${dictType}">
      <option value="">${placeholder}</option>
      ${items.map(item => `
        <option value="${item.dictValue}" ${String(item.dictValue) === String(value) ? 'selected' : ''}>
          ${item.dictLabel}
        </option>
      `).join('')}
    </select>
  `;

  const select = el.querySelector('select');
  select.addEventListener('change', () => {
    if (onChange) onChange(select.value);
  });
}
