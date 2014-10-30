package org.jboss.as.selfmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * This is the barebone test example that tests subsystem
 * It does same things that {@link SubsystemParsingTestCase} does but most of internals are already done in AbstractSubsystemBaseTest
 * If you need more control over what happens in tests look at  {@link SubsystemParsingTestCase}
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(SelfmonitorExtension.SUBSYSTEM_NAME, new SelfmonitorExtension());
    }

    @Override
    protected String getSubsystemXml() {
        
        File configFile = null;
        try {
            configFile = new File(getClass().getClassLoader().getResource("selfmonitor-subsystem.xml").toURI());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Configuration file was not found.", ex);
        }
        StringBuilder configBuilder = new StringBuilder();
        try{
            InputStreamReader isr = new FileReader(configFile);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                configBuilder.append(line.trim());
                line = br.readLine();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Error occurred while loading subsystem configuration.", ex);
        }
        return configBuilder.toString();
    }

}
