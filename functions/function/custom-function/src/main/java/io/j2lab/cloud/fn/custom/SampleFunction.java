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

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.ignite.client.ClientCache;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

public class SampleFunction implements Function<Flux<Message<?>>, Flux<Message<?>>> {

	@Autowired
	ClientCache<String, JsonNode> igniteCache;

	@Override
	public Flux<Message<?>> apply(Flux<Message<?>> input) {
		return input.filter(message -> {
			JsonNode body = (JsonNode) message.getPayload();
			return (body.has("e_n") && "getEmail".equals(body.get("e_n").asText()));
		});
	}

}
