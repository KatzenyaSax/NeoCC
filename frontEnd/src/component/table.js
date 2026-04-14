// 通用数据表格组件
// 用法：
//   renderTable(container, columns, records, pagination, onPageChange)
//   columns: [ { label, field, render?: (val, row) => string } ]
//   pagination: { total, page, size } 或 null（无分页）

export function renderTable(container, columns, records, pagination, onPageChange) {
  if (typeof container === 'string') {
    container = document.querySelector(container);
  }

  // 表头
  const thead = `
    <thead>
      <tr>
        ${columns.map(col => `<th>${col.label}</th>`).join('')}
      </tr>
    </thead>
  `;

  // 数据行
  const tbody = `
    <tbody>
      ${records.length === 0
        ? `<tr><td colspan="${columns.length}" class="table-empty">暂无数据</td></tr>`
        : records.map(row => `
          <tr>
            ${columns.map(col => {
              const val = row[col.field];
              const text = col.render ? col.render(val, row) : (val ?? '');
              return `<td>${text}</td>`;
            }).join('')}
          </tr>
        `).join('')
      }
    </tbody>
  `;

  // 分页
  let paginationHtml = '';
  if (pagination) {
    const { total, page, size } = pagination;
    const totalPages = Math.ceil(total / size);
    paginationHtml = `
      <div class="pagination">
        <span class="pagination-info">共 ${total} 条</span>
        <button class="btn btn-sm" ${page <= 1 ? 'disabled' : ''} data-page="${page - 1}">上一页</button>
        <span class="pagination-current">第 ${page} / ${totalPages} 页</span>
        <button class="btn btn-sm" ${page >= totalPages ? 'disabled' : ''} data-page="${page + 1}">下一页</button>
      </div>
    `;
  }

  container.innerHTML = `
    <table class="data-table">${thead}${tbody}</table>
    ${paginationHtml}
  `;

  // 绑定分页事件
  if (pagination && onPageChange) {
    container.querySelectorAll('.pagination button[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        const newPage = parseInt(btn.dataset.page);
        if (!btn.disabled) onPageChange(newPage);
      });
    });
  }
}
