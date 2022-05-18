/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.j2lab.cloud.fn.custom;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Function;

import reactor.core.publisher.Flux;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * @author Eric Yoo
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CustomFunctionProperties.class)
public class CustomFunctionConfiguration {

	private ClassLoader loader;

	@Bean
	public Function<Flux<Message<?>>, Flux<Message<?>>> customFunction(CustomFunctionProperties props) {
		if (props.getUrl() != null && props.getUrl().length() > 0) {
			try {
				this.loader = new URLClassLoader(new URL[] {new URL(props.getUrl())}, ClassLoader.getSystemClassLoader());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		else {
			this.loader = ClassLoader.getSystemClassLoader();
		}

		if (props.getClassName() != null && props.getClassName().length() > 0) {
			try {
				Class<?> resultClass = this.loader.loadClass(props.getClassName());
				return (Function) resultClass.newInstance();
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		else {
			return new SampleFunction();
		}

		return null;
	}
}
