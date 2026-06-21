package com.patbaumgartner.habithooks.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

/**
 * Supplies the version string from the Maven-generated {@code version.properties}
 * resource bundled in the JAR.
 */
public final class VersionProvider implements IVersionProvider {

    private static final String PROPERTIES_RESOURCE = "com/patbaumgartner/habithooks/version.properties";

    private static final String UNKNOWN = "unknown";

    @Override
    public String[] getVersion() {
        return new String[] { "habit-hooks " + currentVersion() };
    }

    /**
     * Returns the raw version string from the bundled {@code version.properties}.
     * @return the version, or {@code "unknown"} when it cannot be read
     */
    public static String currentVersion() {
        Properties props = new Properties();
        try (InputStream in = VersionProvider.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        }
        catch (IOException ex) {
            return UNKNOWN;
        }
        return props.getProperty("version", UNKNOWN);
    }

}
