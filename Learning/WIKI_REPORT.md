# NeoCC 大富翁项目 - 技术学习Wiki Report

> 本文档专为新手程序员设计，详细讲解项目中使用的各项技术原理、目的和实际应用示例。
> 
> **阅读建议**：按顺序阅读，每个技术点都配有项目中的实际代码示例。

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [后端技术详解](#2-后端技术详解)
3. [前端技术详解](#3-前端技术详解)
4. [前后端交互](#4-前后端交互)
5. [学习问题](#5-学习问题)

---

## 1. 项目概述

### 1.1 项目背景

NeoCC（大富翁）是一个**金融贷款业务管理系统**，涵盖从客户获取、合同签署、贷款审核到佣金计算的完整业务流程。

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端层 (Frontend)                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Vue 3 + Element Plus + Pinia + Axios + Vite             │  │
│  │  端口: 3001                                               │  │
│  └───────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTP请求
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      网关层 (API Gateway)                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Spring Cloud Gateway (端口: 8086)                        │  │
│  │  功能: 路由转发、跨域处理、负载均衡                        │  │
│  └───────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
            ┌───────────────────┼───────────────────┐
            ▼                   ▼                   ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│  auth服务     │    │  sales服务    │    │  finance服务  │
│  端口: 8085   │    │  端口: 8083   │    │  端口: 8084   │
│  用户认证授权  │    │  销售管理     │    │  金融管理     │
└───────────────┘    └───────────────┘    └───────────────┘
            │                   │                   │
            ▼                   ▼                   ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│dafuweng_auth  │    │dafuweng_sales │    │dafuweng_finance│
│   (MySQL)     │    │   (MySQL)     │    │   (MySQL)     │
└───────────────┘    └───────────────┘    └───────────────┘
```

### 1.3 微服务模块说明

| 模块 | 端口 | 职责 | 数据库 |
|------|------|------|--------|
| **gateway** | 8086 | API网关，统一入口 | 无 |
| **auth** | 8085 | 用户认证、角色权限 | dafuweng_auth |
| **sales** | 8083 | 客户管理、合同管理 | dafuweng_sales |
| **finance** | 8084 | 贷款审核、银行管理 | dafuweng_finance |
| **system** | 8082 | 部门、战区、字典 | dafuweng_system |

---

## 2. 后端技术详解

### 2.1 Spring Boot - 基础框架

#### 原理
Spring Boot 是 Spring 框架的扩展，它通过**自动配置**和**约定优于配置**的理念，大大简化了 Spring 应用的搭建和开发过程。

#### 核心特性

| 特性 | 说明 |
|------|------|
| **自动配置** | 根据classpath中的依赖自动配置Spring应用 |
| **起步依赖** | 简化Maven配置，如`spring-boot-starter-web` |
| **内嵌服务器** | 内置Tomcat，无需部署WAR包 |
| **Actuator** | 提供生产级别的监控和管理功能 |

#### 项目示例

**启动类** (`sales/src/main/java/com/dafuweng/sales/SalesApplication.java`):
```java
@SpringBootApplication
@EnableDiscoveryClient  // 启用Nacos服务发现
@EnableFeignClients     // 启用OpenFeign
@MapperScan("com.dafuweng.sales.dao")  // MyBatis Mapper扫描
public class SalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
    }
}
```

**配置文件** (`application.yml`):
```yaml
server:
  port: 8083  # 服务端口

spring:
  application:
    name: sales  # 服务名称，注册到Nacos
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dafuweng_sales?...
    username: root
    password: 123456
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos地址
```

---

### 2.2 Spring Cloud Alibaba - 微服务架构

#### 原理
Spring Cloud Alibaba 提供了一整套微服务解决方案，包括服务注册发现、配置管理、服务调用等。

#### 核心组件

| 组件 | 功能 | 类比 |
|------|------|------|
| **Nacos** | 服务注册与发现、配置中心 | 类似电话簿 |
| **Gateway** | API网关，统一入口 | 类似小区大门 |
| **OpenFeign** | 声明式HTTP客户端 | 类似远程调用助手 |
| **RabbitMQ** | 消息队列，异步通信 | 类似快递驿站 |

#### 项目示例：服务注册与发现

**1. Nacos服务注册** (每个服务的pom.xml):
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

**2. Gateway路由配置** (`gateway/src/main/resources/application.yml`):
```yaml
spring:
  cloud:
    gateway:
      routes:
        # sales服务路由
        - id: sales-route
          uri: lb://sales  # lb: 负载均衡
          predicates:
            - Path=/sales/**  # 路径匹配
          filters:
            - StripPrefix=1   # 去掉前缀/sales
        
        # finance服务路由
        - id: finance-route
          uri: lb://finance
          predicates:
            - Path=/finance/**
          filters:
            - StripPrefix=1
```

**3. OpenFeign服务调用** (`finance模块调用sales服务`):
```java
// 1. 定义Feign客户端
@FeignClient(name = "sales")
public interface SalesClient {
    @GetMapping("/api/contract/{id}")
    Result<ContractVO> getContract(@PathVariable("id") Long id);
}

// 2. 使用Feign客户端
@Service
public class LoanAuditServiceImpl implements LoanAuditService {
    @Autowired
    private SalesClient salesClient;
    
    public void approve(Long contractId) {
        // 调用sales服务获取合同信息
        Result<ContractVO> result = salesClient.getContract(contractId);
        ContractVO contract = result.getData();
        // ... 业务逻辑
    }
}
```

---

### 2.3 MyBatis-Plus - ORM框架

#### 原理
MyBatis-Plus 是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

#### 核心特性

| 特性 | 说明 |
|------|------|
| **BaseMapper** | 内置通用CRUD方法，无需手写SQL |
| **代码生成器** | 自动生成Entity、Mapper、Service、Controller |
| **分页插件** | 物理分页，支持多种数据库 |
| **逻辑删除** | 自动处理逻辑删除字段 |
| **乐观锁** | 自动处理并发更新冲突 |

#### 项目示例

**1. Entity实体类** (`sales/src/main/java/com/dafuweng/sales/entity/CustomerEntity.java`):
```java
@Data
@TableName("customer")  // 指定表名
public class CustomerEntity implements Serializable {
    
    @TableId  // 主键
    private Long id;
    
    private String name;
    private String phone;
    private String idCard;
    
    // 客户类型: 1-个人, 2-企业
    private Integer customerType;
    
    // 意向等级: 1-低, 2-中, 3-高, 4-很有意向, 5-已签约
    private Integer intentionLevel;
    
    // 状态: 0-无效, 1-有效, 5-公海
    private Integer status;
    
    // 审计字段
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;
    
    @TableLogic  // 逻辑删除
    private Short deleted;
    
    @Version  // 乐观锁
    private Integer version;
}
```

**2. Mapper接口** (`sales/src/main/java/com/dafuweng/sales/dao/CustomerDao.java`):
```java
@Mapper
public interface CustomerDao extends BaseMapper<CustomerEntity> {
    // 继承BaseMapper后，自动拥有以下方法：
    // - insert(entity)      插入
    // - deleteById(id)      根据ID删除
    // - updateById(entity)  根据ID更新
    // - selectById(id)      根据ID查询
    // - selectList(wrapper) 条件查询
    
    // 自定义SQL方法
    List<CustomerEntity> selectPublicSeaList(@Param("days") int days);
}
```

**3. XML映射文件** (`sales/src/main/resources/sales/mapper/CustomerDao.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.dafuweng.sales.dao.CustomerDao">
    
    <!-- 自定义查询：公海客户列表 -->
    <select id="selectPublicSeaList" resultType="com.dafuweng.sales.entity.CustomerEntity">
        SELECT * FROM customer 
        WHERE status = 5 
        AND public_sea_time &lt;= DATE_SUB(NOW(), INTERVAL #{days} DAY)
        AND deleted = 0
    </select>
    
</mapper>
```

**4. Service层使用**:
```java
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerDao, CustomerEntity> 
    implements CustomerService {
    
    public PageResponse<CustomerEntity> pageQuery(PageRequest request) {
        // 1. 创建分页对象
        Page<CustomerEntity> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        // 2. 创建查询条件
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerEntity::getStatus, 1)  // 状态=有效
               .like(StringUtils.isNotBlank(request.getName()), 
                     CustomerEntity::getName, request.getName())
               .orderByDesc(CustomerEntity::getCreatedAt);
        
        // 3. 执行分页查询
        Page<CustomerEntity> result = this.page(page, wrapper);
        
        // 4. 返回结果
        return PageResponse.of(result.getRecords(), result.getTotal());
    }
}
```

---

### 2.4 RabbitMQ - 消息队列

#### 原理
消息队列（Message Queue）是一种**异步通信**机制，发送者将消息发送到队列，接收者从队列中获取消息，实现系统间的解耦。

#### 为什么要用消息队列？

想象一个场景：销售签署合同后，需要通知金融部门创建贷款审核记录。

**不用MQ（同步调用）**：
```
销售签署合同 → 调用金融API → 金融创建审核记录 → 返回结果 → 销售继续
     ↑______________________________________________|
     问题：金融服务挂了，销售签署也失败，强耦合！
```

**使用MQ（异步消息）**：
```
销售签署合同 → 发送MQ消息 → 立即返回成功
                          ↓
                    金融服务监听 → 创建审核记录
     
     好处：金融服务挂了不影响销售签署，解耦！
```

#### 项目示例：合同签署事件

**1. 定义事件对象** (`common/src/main/java/com/dafuweng/common/mq/event/ContractSignedEvent.java`):
```java
@Data
public class ContractSignedEvent implements Serializable {
    private Long contractId;      // 合同ID
    private Long customerId;      // 客户ID
    private Long salesRepId;      // 销售代表ID
    private Long deptId;          // 部门ID
    private BigDecimal contractAmount;  // 合同金额
    private Date signDate;        // 签署日期
}
```

**2. MQ配置** (`common/src/main/java/com/dafuweng/common/mq/MqConfig.java`):
```java
@Configuration
public class MqConfig {
    // 交换机名称
    public static final String EXCHANGE_SALES = "sales.exchange";
    // 路由键
    public static final String ROUTING_CONTRACT_SIGNED = "contract.signed";
    // 队列名称
    public static final String QUEUE_FINANCE_CONTRACT = "finance.contract.queue";
    
    @Bean
    public DirectExchange salesExchange() {
        return new DirectExchange(EXCHANGE_SALES);
    }
    
    @Bean
    public Queue financeContractQueue() {
        return new Queue(QUEUE_FINANCE_CONTRACT);
    }
    
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(financeContractQueue())
            .to(salesExchange())
            .with(ROUTING_CONTRACT_SIGNED);
    }
}
```

**3. 发送消息** (sales模块):
```java
@Service
public class ContractSignServiceImpl implements ContractSignService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    @Transactional
    public void sign(Long contractId) {
        // 1. 更新合同状态
        contract.setStatus((short) 2);
        contractDao.updateById(contract);
        
        // 2. 发送事件给金融部门
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(contract.getId());
        event.setCustomerId(contract.getCustomerId());
        // ... 设置其他字段
        
        rabbitTemplate.convertAndSend(
            MqConfig.EXCHANGE_SALES,
            MqConfig.ROUTING_CONTRACT_SIGNED,
            event
        );
    }
}
```

**4. 接收消息** (finance模块):
```java
@Component
@Slf4j
public class ContractSignedListener {
    @Autowired
    private LoanAuditService loanAuditService;
    
    @RabbitListener(queues = MqConfig.QUEUE_FINANCE_CONTRACT)
    public void handleContractSigned(ContractSignedEvent event) {
        log.info("收到合同签署事件: contractId={}", event.getContractId());
        
        // 创建贷款审核记录
        LoanAuditEntity audit = new LoanAuditEntity();
        audit.setContractId(event.getContractId());
        audit.setCustomerId(event.getCustomerId());
        audit.setStatus((short) 0);  // 待审核
        // ... 设置其他字段
        
        loanAuditService.save(audit);
    }
}
```

---

### 2.5 Spring Security - 安全框架

#### 原理
Spring Security 是一个强大的安全框架，提供**认证**（Authentication，验证你是谁）和**授权**（Authorization，验证你能做什么）功能。

#### 核心概念

| 概念 | 说明 |
|------|------|
| **Principal** | 主体，即当前用户 |
| **Authentication** | 认证信息，包含用户名、密码、权限等 |
| **GrantedAuthority** | 授予的权限 |
| **Filter Chain** | 过滤器链，处理安全相关请求 |

#### 项目示例：JWT认证流程

**1. 登录认证** (`auth模块`):
```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        // 1. 验证用户名密码
        SysUserEntity user = userService.getByUsername(dto.getUsername());
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        // 2. 查询用户权限
        List<String> permissions = permissionService.getPermissions(user.getId());
        
        // 3. 生成JWT Token
        String token = JwtUtil.createToken(user.getId(), user.getUsername(), permissions);
        
        // 4. 返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        return Result.success(vo);
    }
}
```

**2. JWT Token结构**:
```
Header.Payload.Signature

Header: {"alg":"HS256","typ":"JWT"}
Payload: {
    "userId": 1,
    "username": "admin",
    "permissions": ["user:add", "user:edit"],
    "exp": 1234567890  // 过期时间
}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

**3. Token验证过滤器**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 1. 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            // 2. 验证Token
            if (JwtUtil.validateToken(token)) {
                // 3. 解析用户信息
                Long userId = JwtUtil.getUserId(token);
                String username = JwtUtil.getUsername(token);
                List<String> permissions = JwtUtil.getPermissions(token);
                
                // 4. 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        username, null, 
                        permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    );
                
                // 5. 设置到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## 3. 前端技术详解

### 3.1 Vue 3 - 渐进式JavaScript框架

#### 原理
Vue 是一个用于构建用户界面的渐进式框架。Vue 3 引入了**Composition API**，提供了更灵活、更强大的组件逻辑复用方式。

#### Vue 2 vs Vue 3 对比

| 特性 | Vue 2 (Options API) | Vue 3 (Composition API) |
|------|---------------------|-------------------------|
| 代码组织 | 按选项(data, methods...) | 按功能逻辑组合 |
| 逻辑复用 | Mixins（有命名冲突问题） | Composables（更灵活） |
| 响应式 | Object.defineProperty | Proxy（性能更好） |
| TypeScript | 支持一般 | 支持更好 |

#### 项目示例：Vue 3 Composition API

**传统 Options API（Vue 2风格）**：
```vue
<script>
export default {
  data() {
    return {
      customerList: [],
      loading: false,
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  methods: {
    getList() {
      this.loading = true
      listCustomer(this.queryParams).then(res => {
        this.customerList = res.data.records
        this.loading = false
      })
    }
  },
  created() {
    this.getList()
  }
}
</script>
```

**Composition API（Vue 3风格，本项目使用）**：
```vue
<script setup>
// 1. 导入API
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { listCustomer } from "@/api/sales/customer"

// 2. 响应式数据
const customerList = ref([])  // ref用于基本类型和对象
const loading = ref(true)

const data = reactive({       // reactive用于对象
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined
  },
  rules: {
    name: [{ required: true, message: "不能为空", trigger: "blur" }]
  }
})

// 解构为ref（保持响应式）
const { queryParams, rules } = toRefs(data)

// 3. 计算属性
const totalPages = computed(() => {
  return Math.ceil(total.value / queryParams.value.pageSize)
})

// 4. 方法
function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(response => {
    customerList.value = response.data?.records || []
    loading.value = false
  })
}

// 5. 生命周期（直接调用）
getList()
</script>
```

#### 核心API详解

**1. ref() - 响应式引用**
```javascript
import { ref } from 'vue'

// 定义响应式数据
const count = ref(0)      // 基本类型
const user = ref({        // 对象
  name: '张三',
  age: 25
})

// 访问和修改
console.log(count.value)     // 读取: 0
console.log(user.value.name) // 读取: 张三
count.value++                // 修改: 1
user.value.age = 26          // 修改
```

**2. reactive() - 响应式对象**
```javascript
import { reactive } from 'vue'

// 定义响应式对象
const form = reactive({
  name: '',
  phone: '',
  status: 1
})

// 直接访问（不需要.value）
console.log(form.name)
form.name = '李四'

// 注意：不能解构，会失去响应式！
// const { name } = form  // ❌ 错误，name不再是响应式的

// 正确做法：使用toRefs
import { toRefs } from 'vue'
const { name, phone } = toRefs(form)  // ✅ 正确，保持响应式
```

**3. computed() - 计算属性**
```javascript
import { ref, computed } from 'vue'

const firstName = ref('张')
const lastName = ref('三')

// 计算属性（有缓存，依赖不变不重新计算）
const fullName = computed(() => {
  return firstName.value + lastName.value
})

// 可写计算属性
const fullName2 = computed({
  get: () => firstName.value + lastName.value,
  set: (val) => {
    [firstName.value, lastName.value] = val.split('')
  }
})
```

**4. watch() - 侦听器**
```javascript
import { ref, watch } from 'vue'

const searchText = ref('')

// 监听单个ref
watch(searchText, (newVal, oldVal) => {
  console.log('搜索文本变化:', oldVal, '->', newVal)
  // 执行搜索
  doSearch(newVal)
})

// 监听多个数据源
const pageNum = ref(1)
const pageSize = ref(10)

watch([pageNum, pageSize], ([newNum, newSize], [oldNum, oldSize]) => {
  console.log('分页变化，重新加载')
  getList()
})

// 立即执行和深度监听
const user = ref({ name: '张三', address: { city: '北京' } })

watch(user, (newVal) => {
  console.log('user变化:', newVal)
}, { 
  immediate: true,  // 立即执行一次
  deep: true        // 深度监听对象内部变化
})
```

---

### 3.2 Element Plus - UI组件库

#### 原理
Element Plus 是一套基于 Vue 3 的桌面端组件库，提供了丰富的组件来帮助开发者快速构建界面。

#### 常用组件示例

**1. 表单组件** (`views/sales/customer/index.vue`):
```vue
<template>
  <!-- 查询表单 -->
  <el-form :model="queryParams" ref="queryRef" :inline="true">
    <el-form-item label="客户名称" prop="name">
      <el-input 
        v-model="queryParams.name" 
        placeholder="请输入客户名称" 
        clearable 
        @keyup.enter="handleQuery" 
      />
    </el-form-item>
    
    <el-form-item label="客户类型" prop="customerType">
      <el-select v-model="queryParams.customerType" placeholder="请选择">
        <el-option label="个人" :value="1" />
        <el-option label="企业" :value="2" />
      </el-select>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      <el-button icon="Refresh" @click="resetQuery">重置</el-button>
    </el-form-item>
  </el-form>
</template>
```

**2. 表格组件**:
```vue
<template>
  <el-table v-loading="loading" :data="customerList">
    <!-- 普通列 -->
    <el-table-column label="ID" align="center" prop="id" width="80" />
    <el-table-column label="客户名称" align="center" prop="name" />
    
    <!-- 自定义列（使用作用域插槽） -->
    <el-table-column label="状态" align="center" prop="status">
      <template #default="scope">
        <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
          {{ scope.row.status === 1 ? '有效' : '无效' }}
        </el-tag>
      </template>
    </el-table-column>
    
    <!-- 操作列 -->
    <el-table-column label="操作" align="center">
      <template #default="scope">
        <el-button link type="primary" @click="handleUpdate(scope.row)">修改</el-button>
        <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
```

**3. 对话框组件**:
```vue
<template>
  <!-- 添加/修改对话框 -->
  <el-dialog :title="title" v-model="open" width="600px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入" />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
const open = ref(false)
const title = ref('')

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加客户'
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      addCustomer(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}
</script>
```

**4. 描述列表组件** (详情展示):
```vue
<template>
  <el-descriptions :column="2" border size="small">
    <el-descriptions-item label="客户名称">
      {{ detailForm.name || '-' }}
    </el-descriptions-item>
    <el-descriptions-item label="联系电话">
      {{ detailForm.phone || '-' }}
    </el-descriptions-item>
    <el-descriptions-item label="状态" :span="2">
      <el-tag :type="detailForm.status === 1 ? 'success' : 'info'">
        {{ statusText(detailForm.status) }}
      </el-tag>
    </el-descriptions-item>
  </el-descriptions>
</template>
```

---

### 3.3 Pinia - 状态管理

#### 原理
Pinia 是 Vue 官方推荐的状态管理库，相比 Vuex 更轻量、更直观，支持 TypeScript 更好。

#### 核心概念

| 概念 | 说明 | 类比 |
|------|------|------|
| **Store** | 状态存储容器 | 类似组件的data |
| **State** | 响应式数据 | 存储的数据 |
| **Getters** | 计算属性 | 派生状态 |
| **Actions** | 方法 | 修改状态的操作 |

#### 项目示例

**1. 定义Store** (`store/modules/user.js`):
```javascript
import { defineStore } from 'pinia'
import { login, getUserInfo } from '@/api/login'
import { setToken, removeToken } from '@/utils/auth'

const useUserStore = defineStore('user', {
  // State: 存储数据
  state: () => ({
    token: '',
    userId: null,
    username: '',
    roles: [],
    permissions: []
  }),
  
  // Getters: 计算属性
  getters: {
    isAdmin: (state) => state.roles.includes('ROLE_admin'),
    hasPermission: (state) => (perm) => state.permissions.includes(perm)
  },
  
  // Actions: 方法
  actions: {
    // 登录
    async login(loginForm) {
      const res = await login(loginForm)
      this.token = res.data.token
      setToken(res.data.token)
      return res
    },
    
    // 获取用户信息
    async getInfo() {
      const res = await getUserInfo()
      this.userId = res.data.userId
      this.username = res.data.username
      this.roles = res.data.roles
      this.permissions = res.data.permissions
      return res
    },
    
    // 退出登录
    async logOut() {
      this.token = ''
      this.roles = []
      this.permissions = []
      removeToken()
    }
  }
})

export default useUserStore
```

**2. 在组件中使用Store**:
```vue
<script setup>
import useUserStore from '@/store/modules/user'

// 获取store实例
const userStore = useUserStore()

// 访问state（自动响应式）
console.log(userStore.username)
console.log(userStore.roles)

// 使用getters
if (userStore.isAdmin) {
  console.log('是管理员')
}

// 调用actions
async function handleLogin() {
  await userStore.login({ username: 'admin', password: '123456' })
  await userStore.getInfo()
}

// 使用computed保持响应式
const isManager = computed(() => {
  return userStore.roles.some(r => ['ROLE_manager', 'ROLE_admin'].includes(r))
})
</script>
```

**3. 权限Store** (`store/modules/permission.js`):
```javascript
const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],           // 所有路由
    sidebarRouters: []    // 侧边栏路由
  }),
  
  actions: {
    // 生成动态路由
    async generateRoutes() {
      const res = await getRouters()  // 从后端获取菜单
      const routerData = res.data?.data || []
      
      // 转换为Vue Router路由
      const sidebarRoutes = filterAsyncRouter(routerData)
      this.sidebarRouters = sidebarRoutes
      
      return sidebarRoutes
    }
  }
})
```

---

### 3.4 Vue Router - 路由管理

#### 原理
Vue Router 是 Vue.js 的官方路由管理器，用于构建单页应用（SPA），实现页面跳转而不刷新整个页面。

#### 核心概念

| 概念 | 说明 |
|------|------|
| **Route** | 路由配置，定义路径和组件的映射 |
| **Router** | 路由器实例，管理所有路由 |
| **RouterView** | 路由出口，显示匹配的路由组件 |
| **RouterLink** | 路由链接，用于导航 |

#### 项目示例

**1. 路由配置** (`router/index.js`):
```javascript
import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout'

// 常量路由（所有用户都能访问）
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true  // 不在侧边栏显示
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用HTML5 History模式
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

export default router
```

**2. 动态路由加载**:
```javascript
// 从后端获取菜单数据
const routerData = [
  {
    name: 'Sales',
    path: '/sales',
    component: 'Layout',
    meta: { title: '销售管理', icon: 'shopping' },
    children: [
      {
        name: 'Customer',
        path: 'customer',
        component: 'sales/customer/index',
        meta: { title: '客户管理' }
      },
      {
        name: 'Contract',
        path: 'contract',
        component: 'sales/contract/index',
        meta: { title: '合同管理' }
      }
    ]
  }
]

// 转换为Vue Router配置
function filterAsyncRouter(asyncRouterMap) {
  return asyncRouterMap.filter(route => {
    if (route.component === 'Layout') {
      route.component = Layout
    } else {
      // 动态导入组件
      route.component = loadView(route.component)
    }
    
    if (route.children) {
      route.children = filterAsyncRouter(route.children)
    }
    return true
  })
}

// 使用import.meta.glob动态导入
const modules = import.meta.glob('./../../views/**/*.vue')

export const loadView = (view) => {
  return modules[`./../../views/${view}.vue`]
}
```

**3. 路由导航**:
```vue
<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()    // 获取当前路由信息
const router = useRouter()  // 获取路由器实例

// 编程式导航
function goToDetail(id) {
  router.push(`/sales/customer/detail/${id}`)
}

function goBack() {
  router.back()
}

// 带查询参数
function search() {
  router.push({
    path: '/sales/customer',
    query: { name: searchText.value }
  })
}

// 获取路由参数
const customerId = route.params.id
const searchName = route.query.name
</script>
```

---

### 3.5 Vite - 构建工具

#### 原理
Vite 是下一代前端构建工具，利用浏览器原生 ES 模块导入特性，实现**极速的冷启动**和**即时的热更新**。

#### Vite vs Webpack

| 特性 | Webpack | Vite |
|------|---------|------|
| 启动时间 | 慢（需要打包） | 快（原生ESM） |
| 热更新 | 重新编译 | 即时（HMR） |
| 配置复杂度 | 复杂 | 简单 |
| 生产构建 | 快 | 更快（Rollup） |

#### 项目配置

**vite.config.js**:
```javascript
import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

const baseUrl = 'http://localhost:8086' // Gateway地址

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  
  return {
    base: env.VITE_APP_ENV === 'production' ? '/' : '/',
    
    plugins: createVitePlugins(env, command === 'build'),
    
    resolve: {
      alias: {
        '~': path.resolve(__dirname, './'),
        '@': path.resolve(__dirname, './src')  // 路径别名
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    
    // 开发服务器配置
    server: {
      port: 3001,
      open: true,  // 自动打开浏览器
      proxy: {
        // 代理配置：将前端请求转发到后端
        '/dev-api': {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
        '/sales/api': {
          target: baseUrl,
          changeOrigin: true
        },
        '/finance/api': {
          target: baseUrl,
          changeOrigin: true
        }
      }
    },
    
    // 生产构建配置
    build: {
      outDir: 'dist',
      sourcemap: false,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
        }
      }
    }
  }
})
```

---

## 4. 前后端交互

### 4.1 Axios封装与请求流程

#### 原理
Axios 是一个基于 Promise 的 HTTP 客户端，用于浏览器和 Node.js。本项目对其进行了封装，统一处理请求拦截、响应拦截和错误处理。

#### 项目示例

**1. Axios封装** (`utils/request.js`):
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '',
  timeout: 10000  // 请求超时时间
})

// ========== 请求拦截器 ==========
service.interceptors.request.use(config => {
  // 1. 动态设置baseURL（根据URL前缀路由到不同微服务）
  config.baseURL = getBaseURL(config.url)
  
  // 2. 添加Token到请求头
  if (getToken()) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }
  
  // 3. GET请求参数序列化
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + new URLSearchParams(config.params).toString()
    config.params = {}
    config.url = url
  }
  
  return config
}, error => {
  return Promise.reject(error)
})

// ========== 响应拦截器 ==========
service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = res.data.message || res.data.msg || '未知错误'
  
  // 根据状态码处理
  if (code === 401) {
    // Token过期，重新登录
    ElMessageBox.confirm('登录状态已过期', '系统提示', {
      confirmButtonText: '重新登录',
      type: 'warning'
    }).then(() => {
      location.href = '/login'
    })
    return Promise.reject('登录过期')
  } else if (code === 500) {
    ElMessage({ message: msg, type: 'error' })
    return Promise.reject(new Error(msg))
  } else if (code === 200) {
    return Promise.resolve(res.data)
  }
}, error => {
  // 网络错误处理
  let message = error.message
  if (message === 'Network Error') {
    message = '后端接口连接异常'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时'
  }
  ElMessage({ message, type: 'error' })
  return Promise.reject(error)
})

// 根据URL前缀获取baseURL（微服务路由）
function getBaseURL(url) {
  if (url.startsWith('/auth/')) return ''
  if (url.startsWith('/sysUser') || url.startsWith('/sysRole')) {
    return '/auth/api'
  }
  if (url.startsWith('/sysDict') || url.startsWith('/sysParam')) {
    return '/system/api'
  }
  if (url.startsWith('/customer') || url.startsWith('/contract')) {
    return '/sales/api'
  }
  if (url.startsWith('/bank') || url.startsWith('/loanAudit')) {
    return '/finance/api'
  }
  return '/dev-api'
}

export default service
```

**2. API接口封装** (`api/sales/customer.js`):
```javascript
import request from '@/utils/request'

// 查询客户列表（分页）
export function listCustomer(query) {
  return request({
    url: '/customer/page',
    method: 'get',
    params: query  // GET请求参数
  })
}

// 查询客户详细
export function getCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'get'
  })
}

// 新增客户
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data: data  // POST请求体
  })
}

// 修改客户
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data: data
  })
}

// 删除客户
export function delCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'delete'
  })
}
```

**3. 在组件中使用**:
```vue
<script setup>
import { listCustomer, getCustomer, addCustomer } from "@/api/sales/customer"

// 查询列表
function getList() {
  loading.value = true
  listCustomer({
    pageNum: 1,
    pageSize: 10,
    name: searchName.value
  }).then(response => {
    // response已经是res.data（响应拦截器处理过）
    customerList.value = response.data?.records || []
    total.value = response.data?.total || 0
    loading.value = false
  })
}

// 新增
function submitForm() {
  addCustomer(form.value).then(() => {
    proxy.$modal.msgSuccess('新增成功')
    open.value = false
    getList()  // 刷新列表
  })
}
</script>
```

---

### 4.2 完整的CRUD流程示例

以**客户管理**为例，展示完整的增删改查流程：

```
┌─────────────────────────────────────────────────────────────────────┐
│                           客户管理CRUD流程                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐     1.点击新增      ┌──────────────┐             │
│  │   列表页面    │ ──────────────────> │   打开对话框   │             │
│  │  customer/   │                     │   open=true   │             │
│  │   index.vue  │ <────────────────── │               │             │
│  └──────┬───────┘     6.刷新列表      └──────┬───────┘             │
│         │                                     │                     │
│         │ 2.调用API                          │ 3.填写表单            │
│         │   listCustomer()                   │   form.value         │
│         │                                     │                     │
│         ▼                                     ▼                     │
│  ┌──────────────┐                      ┌──────────────┐             │
│  │   API层      │                      │   表单验证    │             │
│  │  customer.js │                      │  validate()  │             │
│  └──────┬───────┘                      └──────┬───────┘             │
│         │                                     │                     │
│         │ 3.发送HTTP请求                      │ 4.验证通过           │
│         │   GET /sales/api/customer/page     │                     │
│         │                                     ▼                     │
│         ▼                             ┌──────────────┐             │
│  ┌──────────────┐                     │  调用addCustomer │            │
│  │   后端服务    │                     └──────┬───────┘             │
│  │  sales:8083  │                            │                     │
│  └──────┬───────┘                            │ 5.发送POST请求        │
│         │                                    │   POST /customer      │
│         │ 4.返回JSON数据                      │                     │
│         │   {code:200, data:{records:[]}}    ▼                     │
│         │                            ┌──────────────┐              │
│         │                            │   后端处理    │              │
│         └───────────────────────────>│  保存到数据库  │              │
│                                      └──────────────┘              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. 学习问题

请尝试回答以下问题来检验你的学习成果：

### 基础问题

**Q1: Vue 3 的 ref 和 reactive 有什么区别？什么时候用 ref，什么时候用 reactive？**

<details>
<summary>点击查看答案</summary>

- **ref**: 用于基本类型（string, number, boolean）和对象，访问时需要 `.value`
- **reactive**: 只用于对象，访问时直接访问属性，不能解构（会失去响应式）
- **建议**: 基本类型用 ref，对象类型两者都可以，但 reactive 配合 toRefs 使用更方便

</details>

---

**Q2: 为什么项目要使用消息队列（RabbitMQ）？直接调用 API 不行吗？**

<details>
<summary>点击查看答案</summary>

使用MQ的好处：
1. **解耦**: 销售服务和金融服务独立，一个挂了不影响另一个
2. **异步**: 销售签署合同后立即返回，不用等待金融处理完成
3. **削峰**: 大量合同签署时，MQ可以缓冲，金融服务按能力消费
4. **可靠**: 消息持久化，即使服务重启也不会丢失

</details>

---

**Q3: 解释 Spring Cloud Gateway 的作用，为什么前端不直接请求各个微服务？**

<details>
<summary>点击查看答案</summary>

Gateway的作用：
1. **统一入口**: 前端只需要知道 Gateway 地址，不需要知道每个服务的地址
2. **路由转发**: 根据路径前缀将请求转发到对应服务（如 /sales/** → sales服务）
3. **跨域处理**: 统一处理 CORS，后端服务不需要单独配置
4. **负载均衡**: 配合 Nacos 实现服务的负载均衡
5. **安全**: 可以在网关层统一做认证、限流等

</details>

---

### 进阶问题

**Q4: 在 customer/index.vue 中，为什么要用 `toRefs(data)`？直接解构 `const { queryParams } = data` 有什么问题？**

<details>
<summary>点击查看答案</summary>

- `reactive` 返回的对象直接解构会失去响应式
- `toRefs` 可以将 `reactive` 对象的所有属性转换为 `ref`，保持响应式
- 错误写法：`const { queryParams } = data` → queryParams 不再是响应式的
- 正确写法：`const { queryParams } = toRefs(data)` → queryParams 是 ref，保持响应式

</details>

---

**Q5: MyBatis-Plus 的 BaseMapper 提供了哪些常用方法？什么情况下需要写 XML？**

<details>
<summary>点击查看答案</summary>

BaseMapper 提供的方法：
- `insert(entity)` - 插入
- `deleteById(id)` - 根据ID删除
- `updateById(entity)` - 根据ID更新
- `selectById(id)` - 根据ID查询
- `selectList(wrapper)` - 条件查询
- `selectPage(page, wrapper)` - 分页查询

需要写 XML 的情况：
- 复杂的多表关联查询
- 复杂的条件判断（如动态 WHERE）
- 需要使用数据库特定函数
- 批量操作优化

</details>

---

**Q6: 解释 JWT 认证流程，Token 过期了怎么办？**

<details>
<summary>点击查看答案</summary>

JWT 认证流程：
1. 用户登录，后端验证用户名密码
2. 验证通过，生成 JWT Token（包含用户ID、权限、过期时间）
3. 前端存储 Token（localStorage 或 cookie）
4. 后续请求携带 Token 在 Header 中
5. 后端验证 Token 签名和过期时间
6. 验证通过，返回请求数据

Token 过期处理：
- 后端返回 401 状态码
- 前端拦截 401，提示用户重新登录
- 或者使用 Refresh Token 机制（本项目暂未实现）

</details>

---

### 实战问题

**Q7: 如果要新增一个"产品管理"页面，需要修改哪些文件？**

<details>
<summary>点击查看答案</summary>

需要修改的文件：

1. **后端** (finance模块):
   - `entity/FinanceProductEntity.java` - 实体类
   - `dao/FinanceProductDao.java` - Mapper接口
   - `service/FinanceProductService.java` - Service接口
   - `service/impl/FinanceProductServiceImpl.java` - Service实现
   - `controller/FinanceProductController.java` - Controller
   - `resources/finance/mapper/FinanceProductDao.xml` - XML（如需要）

2. **前端**:
   - `api/finance/product.js` - API封装
   - `views/finance/product/index.vue` - 页面组件
   - 数据库插入菜单数据（system模块）

</details>

---

**Q8: 项目中出现了 JavaScript 大数字精度问题（如 contractId 被截断），是如何解决的？**

<details>
<summary>点击查看答案</summary>

问题原因：
- JavaScript 的 Number 类型最大安全整数是 `9007199254740991`
- 超过这个值会丢失精度

解决方案：
1. **后端**: 返回字符串类型的 ID（`idStr` 字段）
2. **后端**: 路径参数用 String 接收，方法内再转 Long
3. **前端**: 使用 `row.idStr` 或 `String(row.id)` 传递

代码示例：
```java
// 后端返回
vo.setIdStr(String.valueOf(entity.getId()));

// 后端接收
@PostMapping("/contract/{contractId}/confirm")
public Result<Void> confirm(@PathVariable String contractId) {
    Long id = Long.parseLong(contractId);
    // ...
}

// 前端使用
const contractId = row.idStr || String(row.id);
```

</details>

---

## 附录：常用命令

### 后端命令

```bash
# 编译整个项目
mvn clean install

# 运行单个服务
cd sales && mvn spring-boot:run

# 打包
mvn clean package
```

### 前端命令

```bash
# 进入前端目录
cd ruoyi-ui

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
```

---

> **文档版本**: 1.0  
> **最后更新**: 2026-04-22  
> **作者**: AI Assistant
