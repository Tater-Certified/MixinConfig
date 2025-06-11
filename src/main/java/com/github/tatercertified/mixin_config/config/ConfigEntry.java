package com.github.tatercertified.mixin_config.config;

public record ConfigEntry(String name, boolean value) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigEntry entry) {
            return name.equals(entry.name);
        }
        return false;
    }
}
