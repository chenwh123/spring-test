
```java
/**
 * 事务管理器
 * 实例获取方式： 通过 @Autowired 注入
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {

    /**
     * 用于获取connection , 一个connection同一时间只能打开一个会话
     */
    @Nullable
    private DataSource dataSource;
}
```