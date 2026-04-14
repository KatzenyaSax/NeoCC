// 弹窗封装
// 用法：
//   modal({ title, content, onConfirm, confirmText: '确定', onCancel })
//   返回 close() 函数，可手动关闭

export function modal({ title, content, onConfirm, confirmText = '确定', onCancel }) {
  // 遮罩
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';

  overlay.innerHTML = `
    <div class="modal-box">
      <div class="modal-header">
        <span class="modal-title">${title}</span>
        <button class="modal-close" id="modalCloseBtn">&times;</button>
      </div>
      <div class="modal-body">${content}</div>
      <div class="modal-footer">
        <button class="btn" id="modalCancelBtn">取消</button>
        <button class="btn btn-primary" id="modalConfirmBtn">${confirmText}</button>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  const close = () => {
    overlay.remove();
  };

  overlay.querySelector('#modalCloseBtn').addEventListener('click', close);
  overlay.querySelector('#modalCancelBtn').addEventListener('click', () => {
    if (onCancel) onCancel();
    close();
  });
  overlay.querySelector('#modalConfirmBtn').addEventListener('click', () => {
    if (onConfirm) onConfirm();
    close();
  });

  // 点击遮罩关闭
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      if (onCancel) onCancel();
      close();
    }
  });

  return { close };
}
