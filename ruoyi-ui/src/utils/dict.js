/**
 * 获取字典数据 - NeoCC: 后端字典接口已删除，使用本地静态字典
 */
export function useDict(...args) {
  const res = ref({})
  return (() => {
    args.forEach((dictType, index) => {
      // NeoCC: 字典接口已删除，返回空数组
      res.value[dictType] = []
    })
    return toRefs(res.value)
  })()
}
