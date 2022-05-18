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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the custom function.
 *
 * @author Eric Yoo
 */
@ConfigurationProperties("j2lab.custom.function")
public class CustomFunctionProperties {

	/**
	 * custom function class name.
	 */
	private String className = "";

	/**
	 * load URL for custom function.
	 */
	private String url = "";

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) { this.url = url; }

}
