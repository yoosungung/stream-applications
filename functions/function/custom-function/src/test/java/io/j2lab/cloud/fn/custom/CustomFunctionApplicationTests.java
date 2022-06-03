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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
		"j2lab.custom.function.class-name=io.j2lab.cloud.fn.custom.Test1",
		"j2lab.custom.function.class-loader-url=file:///Users/eric/Documents/stream-applications/functions/function/custom-function/target/test-classes/",
		"j2lab.custom.function.ignite-url=218.152.137.218:10800",
		"j2lab.custom.function.cache-name=tagUserId"
})
@DirtiesContext
public class CustomFunctionApplicationTests {

	@Autowired
	Function<Flux<Message<?>>, Flux<Message<?>>> custom;

	@Test
	public void testFilter() {
		ObjectMapper mapper = new ObjectMapper();

		Flux<Message<?>> messageFlux =
				Flux.just("{\"_id\": \"8227bfa2087fd101\"}")
						.map(itm -> {
							JsonNode body = null;
							try {
								body = mapper.readTree(itm);
								return new GenericMessage(body);
							}
							catch (JsonProcessingException e) {
								throw new RuntimeException(e);
							}
						});
		Flux<Message<?>> result = this.custom.apply(messageFlux);
		/*
		result.map(Message::getPayload)
				.cast(String.class)
				.as(StepVerifier::create)
				.expectNext("{}")
				.expectComplete()
				.verify();
		 */
		result.subscribe();
	}

	@SpringBootApplication
	static class CustomFunctionTestApplication {

	}

}
