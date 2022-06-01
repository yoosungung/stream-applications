/*
 * Copyright 2011-2021 the original author or authors.
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

package io.j2lab.cloud.fn.supplier.matomo;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MultiValueMapAdapter;

/**
 * Configuration for the Matomo Supplier.
 *
 * @author Eric Yoo
 */
@EnableConfigurationProperties(MatomoSupplierProperties.class)
@Configuration
public class MatomoSupplierConfiguration {

	private ObjectMapper mapper = new ObjectMapper();
	@Bean
	@ConditionalOnMissingBean
	public HeaderMapper<HttpHeaders> matomoHeaderMapper(MatomoSupplierProperties matomoSupplierProperties) {
		DefaultHttpHeaderMapper defaultHttpHeaderMapper = DefaultHttpHeaderMapper.inboundMapper();
		defaultHttpHeaderMapper.setInboundHeaderNames(matomoSupplierProperties.getMappedRequestHeaders());
		return defaultHttpHeaderMapper;
	}

	@Bean
	public Publisher<Message<JsonNode>> matomoSupplierFlow(MatomoSupplierProperties matomoSupplierProperties,
			HeaderMapper<HttpHeaders> matomoHeaderMapper, ServerCodecConfigurer serverCodecConfigurer) {

		return IntegrationFlows.from(
						WebFlux.inboundChannelAdapter(matomoSupplierProperties.getPathPattern())
								.requestMapping(m -> m.methods(HttpMethod.POST))
								.requestPayloadType(JsonNode.class)
								.statusCodeExpression(new ValueExpression<>(HttpStatus.ACCEPTED))
								.headerMapper(matomoHeaderMapper)
								.codecConfigurer(serverCodecConfigurer)
								.crossOrigin(crossOrigin ->
										crossOrigin.origin(matomoSupplierProperties.getCors().getAllowedOrigins())
												.allowedHeaders(matomoSupplierProperties.getCors().getAllowedHeaders())
												.allowCredentials(matomoSupplierProperties.getCors().getAllowCredentials()))
								.autoStartup(false))
				.enrichHeaders((headers) ->
						headers.headerFunction(MessageHeaders.CONTENT_TYPE,
								(message) ->
										(MediaType.APPLICATION_FORM_URLENCODED.equals(
												message.getHeaders().get(MessageHeaders.CONTENT_TYPE, MediaType.class)))
												? MediaType.APPLICATION_JSON
												: null,
								true))
				.transform(Message.class, message -> {
					MessageHeaders headers = message.getHeaders();
					String url = headers.get("http_requestUrl").toString();

					if (url != null && url.length() > 0 && url.contains("/matomo.php?")) {

						ObjectNode body = null;
						if (!((MultiValueMapAdapter) message.getPayload()).isEmpty()) {
							body = ((ObjectNode) message.getPayload());
						}
						else {
							body = mapper.createObjectNode();
						}

						Map<String, Object> newHeaders = new HashMap<String, Object>();
						for (String k : headers.keySet()) {
							if (!"http_requestUrl".equals(k)) {
								Object val = headers.get(k);
								if (val instanceof Serializable) {
									newHeaders.put(k, val);
								}
							}
						}

						try {
							url = URLDecoder.decode(url, "UTF-8");
						}
						catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
						String[] queryPath = url.split("/matomo.php?");
						String[] pairs = queryPath[1].substring(1).split("&");
						for (String pair : pairs) {
							int idx = pair.indexOf("=");
							body.put(pair.substring(0, idx), pair.substring(idx + 1));
						}

						if (body.has("_id")) {
							newHeaders.put("kafka_messageKey", body.get("_id").asText().getBytes());
						}

						Message<JsonNode> newMessage = MessageBuilder.withPayload((JsonNode) body)
								.copyHeaders(newHeaders)
								.build();

						return newMessage;
					}
					else {
						return message;
					}
				})
				.toReactivePublisher();
	}

	@Bean
	public Supplier<Flux<Message<JsonNode>>> matomoSupplier(
			Publisher<Message<JsonNode>> matomoRequestPublisher,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {

		return () -> Flux.from(matomoRequestPublisher)
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

}
