package com.github.tatercertified.mixin_config.config;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * A compacted version of {@link ConfigEntry} that contains the class and all methods inside of it
 * @param classEntry Class ConfigEntry data, if present
 * @param methodEntries List of all method ConfigEntry data
 */
public record ContainedConfigEntry(@Nullable ConfigEntry classEntry, LinkedList<ConfigEntry> methodEntries) {
}
