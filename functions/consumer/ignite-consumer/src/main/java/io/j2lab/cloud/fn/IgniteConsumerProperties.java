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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.json.JsonNodeWrapperToJsonNodeConverter;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.validation.annotation.Validated;

/**
 * Used to configure those Ignite Sink module options that are not related to connecting to Ignite.
 *
 * @author Eric Yoo
 */

@ConfigurationProperties("ignite.consumer")
@Validated
public class IgniteConsumerProperties {
	private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
	private static final StandardEvaluationContext EXPRESSION_CONTEXT = new StandardEvaluationContext();

	/**
	 * Ignite thin client server Url.
	 */
	private String serverUrl;

	/**
	 * Ignite save target cache name.
	 */
	private String cacheName;

	/**
	 * A SpEL expression to use for storing to a key.
	 */
	private String keyExpression;

	public IgniteConsumerProperties() {
		this.EXPRESSION_CONTEXT.addPropertyAccessor(new JsonPropertyAccessor());
		ConverterRegistry converterRegistry = (ConverterRegistry) DefaultConversionService.getSharedInstance();
		converterRegistry.addConverter(new JsonNodeWrapperToJsonNodeConverter());
	}

	public String getServerUrl() {
		return this.serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getCacheName() {
		return this.cacheName;
	}
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getKeyExpression() {
		return this.keyExpression;
	}
	public void setKeyExpression(String keyExpression) {
		this.keyExpression = keyExpression;
	}

	public String[] serverUrls() {
		return this.serverUrl.split(",");
	}

	public Expression keyExpression() {
		return this.EXPRESSION_PARSER.parseExpression(this.keyExpression);
	}
	public StandardEvaluationContext keyContent() {
		return this.EXPRESSION_CONTEXT;
	}

	public String getValue(Object target) {
		return this.keyExpression().getValue(this.keyContent(), target, String.class);
	}

}
