# 仓库指南

## 项目结构与模块组织
- 核心代码位于 `src/main/java/com/kinghy/invoiceanalysis`。
- 分层包包括：`controller`、`service`、`service/impl`、`repository`、`entity/dto`、`entity/pojo`、`strategy`（含 `strategy/impl`、`strategy/util`）。
- 运行配置在 `src/main/resources/application.properties`。
- JSON 模板文件当前存放在 `src/main/java/com/kinghy/invoiceanalysis/config/templates`。
- 测试代码位于 `src/test/java/com/kinghy/invoiceanalysis`。
- 设计说明在 `docs/`；Maven 构建产物输出到 `target/`。

## 构建、测试与开发命令
- `mvn clean compile`：编译 Java 源码并校验依赖解析。
- `mvn test`：运行单元/集成测试（JUnit 5，来自 `spring-boot-starter-test`）。
- `mvn spring-boot:run`：使用当前 `application.properties` 启动本地服务。
- `mvn clean package`：打包生成可运行 JAR（位于 `target/`）。
- 本地运行示例：`mvn spring-boot:run -Dspring-boot.run.profiles=dev`。

## 代码风格与命名规范
- 使用 Java 8（`pom.xml` 中 `<java.version>1.8</java.version>`），4 空格缩进，UTF-8 编码。
- 遵循 Spring 常见命名：`*Controller`、`*Service`、`*Repository`、`*Strategy`。
- 类名使用 PascalCase；方法/字段使用 camelCase；常量使用 UPPER_SNAKE_CASE。
- 控制层保持精简，提取与业务逻辑放在 `service` 与 `strategy` 包中。
- 在合适场景优先使用项目已采用的 Lombok 注解（如 `@Slf4j`）。

## 测试指南
- 测试框架：Spring Boot Test + JUnit Jupiter。
- 测试目录结构需镜像主代码包结构，测试类命名为 `*Tests.java`。
- 新增提取规则或策略时，补充对应的 service/repository 行为测试。
- 提交 PR 前执行 `mvn test`，覆盖模板匹配与解析边界场景。

## 提交与 Pull Request 规范
- 现有提交多为简短信息（如 `初版完善`、`Initial commit`）。
- 建议使用简洁祈使句并带作用域前缀，例如：`service: improve regex extraction fallback`。
- 每次提交保持单一变更点；行为变化时在正文说明原因（why）。
- PR 应包含：变更摘要、影响包路径、测试结果（`mvn test` 输出）、接口行为变化的请求/响应示例。

## 版本迭代日志规范
- 每次代码修改或配置调整后，必须同步更新 `docs/` 目录下的版本迭代日志文件。
- 日志需至少记录：修改日期、修改人（或执行主体）、影响文件路径、变更内容摘要。
- 若本次修改未更新迭代日志，视为变更流程未完成。

## 安全与配置建议
- 不要提交生产环境凭据，数据库密钥应外置管理。
- 本地开发默认使用 `template.source=filesystem`；切换到 `database` 前确保数据库配置完整可用。

## Agent 专项说明
- 本仓库中，AI 回复应使用中文。
