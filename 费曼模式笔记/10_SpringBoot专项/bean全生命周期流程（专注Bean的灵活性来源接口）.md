## Bean生命周期的定义
* 整个流程可以清晰地划分为四个核心阶段
* 实例化(`Instantiation`)：在工厂里把零件组装成一辆“空壳车”。 这里对应的调用构造方法，分配内存空间，但是还没注入对象它的所有属性（字段）都是默认值（null、0、false）。  
* 属性赋值 (`Population`)：给空壳车装上引擎、座椅、方向盘等所有配件。 这里就注入对象了对于 @Autowired 或 @Value 注解的字段，Spring 会在这里进行依赖注入，将对应的 Bean 或配置值填充进去  
* **初始化 (`Initialization`)**：对车辆进行最后的调试、质检和磨合，确保它能正常上路。 这里内容很多 
      *  检查 Aware 接口，如果Bean实现了某些特殊的 `Aware` 接口 比如 `BeanNameAware` 或 `ApplicationContextAware`。如果实现了，Spring 就会调用相应的方法，把 Bean 自己的名字、或者 Spring 容器本身（ApplicationContext）注入给它。这让 Bean 能够感知到自己在 Spring 世界里的“身份”和“环境”。  


         
*  执行 `BeanPostProcessor` 的 `postProcessBeforeInitialization` 你可以理解为类似Bom对象的成熟的提升触发的前置操作 这是初始化前的最后一个“加工站”。`BeanPostProcessor` 是一个强大的扩展接口，它的 `postProcessBeforeInitialization` 方法会在这个时机被调用。你可以在这里对 Bean 进行任何自定义的修改Spring AOP 的代理对象，很多时候就是在这里被创建并替换掉原始 Bean 的。    
  
  
             
*  执行 `@PostConstruct` 注解的方法 这是最常用的初始化回调方法。当你的 Bean 上某个方法被 `@PostConstruct` 标记后，Spring 会确保它在 Bean 完成所有属性注入后、但在正式投入使用前执行。此时 Bean 的所有属性都已经被正确注入，是安全的。  
*   执行 `InitializingBean` 接口的 `afterPropertiesSet` 方法 这和 `@PostConstruct` 的作用类似，是 Spring 原生提供的一种初始化回调方式。如果一个 Bean 实现了 `InitializingBean` 接口，Spring 就会调用它的 `afterPropertiesSet` 方法。  **注意InitializingBean 现在已经不推荐使用了，它的作用和@PostConstruct类似但是侵入性太强，其次两者也不能同时使用，否则会出错**
*   执行自定义的 init-method 这是你在 XML 配置或通过 @Bean(initMethod = "...") 指定的初始化方法。它的执行顺序在 `@PostConstruct` 和 `afterPropertiesSet` 之后。  
*   执行 `BeanPostProcessor` 的 `postProcessAfterInitialization` 这是初始化阶段的最后一道工序。`BeanPostProcessor` 的 `postProcessAfterInitialization`  方法会被调用。同样可以对 Bean 进行加工。AOP 的代理对象也可能在这里被创建。经过这一步，Bean 就从一个“调试完毕的空壳车”，变成了一辆“可以正式交付的成品车”。它被放入一级缓存 (singletonObjects)，随时准备被使用。  
* 使用 (`In Use`)：车辆交付，随时待命
* 销毁 (`Destruction`)：车辆报废，回收资源。 如果 Bean 实现了 `DisposableBean` 接口，Spring 会调用其 `destroy()` 方法。如果 Bean 通过 @Bean(destroyMethod = "...") 或 XML 指定了销毁方法，Spring 也会调用它。  