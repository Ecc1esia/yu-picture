# yu-picture

## 项目简介

`yu-picture-repro` 是一个基于 DDD（领域驱动设计）的多人协作图库项目，旨在为用户提供一个高效、安全的图片管理平台。  
我们的愿景是打造一个集图片存储、管理和协作于一体的现代化解决方案，帮助用户轻松组织和分享图片资源。

## 功能特性
- **图片上传与管理**: 支持多种格式图片上传，提供缩略图、分类、标签等功能。
- **权限控制**: 基于角色的权限管理，支持用户、管理员等不同角色。
- **协作功能**: 支持团队空间，允许多用户协作管理图片资源。
- **响应式设计**: 前端界面采用响应式布局，适配多种设备。
- **图片编辑**: 提供基础的图片编辑功能，如裁剪、旋转、滤镜等。
- **版本回滚**: 支持图片版本管理，允许用户恢复到之前的版本。

## 技术栈
- **后端**: Java, Spring Boot, MyBatis Plus, Redis, MySQL
  - 使用 Spring Boot 构建 RESTful API，结合 MyBatis Plus 实现高效的数据库操作。
  - Redis 用于缓存高频访问数据，提升系统性能。
- **前端**: Vue.js, Vite, TypeScript, Pinia, Vue Router
  - Vue.js 和 Vite 提供高效的开发体验，TypeScript 确保代码质量。
  - Pinia 作为状态管理工具，Vue Router 负责路由管理。
- **其他**: Docker, Maven, Hutool 工具库
  - Docker 容器化部署，简化环境配置。
  - Maven 管理后端依赖，Hutool 提供便捷的工具类支持。

## 快速开始

1. 克隆项目仓库：
   ```bash
   git clone https://github.com/ecc1esia/yu-picture-repro.git
   cd yu-picture-repro
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
