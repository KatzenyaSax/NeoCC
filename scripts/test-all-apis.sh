#!/bin/bash

echo "=========================================="
echo "🔍 NeoCC 系统全面测试"
echo "=========================================="
echo ""

# 测试计数器
TOTAL=0
PASS=0
FAIL=0

# 测试函数
test_api() {
    local name=$1
    local url=$2
    local method=${3:-"GET"}
    local data=${4:-""}
    
    TOTAL=$((TOTAL + 1))
    echo "测试 $TOTAL: $name"
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$url" -H "Content-Type: application/json" -d "$data")
    else
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    fi
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ PASS (HTTP $HTTP_CODE)"
        PASS=$((PASS + 1))
    else
        echo "❌ FAIL (HTTP $HTTP_CODE)"
        FAIL=$((FAIL + 1))
    fi
    echo ""
}

test_content_type() {
    local name=$1
    local url=$2
    
    TOTAL=$((TOTAL + 1))
    echo "测试 $TOTAL: $name"
    
    CONTENT_TYPE=$(curl -sI "$url" | grep -i "content-type" | tr -d '\r')
    
    if echo "$CONTENT_TYPE" | grep -q "charset=utf-8"; then
        echo "✅ PASS ($CONTENT_TYPE)"
        PASS=$((PASS + 1))
    else
        echo "❌ FAIL ($CONTENT_TYPE)"
        FAIL=$((FAIL + 1))
    fi
    echo ""
}

echo "=========================================="
echo "📋 API接口测试"
echo "=========================================="
echo ""

# 1. 认证相关API
test_api "验证码接口" "http://localhost/prod-api/captchaImage"
test_api "登录接口" "http://localhost/prod-api/login" "POST" '{"username":"admin","password":"admin123"}'

# 2. 销售模块API
test_api "客户管理API" "http://localhost/prod-api/api/customer/page?current=1&size=10"
test_api "合同管理API" "http://localhost/prod-api/api/contract/page?current=1&size=10"
test_api "联系记录API" "http://localhost/prod-api/api/contactRecord/page?current=1&size=10"
test_api "工作日志API" "http://localhost/prod-api/api/workLog/page?current=1&size=10"
test_api "业绩记录API" "http://localhost/prod-api/api/performanceRecord/page?current=1&size=10"
test_api "客户转移API" "http://localhost/prod-api/api/customerTransferLog/page?current=1&size=10"

# 3. 财务模块API
test_api "贷款审核API" "http://localhost/prod-api/api/loanAudit/page?current=1&size=10"
test_api "佣金记录API" "http://localhost/prod-api/api/commissionRecord/page?current=1&size=10"
test_api "服务费记录API" "http://localhost/prod-api/api/serviceFeeRecord/page?current=1&size=10"
test_api "银行管理API" "http://localhost/prod-api/api/bank/page?current=1&size=10"
test_api "产品管理API" "http://localhost/prod-api/api/financeProduct/page?current=1&size=10"

echo "=========================================="
echo "📝 字符编码测试"
echo "=========================================="
echo ""

# 4. Content-Type测试
test_content_type "登录API字符编码" "http://localhost/prod-api/captchaImage"
test_content_type "销售API字符编码" "http://localhost/prod-api/api/customer/page?current=1&size=10"
test_content_type "财务API字符编码" "http://localhost/prod-api/api/loanAudit/page?current=1&size=10"

echo "=========================================="
echo "🌐 前端页面测试"
echo "=========================================="
echo ""

# 5. 前端页面测试
test_api "首页" "http://localhost/"
test_api "登录页面" "http://localhost/login"
test_api "客户管理页面" "http://localhost/sales/customer"
test_api "合同管理页面" "http://localhost/sales/contract"
test_api "贷款审核页面" "http://localhost/finance/loan-audit"
test_api "佣金记录页面" "http://localhost/finance/commission"

echo "=========================================="
echo "📊 测试总结"
echo "=========================================="
echo ""
echo "总测试数: $TOTAL"
echo "✅ 通过: $PASS"
echo "❌ 失败: $FAIL"
echo "通过率: $((PASS * 100 / TOTAL))%"
echo ""

if [ $FAIL -eq 0 ]; then
    echo "🎉 所有测试通过！系统运行正常！"
    exit 0
else
    echo "⚠️  存在失败的测试，请检查上述结果"
    exit 1
fi
