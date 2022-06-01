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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ignite.client.ClientCache;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
			"ignite.consumer.serverUrl=218.152.137.218:10800",
			"ignite.consumer.cacheName=tagUserId",
			"ignite.consumer.keyExpression=k"
		})
class IgniteConsumerConfigurationTest {
	@Autowired
	Consumer<Message<JsonNode>> igniteConsumer;

	@Autowired
	ClientCache<String, JsonNode> clientCache;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void test_thinClient() throws JsonProcessingException {
		JsonNode sent = this.mapper.readTree("{\"k\": \"123\",\"v1\": \"asdf\",\"v2\": \"qwer\"}");
		final Message<JsonNode> message = MessageBuilder.withPayload(sent).build();
		this.igniteConsumer.accept(message);
		JsonNode result = this.clientCache.get("123");
		assertThat(result).isEqualTo(sent);
	}

	@Test
	void test_getvalue() {
		JsonNode res = clientCache.get("8227bfa2087fd101");
		res.toString();
	}

	@SpringBootApplication
	static class IgniteConsumerTestApplication {
	}
}
