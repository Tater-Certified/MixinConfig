package com.github.tatercertified.testmod;

import com.github.tatercertified.mixin_config.validation.Validator;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Testmod implements ModInitializer {
    public static final Set<String> FIRED = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("TESTMOD");
    @Override
    public void onInitialize() {
    }

    public static void runTests() {
        LOGGER.info("Running Tests");
        int errors = 0;
        for (String name : FIRED) {
            if (Validator.isDisabled(name)) {
                LOGGER.error("DID NOT DISABLE: {}", name);
                errors++;
            }
        }
        if (errors == 0) {
            LOGGER.info("Tests Passed");
        } else {
            LOGGER.warn("Tests Failed");
        }
    }
}
