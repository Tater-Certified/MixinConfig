package com.github.tatercertified.mixin_config.utils;

import com.github.tatercertified.mixin_config.MixinConfig;
import com.github.tatercertified.mixin_config.annotations.Config;

import java.util.HashSet;

public class NotifyingHashSet <T> extends HashSet<T> {
    private final String message;

    /**
     * A HashSet that prints out a message each time an element is added
     * @param message String message to print using "{}" to insert the {@link Config#name()}
     */
    public NotifyingHashSet(String message) {
        this.message = message;
    }

    @Override
    public boolean add(T t) {
        boolean output = super.add(t);
        if (output && MixinConfig.VERBOSE) {
            MixinConfig.LOGGER.info(message, t);
        }
        return output;
    }
}
