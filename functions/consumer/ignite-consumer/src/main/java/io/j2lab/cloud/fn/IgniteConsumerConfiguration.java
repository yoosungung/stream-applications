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

package io.j2lab.cloud.fn;

import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * @author Eric Yoo
 */

@Configuration
@EnableConfigurationProperties(IgniteConsumerProperties.class)
public class IgniteConsumerConfiguration {

	@Bean
	public ClientCache<String, JsonNode> igniteCache(IgniteConsumerProperties igniteConsumerProperties) {
		ClientConfiguration cfg = new ClientConfiguration();
		cfg.setAddresses(igniteConsumerProperties.serverUrls());

		IgniteClient client = Ignition.startClient(cfg);

		return client.getOrCreateCache(igniteConsumerProperties.getCacheName());
	}

	@Bean
	public Consumer<Message<JsonNode>> igniteConsumer(
			ClientCache<String, JsonNode> clientCache,
			IgniteConsumerProperties igniteConsumerProperties) {
		return message -> {
				JsonNode body = message.getPayload();
				String key = igniteConsumerProperties.getValue(body);
				clientCache.put(key, body);
			};
	}

}
