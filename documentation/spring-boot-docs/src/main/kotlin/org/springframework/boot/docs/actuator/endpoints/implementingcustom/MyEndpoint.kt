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

package org.springframework.boot.docs.actuator.endpoints.implementingcustom

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation

@Endpoint(id = "custom")
@Suppress("UNUSED_PARAMETER")
class MyEndpoint {

	// tag::read[]
	@ReadOperation
	fun getData(): CustomData {
		return CustomData("test", 5)
	}
	// end::read[]

	// tag::write[]
	@WriteOperation
	fun updateData(name: String?, counter: Int) {
		// injects "test" and 42
	}
	// end::write[]
}

