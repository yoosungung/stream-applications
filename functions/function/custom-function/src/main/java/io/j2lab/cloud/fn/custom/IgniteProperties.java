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
import org.springframework.validation.annotation.Validated;

/**
 * Used to configure those Ignite Sink module options that are not related to connecting to Ignite.
 *
 * @author Eric Yoo
 */

@ConfigurationProperties("ignite.consumer")
@Validated
public class IgniteProperties {

	/**
	 * Ignite thin client server Url.
	 */
	private String[] serverUrl;

	/**
	 * Ignite save target cache name.
	 */
	private String cacheName;

	public String[] getServerUrl() {
		return this.serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		String[] urls = serverUrl.split(",");
		this.serverUrl = urls;
	}

	public String getCacheName() {
		return this.cacheName;
	}
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

}
