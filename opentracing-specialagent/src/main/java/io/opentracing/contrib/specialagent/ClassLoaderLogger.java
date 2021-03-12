package io.opentracing.contrib.specialagent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Kasper
 */
public class ClassLoaderLogger {

	private static String toName(ClassLoader loader) {
		return loader != null ? loader.getClass().getName() + "@" + Integer.toString(System.identityHashCode(loader), 16) : "null";
	}

	public static synchronized void log(String msg) {
		log(null, msg, null);
	}

	public static synchronized void log(ClassLoader loader, String msg) {
		log(loader, msg, null);
	}

	public static synchronized void log(ClassLoader loader, String msg, Throwable t) {
		// try (PrintWriter writer = new PrintWriter(
		// 		new FileWriter("C:\\Users\\Kasper\\Desktop\\classloader-log.txt", true)
		// )) {
		// 	writer.println("[" + Thread.currentThread().getName() + ", " + toName(loader) + "]: " + msg);

		// 	if (t != null) {
		// 		writer.println(t.toString());
		// 		t.printStackTrace(writer);
		// 	}
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
	}

	public static synchronized void dumpHierarchy(ClassLoader loader) {
		// String loaderList = "Hierarchy is ";
		// for (ClassLoader current = loader; current != null; current = current.getParent()) {
		// 	loaderList += toName(current) + (current.getParent() == null ? "" : " -> ");
		// }
		// log(loader, loaderList);
	}
}
