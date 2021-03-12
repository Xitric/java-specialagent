package io.opentracing.contrib.specialagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.TypeConstantAdjustment;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import static io.opentracing.contrib.specialagent.DefaultAgentRule.log;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * An additional classloading injection performed specifically for OSGi classloaders, since they work a little different
 * from others. This has caused missing class injections, and as a result many ClassNotFoundExceptions at runtime.
 *
 * @author Kasper
 */
public class OsgiClassLoaderAgent {

	private static final String CLASS_LOADER_NAME = "osgi";
	private static final String SPECIAL_AGENT_NAMESPACE = "io.opentracing.contrib.specialagent.rule";

	public static AgentBuilder premain(final AgentBuilder builder) {
		log("\n<<<<<<<<<<<<<<<<< Installing OsgiClassLoaderAgent >>>>>>>>>>>>>>>>>>\n", null, DefaultAgentRule.DefaultLevel.FINE);
		try {
			return builder.type(isSubTypeOf(ClassLoader.class))
					.and(ElementMatchers.<TypeDescription>nameContains(CLASS_LOADER_NAME))
					.transform(new AgentBuilder.Transformer() {
						@Override
						public DynamicType.Builder<?> transform(
								final DynamicType.Builder<?> builder,
								final TypeDescription typeDescription,
								final ClassLoader classLoader,
								final JavaModule module
						) {
							return builder
									.visit(TypeConstantAdjustment.INSTANCE)
									.visit(Advice.to(LoadClass.class).on(
											named("loadClass").and(
													takesArguments(2)
															.and(takesArgument(0, String.class))
															.and(takesArgument(1, Boolean.class)))
													.and(returns(Class.class).and(isPublic().or(isProtected())))
											)
									);
						}
					});
		} finally {
			log("\n>>>>>>>>>>>>>>>>>> Installed OsgiClassLoaderAgent <<<<<<<<<<<<<<<<<<\n", null, DefaultAgentRule.DefaultLevel.FINE);
		}
	}

	public static class LoadClass {
		public static Method defineClass;

		@Advice.OnMethodExit(onThrowable = ClassNotFoundException.class)
		public static void onExit(
				final @Advice.This ClassLoader thiz,
				final @Advice.Argument(0) String name,
				final @Advice.Argument(1) Boolean resolve,
				@Advice.Return(readOnly = false, typing = Typing.DYNAMIC) Class<?> result,
				@Advice.Thrown(readOnly = false, typing = Typing.DYNAMIC) ClassNotFoundException thrown) {

			// Ideally, we should only be messing with class lookups from within the namespace of SpecialAgent
			if (!name.startsWith(SPECIAL_AGENT_NAMESPACE)) {
				return;
			}

			// Some classloaders do not propagate to their parent, so we attempt to resolve the lookup using the boot
			// loader
			try {
				result = Class.forName(name, resolve, null);
				thrown = null;
				return;
			} catch (ClassNotFoundException ignored) {
			}

			// Finally, we can resort to SpecialAgent, since at this point we are either missing a deferred injection or
			// something else is very wrong
			try {
				final byte[] bytecode = SpecialAgent.findClass(thiz, name);
				if (bytecode != null) {
					if (defineClass == null)
						defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);

					result = (Class<?>) defineClass.invoke(thiz, name, bytecode, 0, bytecode.length, null);
					thrown = null;
				}
			} catch (NoSuchMethodException | InvocationTargetException e) {
				log("<><><><> OsgiClassLoaderAgent.LoadClass#exit", e, DefaultAgentRule.DefaultLevel.SEVERE);
			} catch (IllegalAccessException e) {
				log("<><><><> OsgiClassLoaderAgent.LoadClass#exit may have encountered a JVM bug", e, DefaultAgentRule.DefaultLevel.SEVERE);
			}
		}
	}
}
