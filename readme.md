```java
@Configuration
@ComponentScan(value = "com",
        //不包含 excludeFilters
        //只包含 includeFilters
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                //自定义指定类型(子类,其实现类都可以)
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Student.class)})
```



# 扫描包的方式

1. 包扫描+注解方式:  例如 @Service . @Resposity . @Controller 

2. @Bean :在@Configuration中 的bean

3. @Import : 

   1. @Import : ![image-20201209144015430](image-20201209144015430.png)

   2. @Import() 实现ImportSelector的类

      

      ![image-20201209144729059](image-20201209144729059.png)

      测试:

      <img src="image-20201209144710744.png" alt="image-20201209144710744" style="zoom:50%;" />

      

   3. 实现ImportBeanDefinitionRegistrar 

      ![image-20201209150051637](image-20201209150051637.png)

4. FactoryBean : 实现FactoryBean  把MyImport3 注册到bean中 或者@Component

   <img src="image-20201209152115669.png" alt="image-20201209152115669" style="zoom:50%;" />

   <img src="image-20201209152051804.png" alt="image-20201209152051804" style="zoom:50%;" />

   ​		MyImport3 的类型其实是Address1 如果要想获取本身 如下:

   ```java
   Object myImport3 = context.getBean("&myImport3");
   ```

   

# Bean的生命周期

在xml文件中:

```xml
init-method="初始化执行的方法" destroy-method="销毁执行的方法"    
```

- ① 实例化 Bean：对于 BeanFactory 容器，当客户向容器请求一个尚未初始化的 Bean 时，或初始化 Bean 的时候需要注入另一个尚未初始化的依赖时，容器就会调用 createBean 进行实例化。对于 ApplicationContext 容器，当容器启动结束后，通过获取 BeanDefinition 对象中的信息，实例化所有的 Bean；

- ② 设置对象属性（依赖注入）：实例化后的对象被封装在 BeanWrapper 对象中，紧接着 Spring 根据 BeanDefinition 中的信息以及通过 BeanWrapper 提供的设置属性的接口完成依赖注入；

- ③ 处理 Aware 接口：Spring 会检测该对象是否实现了 xxxAware 接口，并将相关的 xxxAware 实例注入给 Bean：

  如果这个 Bean 已经实现了 BeanNameAware 接口，会调用它实现的 setBeanName(String BeanId) 方法，此处传递的就是 Spring 配置文件中 Bean 的 id 值；

  如果这个 Bean 已经实现了 BeanFactoryAware 接口，会调用它实现的 setBeanFactory() 方法，传递的是 Spring 工厂自身；

  如果这个 Bean 已经实现了 ApplicationContextAware 接口，会调用 setApplicationContext(ApplicationContext) 方法，传入 Spring 上下文；

- ④ BeanPostProcessor：如果想对 Bean 进行一些自定义的处理，那么可以让 Bean 实现了 BeanPostProcessor 接口，那将会调用 postProcessBeforeInitialization(Object obj, String s) 方法；

- ⑤ InitializingBean 与 init-method：如果 Bean 在 Spring 配置文件中配置了 init-method 属性，则会自动调用其配置的初始化方法；

- ⑥ 如果这个 Bean 实现了 BeanPostProcessor 接口，将会调用 postProcessAfterInitialization(Object obj, String s) 方法；由于这个方法是在 Bean 初始化结束时调用的，因而可以被应用于内存或缓存技术；

以上几个步骤完成后，Bean 就已经被正确创建了，之后就可以使用这个 Bean 了。

- ⑦ DisposableBean：当 Bean 不再需要时，会经过清理阶段，如果 Bean 实现了 DisposableBean 这个接口，会调用其实现的 destroy() 方法；
- ⑧ destroy-method：最后，如果这个 Bean 的 Spring 配置中配置了 destroy-method 属性，会自动调用其配置的销毁方法。



```
/**
 * bean的生命周期：
 * 		bean创建---初始化----销毁的过程
 * 容器管理bean的生命周期；
 * 我们可以自定义初始化和销毁方法；容器在bean进行到当前生命周期的时候来调用我们自定义的初始化和销毁方法
 * 
 * 构造（对象创建）
 * 		单实例：在容器启动的时候创建对象
 * 		多实例：在每次获取的时候创建对象\
 * 
 * BeanPostProcessor.postProcessBeforeInitialization
 * 初始化：
 * 		对象创建完成，并赋值好，调用初始化方法。。。
 * BeanPostProcessor.postProcessAfterInitialization
 * 销毁：
 * 		单实例：容器关闭的时候
 * 		多实例：容器不会管理这个bean；容器不会调用销毁方法；
 * 
```

## BeanPostProcessor后置处理器

### ==bean实例化过程==

1. 实例化bean
2. 属性赋值
3. 寻找依赖关系
4. 调用BeanPostProcessor后置处理器中前置初始化方法
5. 如果实现了InitializingBean 就调用afterPropertiesSet
6. 初始化
7. 调用BeanPostProcessor后置处理器中后置初始化方法
8. 创建完成



```java
@Component
public class MyBeanPostProcess implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(beanName+"---------before");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(beanName+"----------after");
        return bean;
    }
}
```



==踩坑:Component+ComponentScan扫描====:在Component注解后扫描才能加入容器中==

==也就是说可以有两种方法像容器中注册组件==

1. @Component + @ConmponentScan
2. 不加注解,直接在Configuration中注册@Bean

```java
@ComponentScan("com")
@Configuration
public class BeanLifeConfig {

}
```

源码:

```java
if (mbd == null || !mbd.isSynthetic()) {
		   //前置方法
            wrappedBean = this.applyBeanPostProcessorsBeforeInitialization(bean, beanName);
        }

        try {
        //初始化
            this.invokeInitMethods(beanName, wrappedBean, mbd);
        } catch (Throwable var6) {
            throw new BeanCreationException(mbd != null ? mbd.getResourceDescription() : null, beanName, "Invocation of init method failed", var6);
        }

        if (mbd == null || !mbd.isSynthetic()) {
        //后置方法
            wrappedBean = this.applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
```

在执行这一段代码前有一个

```java
            this.populateBean(beanName, mbd, instanceWrapper);
```

给bean进行属性赋值

# 属性赋值

1. 从外部加载文件到pojo中

   <img src="image-20201209170317276.png" alt="image-20201209170317276" style="zoom:50%;" />  

# 自动装配

==同时注入IOC中两个相同类型的 bean @Autowired 会先检查类型,检测到相同的类型的几个bean,再自动按照名字进行注入,可以使用@Qualifier 指定bean 的名字注入==

   @Primary :默认使用哪个Bean





@Autowired用法:

1. 标注在成员变量上,从容器中获取值
2. 标注在构造函数 方法上时 方法里的形参就是ioc容器中的值

自定义组件想要使用Spring底层组件(ApplicationContext , BeanFactory....) 需要实现xxxAware:在创建对象的时候会自动调用回掉函数(接口规定的需要实现的方法)

动态切换数据源

![image-20201210111036053](image-20201210111036053.png)

![image-20201210111140509](image-20201210111140509.png)

## Conditional 条件判断 

实现此接口

<img src="image-20201210121456579.png" alt="image-20201210121456579" style="zoom:50%;" />

![image-20201210121525524](image-20201210121525524.png)

如果有dao2 的组件就加载T3





# AOP





==为了不让IOC中的代码生效,这里采用的@Bean方式,只有注入Configuration文件中才会生效==

```java
@EnableAspectJAutoProxy//开启aspectj功能,相当于xml文件中的开启功能
@Configuration 
public class AOPConfig {
    @Bean //采用这种方法注入bean
    public Cat cat(){
        return new Cat("xgw");
    }
    @Bean
    public AOPTest aopTest(){ //将aspectj组件注入ioc容器
        return new AOPTest();
    }
}
```



```java
@Aspect //声明这是aspectj文件
public class AOPTest {
//    @Pointcut(value = "execution(public void jiao())")
//    @Pointcut(value = "execution(public *  com.bean.*.*(..))")
    @Pointcut(value = "execution( *  com.bean.*.*(..))")
    public void t1(){}
    @Before("t1()")
    public void t2(){
        System.out.println("ASJ------before");
    }
    @After("t1()")
    public void t3(){
        System.out.println("ASJ------after");
    }
}
```

==只有从ioc容器中获取的bean执行方法时才会有AOP横切,自己new对象时并不会有效果==



## 源码分析

```java
//最终来到这个方法
private static BeanDefinition registerOrEscalateApcAsRequired(
			Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {
//cls =AnnotationAwareAspectJAutoProxyCreator
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
            //AUTO_PROXY_CREATOR_BEAN_NAME=internalAutoProxyCreator=上面的cls
            //如果有就做一系列工作,第一次没有
			BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
				int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				if (currentPriority < requiredPriority) {
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}
		//没有就执行下面
		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    	//在这就创建了AnnotationAwareAspectJAutoProxyCreator 名字就是internalAutoProxyCreator
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;
    	//只是定义信息,并没有创建
	}
```

以上过程就是定义AnnotationAwareAspectJAutoProxyCreator信息过程 ,AnnotationAwareAspectJAutoProxyCreator实现了BeanPostProcessor,和BeanFactoryAware接口,具有他们的特点

## 流程:

```java
//--------------------创建容器
AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext(AOPConfig.class);
//--------------------刷新容器
refresh();
//---------------------注册beanpostprocess
registerBeanPostProcessors(beanFactory);拦截bean的创建,注册BeanPostProcessors后置处理器
    1.先获取已经定义了的所需要创建的BeanPostProcessors后置处理器
    String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
	2.同时加了一些
beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
	3.优先注册实现了PriorityOrdered接口的BeanPostProcessor；
 	4.再给容器中注册实现了Ordered接口的BeanPostProcessor；
  	5.注册没实现优先级接口的BeanPostProcessor；
	6.注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，保存在容器中；
		创建internalAutoProxyCreator的BeanPostProcessor【AnnotationAwareAspectJAutoProxyCreator】
 			1）、创建Bean的实例
			2）、populateBean；给bean的各种属性赋值
 			3）、initializeBean：初始化bean；
					1）、invokeAwareMethods()：处理Aware接口的方法回调
					2）、applyBeanPostProcessorsBeforeInitialization()：应用后置处理器的postProcessBeforeInitialization（）
					3）、invokeInitMethods()；执行自定义的初始化方法
					4）、applyBeanPostProcessorsAfterInitialization()；执行后置处理器的postProcessAfterInitialization（）；
			
	7.把BeanPostProcessor注册到BeanFactory中；
 				beanFactory.addBeanPostProcessor(postProcessor);
 
//---------------------完成BeanFactory初始化工作
finishBeanFactoryInitialization(beanFactory);//初始化剩余的bean
	1. getBean(beanName);		for (String beanName : beanNames) //在此循环遍历剩余的所有bean,创建对象
		getbean()-->dogetbean()-->getSingleton()-->创建bean
	2. 在创建bean之前,先从缓存中获取是否有这个bean,如果有直接返回,没有再去创建
		1. 在创建之前,后置处理器先尝试返回对象,会有一个拦截,这个拦截器类型和第三步不同
          AnnotationAwareAspectJAutoProxyCreator在所有bean创建之前会有一个拦截，			        				InstantiationAwareBeanPostProcessor，会调用postProcessBeforeInstantiation()
		-----
        	Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
//===============================注意!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!==============================
        	此处的BeanPostProcessors和上面第三步为例的相同,比较特殊
        第三步其他的beanpostprocessor:     【BeanPostProcessor是在Bean对象创建完成初始化前后调用的】:
	    这一步:	【InstantiationAwareBeanPostProcessor是在创建Bean实例之前先尝试用后置处理器返回对象的】
             AnnotationAwareAspectJAutoProxyCreator就是这个类型的,所以有了1. 中的拦截
//===============================注意!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!==============================

```

```
 AnnotationAwareAspectJAutoProxyCreator【InstantiationAwareBeanPostProcessor】	的作用：
 * 1）、每一个bean创建之前，调用postProcessBeforeInstantiation()；
 * 		关心MathCalculator和LogAspect的创建
 * 		1）、判断当前bean是否在advisedBeans中（保存了所有需要增强bean）
 * 		2）、判断当前bean是否是基础类型的Advice、Pointcut、Advisor、AopInfrastructureBean，
 * 			或者是否是切面（@Aspect）
 * 		3）、是否需要跳过
 * 			1）、获取候选的增强器（切面里面的通知方法）【List<Advisor> candidateAdvisors】
 * 				每一个封装的通知方法的增强器是 InstantiationModelAwarePointcutAdvisor；
 * 				判断每一个增强器是否是 AspectJPointcutAdvisor 类型的；返回true
 * 			2）、永远返回false
 * 
 * 2）、创建对象
 * postProcessAfterInitialization；
 * 		return wrapIfNecessary(bean, beanName, cacheKey);//包装如果需要的情况下
 * 		1）、获取当前bean的所有增强器（通知方法）  Object[]  specificInterceptors
 * 			1、找到候选的所有的增强器（找哪些通知方法是需要切入当前bean方法的）
 * 			2、获取到能在bean使用的增强器。
 * 			3、给增强器排序
 * 		2）、保存当前bean在advisedBeans中；
 * 		3）、如果当前bean需要增强，创建当前bean的代理对象；
 * 			1）、获取所有增强器（通知方法）
 * 			2）、保存到proxyFactory
 * 			3）、创建代理对象：Spring自动决定
 * 				JdkDynamicAopProxy(config);jdk动态代理；
 * 				ObjenesisCglibAopProxy(config);cglib的动态代理；
 * 		4）、给容器中返回当前组件使用cglib增强了的代理对象；
 * 		5）、以后容器中获取到的就是这个组件的代理对象，执行目标方法的时候，代理对象就会执行通知方法的流程；
 * 		
 * 	
 * 	3）、目标方法执行	；
 * 		容器中保存了组件的代理对象（cglib增强后的对象），这个对象里面保存了详细信息（比如增强器，目标对象，xxx）；
 * 		1）、CglibAopProxy.intercept();拦截目标方法的执行
 * 		2）、根据ProxyFactory对象获取将要执行的目标方法拦截器链；
 * 			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
 * 			1）、List<Object> interceptorList保存所有拦截器 5
 * 				一个默认的ExposeInvocationInterceptor 和 4个增强器；
 * 			2）、遍历所有的增强器，将其转为Interceptor；
 * 				registry.getInterceptors(advisor);
 * 			3）、将增强器转为List<MethodInterceptor>；
 * 				如果是MethodInterceptor，直接加入到集合中
 * 				如果不是，使用AdvisorAdapter将增强器转为MethodInterceptor；
 * 				转换完成返回MethodInterceptor数组；
 * 
 * 		3）、如果没有拦截器链，直接执行目标方法;
 * 			拦截器链（每一个通知方法又被包装为方法拦截器，利用MethodInterceptor机制）
 * 		4）、如果有拦截器链，把需要执行的目标对象，目标方法，
 * 			拦截器链等信息传入创建一个 CglibMethodInvocation 对象，
 * 			并调用 Object retVal =  mi.proceed();
 * 		5）、拦截器链的触发过程;
 * 			1)、如果没有拦截器执行执行目标方法，或者拦截器的索引和拦截器数组-1大小一样（指定到了最后一个拦截器）执行目标方法；
 * 			2)、链式获取每一个拦截器，拦截器执行invoke方法，每一个拦截器等待下一个拦截器执行完成返回以后再来执行；
 * 				拦截器链的机制，保证通知方法与目标方法的执行顺序；
 * 		
 * 	总结：
 * 		1）、  @EnableAspectJAutoProxy 开启AOP功能
 * 		2）、 @EnableAspectJAutoProxy 会给容器中注册一个组件 AnnotationAwareAspectJAutoProxyCreator
 * 		3）、AnnotationAwareAspectJAutoProxyCreator是一个后置处理器；
 * 		4）、容器的创建流程：
 * 			1）、registerBeanPostProcessors（）注册后置处理器；创建AnnotationAwareAspectJAutoProxyCreator对象
 * 			2）、finishBeanFactoryInitialization（）初始化剩下的单实例bean
 * 				1）、创建业务逻辑组件和切面组件
 * 				2）、AnnotationAwareAspectJAutoProxyCreator拦截组件的创建过程
 * 				3）、组件创建完之后，判断组件是否需要增强
 * 					是：切面的通知方法，包装成增强器（Advisor）;给业务逻辑组件创建一个代理对象（cglib）；
 * 		5）、执行目标方法：
 * 			1）、代理对象执行目标方法
 * 			2）、CglibAopProxy.intercept()；
 * 				1）、得到目标方法的拦截器链（增强器包装成拦截器MethodInterceptor）
 * 				2）、利用拦截器的链式机制，依次进入每一个拦截器进行执行；
 * 				3）、效果：
 * 					正常执行：前置通知-》目标方法-》后置通知-》返回通知
 * 					出现异常：前置通知-》目标方法-》后置通知-》异常通知
 * 		
 * 
 * 
```



# 动态代理模式

## JDK动态代理和CGLIB动态代理

jdk:实现了接口,创建接口实现类的代理对象,和接口实现类同样的功能

cglib: 没有实现接口,创建父类子类的代理对象 和子类相同功能



# 源码分析

## BeanFactoryPostProcessors

BeanPostProcessors :bean后置处理器

​		在初始化前进行拦截

### BeanFactoryPostProcessors

- 在创建BeanFactory 标准初始化后调用的,可以进行定制修改BeanFactory .所有的Bean定义已经加载到容器中(BeanFactory ),但是还未创建实例对象

- 

  1. 创建容器: 

     ```java
             AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig2.class);
     ```

  2. ![image-20201210142232770](image-20201210142232770.png)



### BeanDefinitionRegistryPostProcessor

继承自 BeanFactoryPostProcessor 但是在BeanFactoryPostProcessors之前执行

<img src="image-20201210145224487.png" alt="image-20201210145224487" style="zoom:50%;" />

![image-20201210145543447](image-20201210145543447.png)

postProcessBeanDefinitionRegistry ():

BeanDefinitionRegistry里面保存的就是Bean的信息创建实例,BeanFactory就是按照这些信息创建Bean  可以注册一些Bean:

<img src="image-20201210150216141.png" alt="image-20201210150216141" style="zoom:50%;" />

>  可以直接更改BeanDefinition的定义信息,因为在实例化之前可以更改信息

```java
@Component
public class beanFacatoryRegisterPostProcessorsTest implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //RootBeanDefinition de = new RootBeanDefinition(xxx.class);在此就将信息更改为xxx类型,Blue就已经			被替代
        RootBeanDefinition de = new RootBeanDefinition(Blue.class);
//        BeanDefinitionBuilder de = BeanDefinitionBuilder.rootBeanDefinition(Blue.class);
        registry.registerBeanDefinition("blue",de);

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
```





```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}
```

​		









# Bean创建过程

```java

```

```java
 refresh();
```

- 

  ​	1. 

  ```
  prepareRefresh();//进行预处理
  ```

  

  2. 

  ```java
  ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();//获取BeanFactory
  ```

  进入

  ```java
  		[1] refreshBeanFactory();
  		[2] return getBeanFactory();
  ```

  

  ```java
  [1]
  // 进入此方法
  protected final void refreshBeanFactory() throws IllegalStateException {
  		if (!this.refreshed.compareAndSet(false, true)) {
  			throw new IllegalStateException(
  					"GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
  		}
      //创建一个序列化ID,此beanFactory 是在GenericApplicationContext执行无参构造器是创建的
      /*
      public class GenericApplicationContext extends 	 AbstractApplicationContext implements BeanDefinitionRegistry {
  
      private final DefaultListableBeanFactory beanFactory;
      public GenericApplicationContext() {
  		this.beanFactory = new DefaultListableBeanFactory();
  	}
      */
  		this.beanFactory.setSerializationId(getId());
  	}
  		return getBeanFactory();
  	}
  
  ```

  已经创建!

  ```java
  [2]
  //返回创建完成的BeanFactory
  public final ConfigurableListableBeanFactory getBeanFactory() {
  		return this.beanFactory;
  	}
  ```

  3. 

  ```java
  prepareBeanFactory(beanFactory);
  //设置完属性
  ```

  4. 

  ```java
  postProcessBeanFactory(beanFactory);//没有实现，忽略
  ```

  5. 

  ```java
  [3]
  invokeBeanFactoryPostProcessors(beanFactory);
  ```

  执行BeanFactoryPostProcessors方法

  ```java
  [3]
  PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
  //getBeanFactoryPostProcessors() 获取所有的BeanFactoryPostProcessors
  ```

  

  ```java
  	public static void invokeBeanFactoryPostProcessors(
  			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
  
  		Set<String> processedBeans = new HashSet<>();
  		//判断上一步获取所有的BeanFactoryPostProcessors是否属于BeanDefinitionRegistry,99%的属于
  		if (beanFactory instanceof BeanDefinitionRegistry) {
  			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
  			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
  			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
  			
  			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                  //判断是否属于BeanDefinitionRegistryPostProcessor
  				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
  					BeanDefinitionRegistryPostProcessor registryProcessor =
  							(BeanDefinitionRegistryPostProcessor) postProcessor;
                      //是的话直接执行
  					registryProcessor.postProcessBeanDefinitionRegistry(registry);
                      //加入到集合中
  					registryProcessors.add(registryProcessor);
  				}
  				else {
                      //不是的话加入另一个集合
  					regularPostProcessors.add(postProcessor);
  				}
  			} 
                 List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
  
  // First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
  			String[] postProcessorNames =
  					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
  			for (String ppName : postProcessorNames) {
  				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
  					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
  					processedBeans.add(ppName);
  				}
  			}
  			sortPostProcessors(currentRegistryProcessors, beanFactory);
  			registryProcessors.addAll(currentRegistryProcessors);
  			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
  			currentRegistryProcessors.clear();
   // Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
  			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
  			for (String ppName : postProcessorNames) {
  				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
  					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
  					processedBeans.add(ppName);
  				}
  			}
  			sortPostProcessors(currentRegistryProcessors, beanFactory);
  			registryProcessors.addAll(currentRegistryProcessors);
  			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
  			currentRegistryProcessors.clear();
  // Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
  			boolean reiterate = true;
  			while (reiterate) {
  				reiterate = false;
  				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
  				for (String ppName : postProcessorNames) {
  					if (!processedBeans.contains(ppName)) {
  						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
  						processedBeans.add(ppName);
  						reiterate = true;
  					}
  				}
  				sortPostProcessors(currentRegistryProcessors, beanFactory);
  				registryProcessors.addAll(currentRegistryProcessors);
  				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
  				currentRegistryProcessors.clear();
  			}
  ```

  ​	

  > First  Next Finally 执行的是以下方法	

  ```java
  private static void invokeBeanDefinitionRegistryPostProcessors(
  			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {
  
  		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
  			postProcessor.postProcessBeanDefinitionRegistry(registry);
  		}
  	}
  ```



​			

> ​		BeanDefinitionRegistryPostProcessor执行完毕之后 再来执行BeanFactoryPostProcessor

```java
String[] postProcessorNames =
      beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
// Ordered, and the rest.
List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
List<String> orderedPostProcessorNames = new ArrayList<>();
List<String> nonOrderedPostProcessorNames = new ArrayList<>();
for (String ppName : postProcessorNames) {
   if (processedBeans.contains(ppName)) {
      // skip - already processed in first phase above
   }
    //将符合条件的BeanFactoryPostProcessor 加入各个集合中
   else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
      priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
   }
   else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
      orderedPostProcessorNames.add(ppName);
   }
   else {
      nonOrderedPostProcessorNames.add(ppName);
   }
}

//将集合中的BeanFactoryPostProcessor元素按顺序执行
// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);
// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

```

6. 

```java
// Register bean processors that intercept bean creation.拦截Bean创建
registerBeanPostProcessors(beanFactory);
```

```java
//获取所有的BeanPostProcessor
String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
//................................
List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		//依然进行排序
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		//最终还会检查是否有监听器,在容器初始化完成后,注册到里面
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}
			/*
public Object postProcessAfterInitialization(Object bean, String beanName) {

this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
			*/
```

> First . Next .Finally 都执行:

```java
private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
            //将BeanPostProcess全部加入BeanFactory
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}
```

7. 

```java
//初始化一个messageSource
// Initialize message source for this context.
initMessageSource();
/*
	原理:
			获取beanFactory:
			ConfigurableListableBeanFactory beanFactory = getBeanFactory();
			如果有MessageSource类型的组件,就赋值给messagesource:
             this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			没有就创建一个默认的:
			DelegatingMessageSource dms = new DelegatingMessageSource();
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);


*/
//作用:
```

8. 

```java
//初始化事件派发器
initApplicationEventMulticaster() 
    /*
    	检查BeanFactory中是否有ApplicationEventMulticaster类型的事件
    	没有就创建
    	this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
		beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);

    */
```

9. 

```java
// Initialize other special beans in specific context subclasses.
//留给子类的,添加新的逻辑
				onRefresh();
```

10. 

```java
//注册事件派发器
registerListeners()
/*
	//获取Beanfactory中的监听器
	getApplicationEventMulticaster().addApplicationListener(listener);
	//注册
	getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);

*/
```

11. 

```java
// Instantiate all remaining (non-lazy-init) singletons.
//初始化所有剩余单实例Bean
finishBeanFactoryInitialization(beanFactory);
/*
	// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
		
		
			
            1. 先获取Bean定义信息
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            2.判断是否继承FactoryBean 
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) 
            3. 没有继承:
            getBean(beanName);
            	// Eagerly check singleton cache for manually registered singletons.
				Object sharedInstance = getSingleton(beanName);
				没有缓存就创建
					获取MergedLocalBeanDefinition信息:
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName)
					如果有Bean依赖的Bean,先创建被依赖的Bean
                        String[] dependsOn = mbd.getDependsOn();
                        registerDependentBean(dep, beanName);
 [creatBean]-----     没有就直接创建 (单实例)
                      
		
			
*/
```

​	[creatBean]--------

> ​				前奏

```java
//获取BeanDefinition信息
RootBeanDefinition mbdToUse = mbd;
//获取BeanDefinition类型
Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
//让BeanPostProcessors拦截器先拦截(正常的BeanPostProcessors在Bean创建完初始化前执行的),但是有一个特殊的
//hasInstantiationAwareBeanPostProcessors会在Bean创建之前执行
Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
	bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
		/*		进入applyBeanPostProcessorsBeforeInstantiation
		
				if (targetType != null) {
			[1]		bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
					if (bean != null) {
			[2]			bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
					}
				}
		*/


		/*
			[1]  	判断是否属于InstantiationAwareBeanPostProcessor
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
                 InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                    if (result != null) {
                        return result;
                    }
                }
             [2]	[1]执行后的结果不为空就执行[2]
		*/


	

```

​	[creatBean]------重点

​	

```java
	//	以上全部调用完毕后:
	protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
		// Instantiate the bean.
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
[1]----		instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}



[1]----/*
	createBeanInstance方法:
		Class<?> beanClass = resolveBeanClass(mbd, beanName);
		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
		}
		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
		if (instanceSupplier != null) {
			return obtainFromSupplier(instanceSupplier, beanName);
		}
		//执行加@Bean方法
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}
*/
    
    
    
    Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		// Allow post-processors to modify the merged bean definition.
		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
[2]-----			applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex);
				}
				mbd.postProcessed = true;
			}
		}
[2]---- /*进入方法 执行  末置的MergedBeanDefinitionPostProcessor后置处理器
	protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof MergedBeanDefinitionPostProcessor) {
				MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
				bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
			}
		}
	}
*/
    
    
 // Initialize the bean instance. 初始化Bean 属性赋值
		Object exposedObject = bean;
		try {
[3]----			populateBean(beanName, mbd, instanceWrapper);
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
/*  [3]-----------------
//先拿到BeanPostProcessor,执行postProcessAfterInstantiation
	for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
						return;
					}
				}
			}
			
			
...............................


//再执行postProcessPropertyValues
for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
					if (pvsToUse == null) {
						if (filteredPds == null) {
							filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
						}
						pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
						if (pvsToUse == null) {
							return;
						}
					}
					pvs = pvsToUse;
				}
			}
			
			
			
// 最后执行赋值
applyPropertyValues(beanName, mbd, bw, pvs);
*/
[4] -----  			exposedObject = initializeBean(beanName, exposedObject, mbd);
/*
	[4]----- //执行xxxxAware
	invokeAwareMethods(beanName, bean);
		进入 
		 实现了以下接口就进行回掉
		   BeanNameAware
            BeanClassLoaderAware
            BeanFactoryAware
			
*/


```



# BeanPostProcessors在Bean创建完初始化前执行的









# AOP

```
/**
 * AOP：【动态代理】
 * 		指在程序运行期间动态的将某段代码切入到指定方法指定位置进行运行的编程方式；
 * 
 * 1、导入aop模块；Spring AOP：(spring-aspects)
 * 2、定义一个业务逻辑类（MathCalculator）；在业务逻辑运行的时候将日志进行打印（方法之前、方法运行结束、方法出现异常，xxx）
 * 3、定义一个日志切面类（LogAspects）：切面类里面的方法需要动态感知MathCalculator.div运行到哪里然后执行；
 * 		通知方法：
 * 			前置通知(@Before)：logStart：在目标方法(div)运行之前运行
 * 			后置通知(@After)：logEnd：在目标方法(div)运行结束之后运行（无论方法正常结束还是异常结束）
 * 			返回通知(@AfterReturning)：logReturn：在目标方法(div)正常返回之后运行
 * 			异常通知(@AfterThrowing)：logException：在目标方法(div)出现异常以后运行
 * 			环绕通知(@Around)：动态代理，手动推进目标方法运行（joinPoint.procced()）
 * 4、给切面类的目标方法标注何时何地运行（通知注解）；
 * 5、将切面类和业务逻辑类（目标方法所在类）都加入到容器中;
 * 6、必须告诉Spring哪个类是切面类(给切面类上加一个注解：@Aspect)
 * [7]、给配置类中加 @EnableAspectJAutoProxy 【开启基于注解的aop模式】
 * 		在Spring中很多的 @EnableXXX;
 * 
 * 三步：
 * 	1）、将业务逻辑组件和切面类都加入到容器中；告诉Spring哪个是切面类（@Aspect）
 * 	2）、在切面类上的每一个通知方法上标注通知注解，告诉Spring何时何地运行（切入点表达式）
 *  3）、开启基于注解的aop模式；@EnableAspectJAutoProxy
 *  
 * AOP原理：【看给容器中注册了什么组件，这个组件什么时候工作，这个组件的功能是什么？】
 * 		@EnableAspectJAutoProxy；
 * 1、@EnableAspectJAutoProxy是什么？
 * 		@Import(AspectJAutoProxyRegistrar.class)：给容器中导入AspectJAutoProxyRegistrar
 * 			利用AspectJAutoProxyRegistrar自定义给容器中注册bean；BeanDefinetion
 * 			internalAutoProxyCreator=AnnotationAwareAspectJAutoProxyCreator
 * 
 * 		给容器中注册一个AnnotationAwareAspectJAutoProxyCreator；
 * 
 * 2、 AnnotationAwareAspectJAutoProxyCreator：
 * 		AnnotationAwareAspectJAutoProxyCreator
 * 			->AspectJAwareAdvisorAutoProxyCreator
 * 				->AbstractAdvisorAutoProxyCreator
 * 					->AbstractAutoProxyCreator
 * 							implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware
 * 						关注后置处理器（在bean初始化完成前后做事情）、自动装配BeanFactory
 * 
 * AbstractAutoProxyCreator.setBeanFactory()
 * AbstractAutoProxyCreator.有后置处理器的逻辑；
 * 
 * AbstractAdvisorAutoProxyCreator.setBeanFactory()-》initBeanFactory()
 * 
 * AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()
 *
 *
 * 流程：
 * 		1）、传入配置类，创建ioc容器
 * 		2）、注册配置类，调用refresh（）刷新容器；
 * 		3）、registerBeanPostProcessors(beanFactory);注册bean的后置处理器来方便拦截bean的创建；
 * 			1）、先获取ioc容器已经定义了的需要创建对象的所有BeanPostProcessor
 * 			2）、给容器中加别的BeanPostProcessor
 * 			3）、优先注册实现了PriorityOrdered接口的BeanPostProcessor；
 * 			4）、再给容器中注册实现了Ordered接口的BeanPostProcessor；
 * 			5）、注册没实现优先级接口的BeanPostProcessor；
 * 			6）、注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，保存在容器中；
 * 				创建internalAutoProxyCreator的BeanPostProcessor【AnnotationAwareAspectJAutoProxyCreator】
 * 				1）、创建Bean的实例
 * 				2）、populateBean；给bean的各种属性赋值
 * 				3）、initializeBean：初始化bean；
 * 						1）、invokeAwareMethods()：处理Aware接口的方法回调
 * 						2）、applyBeanPostProcessorsBeforeInitialization()：应用后置处理器的postProcessBeforeInitialization（）
 * 						3）、invokeInitMethods()；执行自定义的初始化方法
 * 						4）、applyBeanPostProcessorsAfterInitialization()；执行后置处理器的postProcessAfterInitialization（）；
 * 				4）、BeanPostProcessor(AnnotationAwareAspectJAutoProxyCreator)创建成功；--》aspectJAdvisorsBuilder
 * 			7）、把BeanPostProcessor注册到BeanFactory中；
 * 				beanFactory.addBeanPostProcessor(postProcessor);
 * =======以上是创建和注册AnnotationAwareAspectJAutoProxyCreator的过程========
 * 
 * 			AnnotationAwareAspectJAutoProxyCreator => InstantiationAwareBeanPostProcessor
 * 		4）、finishBeanFactoryInitialization(beanFactory);完成BeanFactory初始化工作；创建剩下的单实例bean
 * 			1）、遍历获取容器中所有的Bean，依次创建对象getBean(beanName);
 * 				getBean->doGetBean()->getSingleton()->
 * 			2）、创建bean
 * 				【AnnotationAwareAspectJAutoProxyCreator在所有bean创建之前会有一个拦截，InstantiationAwareBeanPostProcessor，会调用postProcessBeforeInstantiation()】
 * 				1）、先从缓存中获取当前bean，如果能获取到，说明bean是之前被创建过的，直接使用，否则再创建；
 * 					只要创建好的Bean都会被缓存起来
 * 				2）、createBean（）;创建bean；
 * 					AnnotationAwareAspectJAutoProxyCreator 会在任何bean创建之前先尝试返回bean的实例
 * 					【BeanPostProcessor是在Bean对象创建完成初始化前后调用的】
 * 					【InstantiationAwareBeanPostProcessor是在创建Bean实例之前先尝试用后置处理器返回对象的】
 * 					1）、resolveBeforeInstantiation(beanName, mbdToUse);解析BeforeInstantiation
 * 						希望后置处理器在此能返回一个代理对象；如果能返回代理对象就使用，如果不能就继续
 * 						1）、后置处理器先尝试返回对象；
 * 							bean = applyBeanPostProcessorsBeforeInstantiation（）：
 * 								拿到所有后置处理器，如果是InstantiationAwareBeanPostProcessor;
 * 								就执行postProcessBeforeInstantiation
 * 							if (bean != null) {
								bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
							}
 * 
 * 					2）、doCreateBean(beanName, mbdToUse, args);真正的去创建一个bean实例；和3.6流程一样；
 * 					3）、
 * 			
 * 		
 *
 */
```

# AOP

```
@EnableAspectJAutoProxy
	@Import(AspectJAutoProxyRegistrar.class)
	//进入AspectJAutoProxyRegistrar
			AnnotationAwareAspectJAutoProxyCreator
```

```java
public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

[1]----		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
		//将@EnableAspectJAutoProxy注解信息
		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}



[1]----
    /*
    public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {
			
		//此方法调用registerOrEscalateApcAsRequired,并传入AnnotationAwareAspectJAutoProxyCreator.class
		
[2]----	return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}




    
   [2]----- 	 private static BeanDefinition registerOrEscalateApcAsRequired(
			Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
	//	此时的AUTO_PROXY_CREATOR_BEAN_NAME=org.springframework.aop.config.internalAutoProxyCreator
	//  没有internalAutoProxyCreator,略过
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
			
				int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				if (currentPriority < requiredPriority) {
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			return null;
		}
在这就注册了一个cls ,就是并传入AnnotationAwareAspectJAutoProxyCreator
		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
注册:		
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;
	}
	*/

```

```
2、 AnnotationAwareAspectJAutoProxyCreator：这个组件类型就是internalAutoProxyCreator
 * 		AnnotationAwareAspectJAutoProxyCreator
 * 			->AspectJAwareAdvisorAutoProxyCreator
 * 				->AbstractAdvisorAutoProxyCreator
 * 					->AbstractAutoProxyCreator
 * 							implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware
 * 						关注后置处理器（在bean初始化完成前后做事情）、自动装配BeanFactory
 * 在以下打断点
 * AbstractAutoProxyCreator.setBeanFactory()	
 * AbstractAutoProxyCreator.有后置处理器的逻辑；
 * 
 * AbstractAdvisorAutoProxyCreator.setBeanFactory()-》initBeanFactory()
 * 
 * AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()
 *
 *
```

<img src="image-20201215121449220.png" alt="image-20201215121449220" style="zoom:50%;" />







# 

> 
