// 字典加载与查询
// 从后端 system 模块加载常用字典，存入 store

import store from './store.js';
import { get } from '../api/client.js';

const COMMON_DICTS = [
  'customer_status',   // 客户状态
  'customer_type',      // 客户类型
  'intention_level',    // 意向等级
  'contract_status',    // 合同状态
  'audit_status',       // 贷款审核状态
  'commission_status',  // 提成状态
  'service_fee_type',         // 服务费类型
  'service_fee_payment_status', // 服务费支付状态
  'performance_status', // 业绩状态
  'contact_type',       // 跟进类型
];

// 加载所有常用字典
export async function loadDicts() {
  const promises = COMMON_DICTS.map(dictType =>
    get(`/sysDict/listByDictType/${dictType}`)
      .then(items => ({ [dictType]: items }))
      .catch(() => ({ [dictType]: [] }))
  );

  const results = await Promise.all(promises);
  const allDicts = { ...store.get('dicts') };

  results.forEach(r => {
    const [key, value] = Object.entries(r)[0];
    allDicts[key] = value;
  });

  store.set('dicts', allDicts);
}

// 根据字典值查标签
export function getDictLabel(dictType, dictValue) {
  const items = store.get('dicts')[dictType] || [];
  const item = items.find(i => String(i.dictValue) === String(dictValue));
  return item ? item.dictLabel : dictValue ?? '';
}
