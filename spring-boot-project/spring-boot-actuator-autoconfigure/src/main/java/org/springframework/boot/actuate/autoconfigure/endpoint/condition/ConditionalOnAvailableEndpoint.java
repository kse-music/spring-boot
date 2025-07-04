/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.endpoint.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.Environment;

/**
 * {@link Conditional @Conditional} that checks whether an endpoint is available. An
 * endpoint is considered available if it is both enabled and exposed on the specified
 * technologies.
 * <p>
 * Matches access according to the endpoint's specific {@link Environment} property,
 * falling back to {@code management.endpoints.default-access} or failing that
 * {@link Endpoint#defaultAccess()}.
 * <p>
 * Matches exposure according to any of the {@code management.endpoints.web.exposure.<id>}
 * or {@code management.endpoints.jmx.exposure.<id>} specific properties or failing that
 * to whether any {@link EndpointExposureOutcomeContributor} exposes the endpoint.
 * <p>
 * Both enablement and exposure conditions should match for the endpoint to be considered
 * available.
 * <p>
 * When placed on a {@code @Bean} method, the endpoint defaults to the return type of the
 * factory method:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint
 *     &#064;Bean
 *     public MyEndpoint myEndpoint() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * It is also possible to use the same mechanism for extensions:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint
 *     &#064;Bean
 *     public MyEndpointWebExtension myEndpointWebExtension() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * In the sample above, {@code MyEndpointWebExtension} will be created if the endpoint is
 * available as defined by the rules above. {@code MyEndpointWebExtension} must be a
 * regular extension that refers to an endpoint, something like:
 *
 * <pre class="code">
 * &#064;EndpointWebExtension(endpoint = MyEndpoint.class)
 * public class MyEndpointWebExtension {
 *
 * }</pre>
 * <p>
 * Alternatively, the target endpoint can be manually specified for components that should
 * only be created when a given endpoint is available:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint(endpoint = MyEndpoint.class)
 *     &#064;Bean
 *     public MyComponent myComponent() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 2.2.0
 * @see Endpoint
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
@Conditional(OnAvailableEndpointCondition.class)
public @interface ConditionalOnAvailableEndpoint {

	/**
	 * Alias for {@link #endpoint()}.
	 * @return the endpoint type to check
	 * @since 3.4.0
	 */
	@AliasFor(attribute = "endpoint")
	Class<?> value() default Void.class;

	/**
	 * The endpoint type that should be checked. Inferred when the return type of the
	 * {@code @Bean} method is either an {@link Endpoint @Endpoint} or an
	 * {@link EndpointExtension @EndpointExtension}.
	 * @return the endpoint type to check
	 */
	@AliasFor(attribute = "value")
	Class<?> endpoint() default Void.class;

	/**
	 * Technologies to check the exposure of the endpoint on while considering it to be
	 * available.
	 * @return the technologies to check
	 * @since 2.6.0
	 */
	EndpointExposure[] exposure() default {};

}
