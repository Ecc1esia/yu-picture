# yu-picture

## 项目简介

`yu-picture-repro` 是一个基于 DDD（领域驱动设计）的多人协作图库项目，旨在为用户提供一个高效、安全的图片管理平台。项目采用
Java 开发，结合 Spring Boot、Vue.js 等技术栈，支持图片上传、下载、分类、权限管理等功能。

## 项目结构
```
yu-picture-repro/ 
├── yu-picture-backend/ # 后端服务模块 
│ ├── src/ 
│ │ ├── main/ 
│ │ │ ├── java/ # Java 源代码 
│ │ │ └── resources/ # 配置文件 
│ │ └── test/ # 测试代码 
│ └── pom.xml # Maven 配置文件 
├── yu-picture-ddd/ # DDD 领域驱动设计模块 
│ ├── src/ 
│ │ ├── main/ 
│ │ │ ├── java/ # Java 源代码 
│ │ │ └── resources/ # 配置文件 
│ │ └── test/ # 测试代码 
│ └── pom.xml # Maven 配置文件
├── yu-picture-web/ # 前端界面模块 
│ ├── src/ 
│ │ ├── components/ # Vue 组件 
│ │ ├── layouts/ # 布局组件 
│ │ ├── views/ # 页面视图 
│ │ ├── App.vue # 根组件 
│ │ ├── main.ts # 入口文件 
│ │ └── router.ts # 路由配置 
│ ├── index.html # HTML 模板 
│ ├── package.json # 依赖配置 
│ └── vite.config.ts # Vite 配置 
├── README.md # 项目说明文档 
└── .gitignore # Git 忽略文件

```

### 模块说明
1. **yu-picture-backend**: 后端服务模块，负责处理业务逻辑、数据存储和 API 接口。
2. **yu-picture-ddd**: 领域驱动设计模块，包含领域模型、服务和基础设施层。
3. **yu-picture-web**: 前端界面模块，使用 Vue.js 构建用户交互界面。

## 功能特性
- **图片上传与管理**: 支持多种格式图片上传，提供缩略图、分类、标签等功能。
- **权限控制**: 基于角色的权限管理，支持用户、管理员等不同角色。
- **协作功能**: 支持团队空间，允许多用户协作管理图片资源。
- **响应式设计**: 前端界面采用响应式布局，适配多种设备。

## 技术栈
- **后端**: Java, Spring Boot, MyBatis Plus, Redis, MySQL
- **前端**: Vue.js, Vite, TypeScript, Pinia, Vue Router
- **其他**: Docker, Maven, Hutool 工具库

## 快速开始
1. 克隆项目仓库：
```bash
 git clone https://github.com/ecc1esia/yu-picture-repro.git cd yu-picture-repro
```

2. 安装依赖：

- 后端：`mvn install`
- 前端：`npm install`

3. 启动服务：
    - 后端：`mvn spring-boot:run`
    - 前端：`npm run dev`

4. 访问应用：
   打开浏览器，访问 `http://localhost:5173`

## 项目进度

- **C端开发**: 前端界面已完成基础布局，后端开发已完成待验证功能
- **B端开发**: todo

## 贡献指南

欢迎提交 Pull Request 或 Issue，共同完善项目！
