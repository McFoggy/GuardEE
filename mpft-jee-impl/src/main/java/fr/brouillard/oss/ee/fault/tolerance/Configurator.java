package fr.brouillard.oss.ee.fault.tolerance;

import fr.brouillard.oss.ee.fault.tolerance.model.RetryConfiguration;

public class Configurator {
    /**
     * Enhances given found retry configuration, with dynamic configuration if any
     * @param name the name of the retry configuration for which additional settings are being retrieved
     * @param baseConfiguration a base configuration to modify
     * @return the retry configuration to use
     */
    public RetryConfiguration retry(String name, RetryConfiguration baseConfiguration) {
        return baseConfiguration;
    }
}
