## 任务：使用 TDD 方式为 Java 项目增加新功能

### 背景
我想要增加一个rbac权限控制，需要实现对于每个页面的权限控制

### 技术栈
- Java 17+
- JUnit 5
- Maven
- Spring Boot

---

## 执行步骤

### 第一步：编写测试（Test First）

请先编写测试代码，确保：
1. 测试类命名规范：`[功能名]Test.java`
2. 测试方法命名清晰：`should[期望行为]When[条件]`
3. 覆盖正常场景和边界场景
4. 使用 `@BeforeEach` 准备测试数据

```java
// 示例测试结构
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService();
    }
    
    @Test
    void shouldReturnUserWhenUserExists() {
        // Arrange
        String userId = "123";
        
        // Act
        User result = userService.getUserById(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // 测试异常场景
    }
}

```
### 第二步：调用 Code Review Skill
完成测试编写后，请调用 code-review skill 审查测试代码：

/use code-review

审查要点：

- 测试覆盖是否完整
- 断言是否合理
- 是否有遗漏的边界情况

### 第三步：实现功能代码
根据测试失败信息，实现功能代码：

1. 创建必要的类和方法
2. 确保编译通过
3. 运行测试验证

### 第四步：调用 Superpowers Skill
功能实现后，请调用 superpowers skill 进行质量检查：

/use superpowers

检查内容：

- 代码 linting（Checkstyle/PMD）
- 单元测试执行
- 代码质量分析
- 安全漏洞扫描

### 第五步：修复与优化
根据 code-review 和 superpowers 的反馈：

1. 修复发现的问题
2. 重构优化代码
3. 再次运行测试确保通过

输出要求
1. 测试代码：src/test/java/...
2. 实现代码：src/main/java/...
3. 测试结果：所有测试通过
4. 代码质量报告：superpowers 输出

请按顺序执行以上步骤，确保 TDD 流程完整。
