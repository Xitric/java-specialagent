package io.opentracing.contrib.specialagent;

/**
 * @author Kasper
 */
public class CallDepthThreadLocalMap {

	private static final ClassValue<ThreadLocalDepth> TLS =
			new ClassValue<ThreadLocalDepth>() {
				@Override
				protected ThreadLocalDepth computeValue(Class<?> type) {
					return new ThreadLocalDepth();
				}
			};

	public static CallDepth getCallDepth(Class<?> k) {
		return TLS.get(k).get();
	}

	public static int incrementCallDepth(Class<?> k) {
		return TLS.get(k).get().getAndIncrement();
	}

	public static int decrementCallDepth(Class<?> k) {
		return TLS.get(k).get().decrementAndGet();
	}

	public static void reset(Class<?> k) {
		TLS.get(k).get().reset();
	}

	private static final class ThreadLocalDepth extends ThreadLocal<CallDepth> {
		@Override
		protected CallDepth initialValue() {
			return new CallDepth();
		}
	}
}