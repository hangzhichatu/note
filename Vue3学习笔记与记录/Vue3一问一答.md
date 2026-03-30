## vue3常见问题解答

* Q1  : Vue3比起Vue2的最大的架构变化是什么?
* A1 : 答案Vue3使用 `Proxy` 重写了整个响应式系统,取代了Vue2中基于 `Object.defineProperty` 的实现:
  * Vue3能监听整个对象,包括新增删除属性 ,数组索引,length等变化,而vue2只能拦截已存在的属性
  * Vue3 惰性代理,在访问嵌套对象的时候,只要访问时候才会代理子对象,而vue2需要递归遍历所有子属性,这导致vue2在初始化时候,开销更大.
  * 性能方面,vue2初始化慢,但是单次更新快,而vue3初始化快,但每次访问有微小开销
  * vue3在可以监听的对象方面比起vue2有更多选择,vue2只可以监听普通对象和数组,而vue3可以监听对象,数组,Map,Set,WeakMap,WeakSet等,换句话来说,vue3支持更多的数据结构
  * 其他方面的优化:
    * 1 模块化设计 Vue3将核心功能拆分为独立模块(`reactivity`,`compile-core`,`runtime-dom`等),便于Tree-shaking,减小打包体积
    * 2 组合式API ,解决了vue2在大型组件里面逻辑分散的问题:
      简单来说,就是vue2定义的时候 data和methods块,当内容变得很多的时候,就不能很方便的看某个数据相关的操作和定义,而组合式API你想怎么写就怎么写,最好都可以通过setup语法糖的方式统一抛出定义
    * 3 更好的TypeScript支持:
      TypeScript支持,是指在开发时有类型推导和检查能力(这块将会另起一篇关于Ts的)
    * 4 编译器优化**Vue 3 编译器能生成带** **PatchFlags** **的代码，运行时只 diff 动态节点，提升更新性能。**
* Q2   **`ref` **和** `reactive` **的区别是什么？分别适用于什么场景？****
* A2  核心区别是适用的对象和内部的响应式逻辑不同
  * 通用数据类型不一致 , `ref` 是基本类型(`String` , `number` , `boolean` 等) `reactive` 仅适用于对象.
  * 实现方式不一致, `ref` 是将值封装到 `{value:...}` 对象中,并适用 `Object.defineProperty` 或 `Proxy` 使其获得响应式,而 `reactive` 直接使用 `Proxy` 对整个对象进行深度响应式代理
  * 访问方式不一样, ` ref` 在 javaScript中通过    `.value` 方式读写,(在模板中自动解包,无需.vlaue)
  * 类型推导
* A2.1 (延展回答问题,关于最佳实践)
  * 声明的ref的最佳实践情况:
    * 声明基本类型的响应式变量
    * 需要在组合函数中返回一个可被多个组件共享的响应式引用
    * 想要利用模板自动解包的遍历性 模板中写 `{{count}}` 而不是 `{{count.value}}`
  * 声明reactive的最佳实践情况
    * 管理组件内部的状态对象(如表单数据 页面状态)
    * 避免大量 `.value` 写法提升可读性
* A2.2 (reactive注意事项)
  * `reactive` 返回的对象是深层响应式的(嵌套对象也会被代理)
  * `reactive` 不能直接解构,会失去响应性,如需解构应配合 `toRefs` :

    ```javascript
    const state = reactive({a:1,b:2})
    const {a,b} = toRefs(state)
    ```
  * `ref` 包装对象的时候,其 `.value` 指向的是该对象,而Vue会对这个对象自动转为reactive 即 (`ref({}) 约等于 ` `reactive({})`)
* Q3 为什么 `<script setup>` 中不需要 `return` 就能在模板中使用变量?
* A1 **核心** 回答 `<script setup>` 中的顶层变量和函数在 **编译阶段被自动提升注入到组件实例的setup()返回对象中，因此模板可以直接访问不需要手动return对外暴露**
* Q3.1 为什么普通的setup()需要return ？
* A3.1 因为Vue组件在渲染模板的时，会从 setup()的返回值里中提取属性作为模板的上下文即 (renderContext) 。如果不 return模板就看不见这些变量
* Q3.2 为什么 `<Script setup> `不需要return ？
* A3.2 这是因为它不是编译时候的特性，而是编译器提供的语法糖。 Vue的SFC编译器在构建阶段会做如下处理
  * 步骤1 ： 静态分析顶层作用域 会扫描 `<script setup> `中的所有顶层变量，函数，导入（函数内方法内定义的变量不在其中）
  * 步骤2： 自动生成 setup() 并且return这些绑定

    ```javascript
    <script>
    import { ref } from 'vue';
    export default {
      setup(__props, { expose }) {
        const count = ref(0);
        function inc() { count.value++ }

        // ⬇️ 编译器自动插入 return
        return { count, inc };
      }
    }
    </script>
    ```

    类似这样是就是上述生成的逻辑
* Q3.3 描述相关细节
* A3.3  一些细节如下：
  * 导入的组件/指令自动注册

    ```javascript
    <script setup>
    import MyButton from './MyButton.vue';
    // 自动注册为 <MyButton />，无需 components 选项
    </script>
    ```

    编译器会把导入的 `.vue` 或者带有 `isComponent` 标记的对象自动加入组件注册表
  * 支持defineProps/defineEmits 等编译时宏
  * 响应式解包在模板中依然有效
* Q3.4 深度思考延申
* A3.4
  1. `<script setup>`能访问 `this`吗？，不能 因为**编译后的 **`setup()` 是一个普通函数，且 Vue 3 的 `setup()` 本身就没有 `this` 上下文（设计上避免 Options API 的混乱
  2. 如何在script setup 中使用生命周期或者provide/indect?  直接导入即可它们是函数式的API不依赖 this
  3. 性能上有提升吗？ 有，减少手动return的样板代码 ，编译器可以做更激进的优化，更有利于Tree-Shaking
  4. 和React Hooks有什么异同？
     相似: 都强调函数式的逻辑组织、自定义Hook/Composable复用
     不同:vue的scrpt setup是编译时优化，React Hooks是运行时
* 3  怎么说？  ：

> “`<script setup>` 是 Vue 3 提供的编译时语法糖。SFC 编译器会静态分析其顶层作用域中的所有声明（包括变量、函数、组件导入），并在编译阶段自动生成一个 `setup()` 函数，将这些绑定自动 `return` 出去。因此模板可以直接访问，无需手动 return。这种方式不仅减少了样板代码，还让编译器能进行更多优化，比如自动注册组件、类型推导增强等。本质上，它是一种‘约定优于配置’的设计，通过编译转换实现开发体验和性能的双重提升。”

---

* Q4 父组件如何向子组件传递数据，子组件如何通知父组件事件除了 props/emit，Vue 3 还有哪些组件通信方式？（至少说出 3 种）
  provide / inject 适用于什么场景？它会破坏组件的“黑盒”原则吗？如何避免滥用？
* A4:
  父节点->子节点: 使用 `progs`

  * 父组件通过在子组件上标签上绑定属性(如:`<Child :msg="parentMsg" />`)传递数据
  * 子组件通过 `definProps` 在 `<Script setup> `中，或	`props` 选项，在(选项式API中)接收
  * 注意 `props`是单项数据流，子组件不能修改，否则会触发警告

  子节点->父节点 : 使用 `$emit` 触发自定义事件

  * 子组件通过 `defineEmits`声明要触发的事件， 比如 `const emit = defineEmits(['update'])`.然后调用 `emit('update',payload)`
  * 父组件监听该事件(如)
* A4.1 回答其他组件通信方式:

  * `provide`/`inject`
    祖先组件通过 `provide` 提供数据，子组件通过 `inject`注入使用
    用于处理跨越多层嵌套的组件间共享状态（比如主题和用户信息等）
    需要手动处理响应式
  * `$ refs` /模板引用（Template Refs）
    父组件通过refs 获取子组件实例 (`<Child ref="childRef" />`),直接调用访问其属性或者方法
    适用于需要命令式操作的子组件的场景（比如聚焦输入框、播放视频等）
    不适合用于传递数据，因为破坏了封装性
  * 全局状态管理 pinia或者vuex（实际工作推荐前者）
    使用store，组件通过store读写共享数据
    适用于复杂应用中多个不相关组件需要共享状态的场景
  * 事件总线，vue3已经移除了，但是可以通过第三方库实现
* A4.2 provide / inject 适用于什么场景？它会破坏组件的“黑盒”原则吗？如何避免浪费:

  * 适用场景:
    跨多级组件传递上下文信息，如:"主题信息"、语言包、用户认证状态、UI库的根配置
    避免 "prop drilling" (层层穿透prop)导致的代码冗余
  * 是否破话黑盒原则：
    是的，有一定风险，黑盒原则指组件应尽可能独立、不依赖外部上下文。而 `inject` 值得组件隐式的依赖祖先提供的数据，降低了复用性和可测试性。它无法脱离上下文单独测试和适用，这导致一些测试和bug不好进行和适用。
  * 如何避免滥用:
    * 1 仅用于高级上下文:如应用级配置、插件注入、而非业务数据。
    * 2 提供默认值，增强组件鲁棒性 `inject('key',defaultvalue)`,增强组件鲁棒性。
    * 3 配合TypeScript类型声明:明确inject的类型，可以提升可维护性
    * 4 **封装为组合式函数（Composables）****：将 provide/inject 逻辑抽象到 composable 中，隔离副作用。**
    * 5 优先考虑props/emit 或者pinia ：除非确实存在深层调用，否则不要轻易适用
* A4.3 总结:

  > **在 Vue 3 中，组件通信应遵循“就近原则”和“显式优于隐式”。**`props/emit` 是首选；跨层级时可谨慎使用 `provide/inject`；复杂状态交由 Pinia 管理；`ref` 用于命令式交互。合理选择通信方式，是构建可维护、可测试组件系统的关键。
  >
* Q5  Vue 3 中 onMounted 对应 Vue 2 中的哪个选项？
  5.1 `<Teleport>` 组件的作用是什么？举一个实际使用场景。
  5.2 `<Suspense>` 是用来做什么的？它通常和什么特性配合使用
* A5 在Vue2 中通过组件选项中定义 `mounted()`执行DOM元素加载完成后的逻辑，在Vue3中 通常在Setup中导入onMounted 静态方法，并且使用它。
  **本质相同** **：两者都在组件实例被挂载到 DOM 后触发，此时可以安全访问 **` $ el` 或原生 DOM 元素

@
