package io.opentracing.contrib.specialagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * @author Kasper
 */
public class ClassLoadListener implements AgentBuilder.Listener {

	private void log(String msg) {
		log(msg, null);
	}

	private void log(String msg, Throwable t) {
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				Paths.get("C:\\Users\\Kasper\\Desktop", "buddylog.txt"),
				StandardCharsets.UTF_8,
				StandardOpenOption.APPEND,
				StandardOpenOption.CREATE))) {
			writer.println(msg);

			if (t != null) {
				writer.println(t.toString());
				t.printStackTrace(writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDiscovery(
			String typeName, ClassLoader classLoader, JavaModule javaModule, boolean b) {
		log("onDiscovery: " + typeName + ", " + AssembleUtil.getNameId(classLoader) + ", " + b);
	}

	@Override
	public void onTransformation(
			TypeDescription typeDescription,
			ClassLoader classLoader,
			JavaModule javaModule,
			boolean b,
			DynamicType dynamicType) {
		log("onTransformation: " + typeDescription.getActualName() + ", " + AssembleUtil.getNameId(classLoader) + ", " + b + ", " + dynamicType);
	}

	@Override
	public void onIgnored(
			TypeDescription typeDescription,
			ClassLoader classLoader,
			JavaModule javaModule,
			boolean b) {
		log("onIgnored: " + typeDescription.getActualName() + ", " + AssembleUtil.getNameId(classLoader) + ", " + b);
	}

	@Override
	public void onError(
			String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
		log("onError: " + s + ", " + AssembleUtil.getNameId(classLoader) + ", " + b, throwable);
	}

	@Override
	public void onComplete(String typeName, ClassLoader classLoader, JavaModule javaModule, boolean b) {
		log("onComplete: " + typeName + ", " + AssembleUtil.getNameId(classLoader) + ", " + b);
	}
}