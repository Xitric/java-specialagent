package io.opentracing.contrib.specialagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.TypeConstantAdjustment;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import static io.opentracing.contrib.specialagent.DefaultAgentRule.log;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author Kasper
 */
public class OsgiClassLoaderAgent {
	public static final ClassFileLocator locatorProxy = BootLoaderAgent.cachedLocator;

	//	private static final AgentBuilder.LocationStrategy bootFallbackLocationStrategy = new AgentBuilder.LocationStrategy() {
	//		@Override
	//		public ClassFileLocator classFileLocator(final ClassLoader classLoader, final JavaModule module) {
	//			return new ClassFileLocator.Compound(
	//					ClassFileLocator.ForClassLoader.ofBootLoader(),
	//					ClassFileLocator.ForClassLoader.of(classLoader)
	//			);
	//		}
	//	};
	//
	//	public static AgentBuilder premain(final AgentBuilder builder) {
	//		log("\n<<<<<<<<<<<<<<<<< Installing OsgiClassLoaderAgent >>>>>>>>>>>>>>>>>>\n", null, DefaultAgentRule.DefaultLevel.FINE);
	//		try {
	//			return builder
	//					.with(bootFallbackLocationStrategy)
	//					.type(isSubTypeOf(ClassLoader.class))
	//					.and(ElementMatchers.<TypeDescription>nameContains("osgi"))
	//					.transform(new AgentBuilder.Transformer() {
	//						           @Override
	//						           public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
	//							           return builder.visit();
	//						           }
	//					           }
	//					);
	//		} finally {
	//			log("\n>>>>>>>>>>>>>>>>>> Installed OsgiClassLoaderAgent <<<<<<<<<<<<<<<<<<\n", null, DefaultAgentRule.DefaultLevel.FINE);
	//		}
	//	}

	public static AgentBuilder premain(final AgentBuilder builder) {
		log("\n<<<<<<<<<<<<<<<<< Installing OsgiClassLoaderAgent >>>>>>>>>>>>>>>>>>\n", null, DefaultAgentRule.DefaultLevel.FINE);
		try {
			return builder.type(isSubTypeOf(ClassLoader.class)).and(ElementMatchers.<TypeDescription>nameContains("osgi")).transform(new AgentBuilder.Transformer() {
				@Override
				public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
					return builder
							.visit(TypeConstantAdjustment.INSTANCE)
//							.visit(Advice.to(LoadClassAdvice.class).on(named("findClass").and(returns(Class.class).and(takesArgument(0, String.class)))));
							.visit(Advice.to(LoadClassAdvice.class).on(isMethod()
									.and(named("loadClass"))
									.and(
											takesArguments(1)
													.and(takesArgument(0, named("java.lang.String")))
													.or(
															takesArguments(2)
																	.and(takesArgument(0, named("java.lang.String")))
																	.and(takesArgument(1, named("boolean")))))
									.and(isPublic().or(isProtected()))
									.and(not(isStatic()))));
				}});
//			return builder.transform(
//					new AgentBuilder.Transformer.ForAdvice()
//							.include(BootProxyClassLoader.INSTANCE, SpecialAgent.class.getClassLoader())
//							.advice(
//									named("findClass").and(returns(Class.class).and(takesArgument(0, String.class))),
//									OsgiClassLoaderAgent.class.getName() + "$LoadClassAdvice"
//							)
//			);
		}
		finally {
			log("\n>>>>>>>>>>>>>>>>>> Installed OsgiClassLoaderAgent <<<<<<<<<<<<<<<<<<\n", null, DefaultAgentRule.DefaultLevel.FINE);
		}
	}

	public static class LoadClassAdvice {
		@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
		public static Class<?> onEnter(@Advice.Argument(0) String name, @Advice.This ClassLoader injectedOn) {
			ClassLoaderLogger.log(injectedOn, "Enter LoadClass for " + name);
			ClassLoaderLogger.dumpHierarchy(injectedOn);
			// need to use call depth here to prevent re-entry from call to Class.forName() below
			// because on some JVMs (e.g. IBM's, though IBM bootstrap loader is explicitly excluded above)
			// Class.forName() ends up calling loadClass() on the bootstrap loader which would then come
			// back to this instrumentation over and over, causing a StackOverflowError
			// TODO: Not a problem for me? ;)
			int callDepth = CallDepthThreadLocalMap.incrementCallDepth(ClassLoader.class);
			if (callDepth > 0) {
				return null;
			}

//			if (true) {
//				throw new RuntimeException("Okay, we actually injected something. Cool!");
//			}

			try (PrintWriter writer = new PrintWriter(new FileWriter("C:\\Users\\Kasper\\Desktop\\loaderlog.txt", true))) {

				writer.println("Trying to load: " + name);

				try {
					//				for (String prefix : Holder.bootstrapPackagesPrefixes) {
					if (name.startsWith("io.opentracing")) {
						writer.println("Thread is " + Thread.currentThread());
						ClassLoaderLogger.log(injectedOn, "Thread is " + Thread.currentThread());

						try {
							Class a = Class.forName(name, false, null);
							writer.println("Boot loader found: " + a);
						} catch (ClassNotFoundException ignored) {
							writer.println("Boot loader failed");
						}

						ClassLoaderLogger.log(injectedOn, "Determining hierarchy for bootproxy");
						ClassLoaderLogger.dumpHierarchy(BootProxyClassLoader.INSTANCE);

						try {
							Class b = Class.forName(name, false, BootProxyClassLoader.INSTANCE);
							writer.println(BootProxyClassLoader.INSTANCE + " found: " + b);
						} catch (ClassNotFoundException ignored) {
							writer.println(BootProxyClassLoader.INSTANCE + " failed");
						}

						ClassLoaderLogger.log(injectedOn, "Determining hierarchy for SpecialAgent");
						ClassLoaderLogger.dumpHierarchy(SpecialAgent.class.getClassLoader());

						try {
							Class c = Class.forName(name, false, SpecialAgent.class.getClassLoader());
							writer.println(SpecialAgent.class.getClassLoader() + " found: " + c);
						} catch (ClassNotFoundException ignored) {
							writer.println(SpecialAgent.class.getClassLoader() + " failed");
						}

						Class d = BootProxyClassLoader.INSTANCE.loadClassOrNull(name, false);
						writer.println(BootProxyClassLoader.INSTANCE + " found by special means: " + d);

						try {
							Class e = Class.forName(name, false, injectedOn.getParent());
							writer.println("Parent classloader found: " + e);
						} catch (ClassNotFoundException ignored) {
							writer.println("Parent classloader failed");
						}

						ClassLoaderLogger.log(injectedOn, "Determining hierarchy for ContextClassLoader");
						ClassLoaderLogger.dumpHierarchy(Thread.currentThread().getContextClassLoader());

						try {
							Class e = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
							writer.println(Thread.currentThread().getContextClassLoader() + " found: " + e);
						} catch (ClassNotFoundException ignored) {
							writer.println(Thread.currentThread().getContextClassLoader() + " failed");
						}

//						if (d == null) {
							writer.println("Injected on: " + injectedOn + ", " + (injectedOn != null ? injectedOn.getClass().getName() + "@" + Integer.toString(System.identityHashCode(injectedOn), 16) : "null"));
							writer.println("Dump:");
							writer.println("Parent: " + injectedOn.getParent());
							writer.println("Resource: " + injectedOn.getResource(name));
							writer.println("Resources: " + Collections.list(injectedOn.getResources("io.opentracing.contrib")));
							writer.print("----------------");
//						}
						
                        try {
							return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
						} catch (ClassNotFoundException ignored) {
							writer.println("Failed altogether?");
						}
					}
					//				}
				} finally {
					// need to reset it right away, not waiting until onExit()
					// otherwise it will prevent this instrumentation from being applied when loadClass()
					// ends up calling a ClassFileTransformer which ends up calling loadClass() further down the
					// stack on one of our bootstrap packages (since the call depth check would then suppress
					// the nested loadClass instrumentation)
					CallDepthThreadLocalMap.reset(ClassLoader.class);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			ClassLoaderLogger.log(injectedOn, "Could not find " + name);
			return null;
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void onExit(
				@Advice.Return(readOnly = false) Class<?> result,
				@Advice.Enter Class<?> resultFromBootstrapLoader,
				@Advice.This ClassLoader injectedOn,
				@Advice.Argument(0) String name) {
			ClassLoaderLogger.log(injectedOn, "Exit LoadClass for " + name);
			if (resultFromBootstrapLoader != null) {
				ClassLoaderLogger.log(injectedOn, "Result from fallback was " + resultFromBootstrapLoader);
				result = resultFromBootstrapLoader;
			}
		}
	}
}
