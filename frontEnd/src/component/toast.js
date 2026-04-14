// Toast 提示组件
// 用法：toast('操作成功', 'success')
// type: info | success | error | warning

export function toast(message, type = 'info') {
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = message;
  document.body.appendChild(el);

  // 3 秒后自动移除
  setTimeout(() => {
    el.classList.add('toast-fade-out');
    setTimeout(() => el.remove(), 300);
  }, 3000);
}
