```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "path": "dashboard",
            "component": "Layout",
            "children": [],
            "meta": {
                "icon": "home",
                "title": "工作台"
            },
            "name": "DASHBOARD"
        },
        {
            "path": "system",
            "component": "Layout",
            "children": [
                {
                    "path": "user",
                    "component": "system/user/index",
                    "meta": {
                        "icon": "user",
                        "title": "用户管理"
                    },
                    "name": "SYSTEM_USER"
                },
                {
                    "path": "role",
                    "component": "system/role/index",
                    "meta": {
                        "icon": "team",
                        "title": "角色管理"
                    },
                    "name": "SYSTEM_ROLE"
                },
                {
                    "path": "permission",
                    "component": "system/permission/index",
                    "meta": {
                        "icon": "lock",
                        "title": "权限管理"
                    },
                    "name": "SYSTEM_PERMISSION"
                },
                {
                    "path": "dept",
                    "component": "system/dept/index",
                    "meta": {
                        "icon": "apartment",
                        "title": "部门管理"
                    },
                    "name": "SYSTEM_DEPT"
                },
                {
                    "path": "zone",
                    "component": "system/zone/index",
                    "meta": {
                        "icon": "global",
                        "title": "战区管理"
                    },
                    "name": "SYSTEM_ZONE"
                },
                {
                    "path": "dict",
                    "component": "system/dict/index",
                    "meta": {
                        "icon": "book",
                        "title": "数据字典"
                    },
                    "name": "SYSTEM_DICT"
                },
                {
                    "path": "param",
                    "component": "system/param/index",
                    "meta": {
                        "icon": "config",
                        "title": "系统参数"
                    },
                    "name": "SYSTEM_PARAM"
                },
                {
                    "path": "log",
                    "component": "system/log/index",
                    "meta": {
                        "icon": "log",
                        "title": "操作日志"
                    },
                    "name": "SYSTEM_LOG"
                }
            ],
            "meta": {
                "icon": "setting",
                "title": "系统管理"
            },
            "name": "SYSTEM"
        },
        {
            "path": "sales",
            "component": "Layout",
            "children": [
                {
                    "path": "customer-list",
                    "component": "sales/customer/index",
                    "meta": {
                        "icon": "people",
                        "title": "客户列表"
                    },
                    "name": "SALES_CUSTOMER_LIST"
                },
                {
                    "path": "customer-add",
                    "component": "sales/customer/index",
                    "meta": {
                        "icon": "",
                        "title": "新增客户"
                    },
                    "name": "SALES_CUSTOMER_ADD"
                },
                {
                    "path": "customer-edit",
                    "component": "sales/customer/index",
                    "meta": {
                        "icon": "",
                        "title": "编辑客户"
                    },
                    "name": "SALES_CUSTOMER_EDIT"
                },
                {
                    "path": "customer-view",
                    "component": "sales/customer-view/index",
                    "meta": {
                        "icon": "",
                        "title": "客户详情"
                    },
                    "name": "SALES_CUSTOMER_VIEW"
                },
                {
                    "path": "public-sea",
                    "component": "sales/public-sea/index",
                    "meta": {
                        "icon": "cloud",
                        "title": "公海客户"
                    },
                    "name": "SALES_PUBLIC_SEA"
                },
                {
                    "path": "contact",
                    "component": "sales/contact/index",
                    "meta": {
                        "icon": "message",
                        "title": "跟进记录"
                    },
                    "name": "SALES_CONTACT"
                },
                {
                    "path": "contract-list",
                    "component": "sales/contract/index",
                    "meta": {
                        "icon": "file-text",
                        "title": "合同列表"
                    },
                    "name": "SALES_CONTRACT_LIST"
                },
                {
                    "path": "contract-add",
                    "component": "sales/contract/index",
                    "meta": {
                        "icon": "",
                        "title": "新增合同"
                    },
                    "name": "SALES_CONTRACT_ADD"
                },
                {
                    "path": "contract-edit",
                    "component": "sales/contract/index",
                    "meta": {
                        "icon": "",
                        "title": "编辑合同"
                    },
                    "name": "SALES_CONTRACT_EDIT"
                },
                {
                    "path": "contract-view",
                    "component": "sales/contract-view/index",
                    "meta": {
                        "icon": "",
                        "title": "合同详情"
                    },
                    "name": "SALES_CONTRACT_VIEW"
                },
                {
                    "path": "contract-sign",
                    "component": "sales/contract-sign/index",
                    "meta": {
                        "icon": "",
                        "title": "合同签署"
                    },
                    "name": "SALES_CONTRACT_SIGN"
                },
                {
                    "path": "worklog",
                    "component": "sales/worklog/index",
                    "meta": {
                        "icon": "edit",
                        "title": "工作日志"
                    },
                    "name": "SALES_WORKLOG"
                },
                {
                    "path": "transfer",
                    "component": "sales/transfer/index",
                    "meta": {
                        "icon": "swap",
                        "title": "客户转移记录"
                    },
                    "name": "SALES_TRANSFER"
                }
            ],
            "meta": {
                "icon": "chart",
                "title": "销售管理"
            },
            "name": "SALES"
        },
        {
            "path": "finance",
            "component": "Layout",
            "children": [
                {
                    "path": "bank",
                    "component": "finance/bank/index",
                    "meta": {
                        "icon": "bank",
                        "title": "银行管理"
                    },
                    "name": "FINANCE_BANK"
                },
                {
                    "path": "product",
                    "component": "finance/product/index",
                    "meta": {
                        "icon": "product",
                        "title": "金融产品"
                    },
                    "name": "FINANCE_PRODUCT"
                },
                {
                    "path": "loan-audit",
                    "component": "finance/loan-audit/index",
                    "meta": {
                        "icon": "audit",
                        "title": "贷款审核"
                    },
                    "name": "FINANCE_LOAN_AUDIT"
                },
                {
                    "path": "commission",
                    "component": "finance/commission/index",
                    "meta": {
                        "icon": "gift",
                        "title": "提成记录"
                    },
                    "name": "FINANCE_COMMISSION"
                },
                {
                    "path": "service-fee",
                    "component": "finance/service-fee/index",
                    "meta": {
                        "icon": "money",
                        "title": "服务费记录"
                    },
                    "name": "FINANCE_SERVICE_FEE"
                }
            ],
            "meta": {
                "icon": "banknote",
                "title": "金融审核"
            },
            "name": "FINANCE"
        },
        {
            "path": "performance",
            "component": "Layout",
            "children": [
                {
                    "path": "perf-summary",
                    "component": "perf-summary/index",
                    "meta": {
                        "icon": "",
                        "title": "业绩汇总"
                    },
                    "name": "PERF_SUMMARY"
                },
                {
                    "path": "perf-ranking",
                    "component": "perf-ranking/index",
                    "meta": {
                        "icon": "",
                        "title": "业绩排名"
                    },
                    "name": "PERF_RANKING"
                }
            ],
            "meta": {
                "icon": "bar-chart",
                "title": "业绩统计"
            },
            "name": "PERFORMANCE"
        }
    ]
}
```