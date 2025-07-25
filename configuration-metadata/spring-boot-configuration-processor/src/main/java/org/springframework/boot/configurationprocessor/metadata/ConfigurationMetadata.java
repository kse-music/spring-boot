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

package org.springframework.boot.configurationprocessor.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType;
import org.springframework.boot.configurationprocessor.support.ConventionUtils;

/**
 * Configuration meta-data.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 1.2.0
 * @see ItemMetadata
 */
public class ConfigurationMetadata {

	private final Map<String, List<ItemMetadata>> items;

	private final Map<String, List<ItemHint>> hints;

	private final Map<String, List<ItemIgnore>> ignored;

	public ConfigurationMetadata() {
		this.items = new LinkedHashMap<>();
		this.hints = new LinkedHashMap<>();
		this.ignored = new LinkedHashMap<>();
	}

	public ConfigurationMetadata(ConfigurationMetadata metadata) {
		this.items = new LinkedHashMap<>(metadata.items);
		this.hints = new LinkedHashMap<>(metadata.hints);
		this.ignored = new LinkedHashMap<>(metadata.ignored);
	}

	/**
	 * Add item meta-data.
	 * @param itemMetadata the meta-data to add
	 */
	public void add(ItemMetadata itemMetadata) {
		add(this.items, itemMetadata.getName(), itemMetadata, false);
	}

	/**
	 * Add item meta-data if it's not already present.
	 * @param itemMetadata the meta-data to add
	 * @since 2.4.0
	 */
	public void addIfMissing(ItemMetadata itemMetadata) {
		add(this.items, itemMetadata.getName(), itemMetadata, true);
	}

	/**
	 * Add item hint.
	 * @param itemHint the item hint to add
	 */
	public void add(ItemHint itemHint) {
		add(this.hints, itemHint.getName(), itemHint, false);
	}

	/**
	 * Add item ignore.
	 * @param itemIgnore the item ignore to add
	 * @since 3.5.0
	 */
	public void add(ItemIgnore itemIgnore) {
		add(this.ignored, itemIgnore.getName(), itemIgnore, false);
	}

	/**
	 * Remove item meta-data for the given item type and name.
	 * @param itemType the item type
	 * @param name the name
	 * @since 3.5.0
	 */
	public void removeMetadata(ItemType itemType, String name) {
		List<ItemMetadata> metadata = this.items.get(name);
		if (metadata == null) {
			return;
		}
		metadata.removeIf((item) -> item.isOfItemType(itemType));
		if (metadata.isEmpty()) {
			this.items.remove(name);
		}
	}

	/**
	 * Merge the content from another {@link ConfigurationMetadata}.
	 * @param metadata the {@link ConfigurationMetadata} instance to merge
	 */
	public void merge(ConfigurationMetadata metadata) {
		for (ItemMetadata additionalItem : metadata.getItems()) {
			mergeItemMetadata(additionalItem);
		}
		for (ItemHint itemHint : metadata.getHints()) {
			add(itemHint);
		}
		for (ItemIgnore itemIgnore : metadata.getIgnored()) {
			add(itemIgnore);
		}
	}

	/**
	 * Return item meta-data.
	 * @return the items
	 */
	public List<ItemMetadata> getItems() {
		return flattenValues(this.items);
	}

	/**
	 * Return hint meta-data.
	 * @return the hints
	 */
	public List<ItemHint> getHints() {
		return flattenValues(this.hints);
	}

	/**
	 * Return ignore meta-data.
	 * @return the ignores
	 */
	public List<ItemIgnore> getIgnored() {
		return flattenValues(this.ignored);
	}

	protected void mergeItemMetadata(ItemMetadata metadata) {
		ItemMetadata matching = findMatchingItemMetadata(metadata);
		if (matching != null) {
			if (metadata.getDescription() != null) {
				matching.setDescription(metadata.getDescription());
			}
			if (metadata.getDefaultValue() != null) {
				matching.setDefaultValue(metadata.getDefaultValue());
			}
			ItemDeprecation deprecation = metadata.getDeprecation();
			ItemDeprecation matchingDeprecation = matching.getDeprecation();
			if (deprecation != null) {
				if (matchingDeprecation == null) {
					matching.setDeprecation(deprecation);
				}
				else {
					if (deprecation.getReason() != null) {
						matchingDeprecation.setReason(deprecation.getReason());
					}
					if (deprecation.getReplacement() != null) {
						matchingDeprecation.setReplacement(deprecation.getReplacement());
					}
					if (deprecation.getLevel() != null) {
						matchingDeprecation.setLevel(deprecation.getLevel());
					}
					if (deprecation.getSince() != null) {
						matchingDeprecation.setSince(deprecation.getSince());
					}
				}
			}
		}
		else {
			add(this.items, metadata.getName(), metadata, false);
		}
	}

	private <K, V> void add(Map<K, List<V>> map, K key, V value, boolean ifMissing) {
		List<V> values = map.computeIfAbsent(key, (k) -> new ArrayList<>());
		if (!ifMissing || values.isEmpty()) {
			values.add(value);
		}
	}

	private ItemMetadata findMatchingItemMetadata(ItemMetadata metadata) {
		List<ItemMetadata> candidates = this.items.get(metadata.getName());
		if (candidates == null || candidates.isEmpty()) {
			return null;
		}
		candidates = new ArrayList<>(candidates);
		candidates.removeIf((itemMetadata) -> !itemMetadata.hasSameType(metadata));
		if (candidates.size() > 1 && metadata.getType() != null) {
			candidates.removeIf((itemMetadata) -> !metadata.getType().equals(itemMetadata.getType()));
		}
		if (candidates.size() == 1) {
			return candidates.get(0);
		}
		for (ItemMetadata candidate : candidates) {
			if (nullSafeEquals(candidate.getSourceType(), metadata.getSourceType())) {
				return candidate;
			}
		}
		return null;
	}

	private boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		return o1 != null && o1.equals(o2);
	}

	public static String nestedPrefix(String prefix, String name) {
		String nestedPrefix = (prefix != null) ? prefix : "";
		String dashedName = ConventionUtils.toDashedCase(name);
		nestedPrefix += nestedPrefix.isEmpty() ? dashedName : "." + dashedName;
		return nestedPrefix;
	}

	private static <T extends Comparable<T>> List<T> flattenValues(Map<?, List<T>> map) {
		List<T> content = new ArrayList<>();
		for (List<T> values : map.values()) {
			content.addAll(values);
		}
		Collections.sort(content);
		return content;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("items: %n"));
		this.items.values().forEach((itemMetadata) -> result.append("\t").append(String.format("%s%n", itemMetadata)));
		return result.toString();
	}

}
