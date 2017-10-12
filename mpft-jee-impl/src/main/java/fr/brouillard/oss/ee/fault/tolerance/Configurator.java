package fr.brouillard.oss.ee.fault.tolerance;

import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class Configurator {
    /**
     * Enhances given found retry configuration, with dynamic configuration if any
     * @param name the name of the retry configuration for which additional settings are being retrieved
     * @param baseConfiguration a base configuration to modify
     * @return the configuration to use
     */
    public InvocationConfiguration retry(String name, InvocationConfiguration baseConfiguration) {
        return baseConfiguration;
    }

    /**
     * Enhances given found retry configuration, with dynamic configuration if any
     * @param name the name of the timeout configuration for which additional settings are being retrieved
     * @param baseConfiguration a base configuration to modify
     * @return the configuration to use
     */
    public InvocationConfiguration timeout(String name, InvocationConfiguration baseConfiguration) {
        return baseConfiguration;
    }
}
