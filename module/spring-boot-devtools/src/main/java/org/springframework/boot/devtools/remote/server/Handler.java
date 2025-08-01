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

package org.springframework.boot.devtools.remote.server;

import java.io.IOException;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

/**
 * A single handler that is able to process an incoming remote server request.
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
@FunctionalInterface
public interface Handler {

	/**
	 * Handle the request.
	 * @param request the request
	 * @param response the response
	 * @throws IOException in case of I/O errors
	 */
	void handle(ServerHttpRequest request, ServerHttpResponse response) throws IOException;

}
