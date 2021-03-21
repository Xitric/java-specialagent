/* Copyright 2021 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent.rule.myfaces12;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import javax.faces.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class MyFacesAgentIntercept {

	final Tracer tracer = GlobalTracer.get();

	public static void logThatShit(String msg) {
		try (PrintWriter writer = new PrintWriter(
				new BufferedOutputStream(
						new FileOutputStream("C:/Users/Kasper/Desktop/jsflog.txt", true)
				), true)
		) {
			writer.println(msg);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void onProcessActionEnter(Object e) {
		ActionEvent event = (ActionEvent) e;
		logThatShit("Intercepted event enter for " + event);
		logThatShit("\tClass: " + event.getClass());
		logThatShit("\tPhase id: " + event.getPhaseId().getOrdinal());
		logThatShit("\tSource: " + event.getSource());
		logThatShit("\tComponent id: " + event.getComponent().getId());
		logThatShit("\tComponent family: " + event.getComponent().getFamily());
		logThatShit("\tComponent renderer: " + event.getComponent().getRendererType());
		logThatShit("\tComponent attribute keys: " + Arrays.toString(event.getComponent().getAttributes().keySet().toArray()));
	}

	public static void onProcessActionExit(Object e) {
		ActionEvent event = (ActionEvent) e;
		logThatShit("Intercepted event exit for " + event);
	}
}
