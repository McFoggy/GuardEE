package fr.brouillard.oss.ee.fault.tolerance;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

public class JEEFaultToleranceTCKArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            String implVersion = System.getProperty("mpft.jee.version");
            if (implVersion != null && implVersion.trim().length() > 0) {
                String gav = String.format("fr.brouillard.oss.jee:portable-mpft-jee-impl:%s", implVersion);

                File[] jeeImplAndDeps = Maven.resolver().resolve(gav).withTransitivity().asFile();
                ((WebArchive) applicationArchive).addAsLibraries(jeeImplAndDeps);
            }
        }
    }
}
