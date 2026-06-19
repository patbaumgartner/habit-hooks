package io.github.patbaumgartner.habithooks.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

/**
 * Supplies the version string from the Maven-generated {@code version.properties}
 * resource bundled in the JAR.
 */
public final class VersionProvider implements IVersionProvider {

    private static final String PROPERTIES_RESOURCE =
            "io/github/patbaumgartner/habithooks/version.properties";

    @Override
    public String[] getVersion() throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        }
        String version = props.getProperty("version", "unknown");
        return new String[]{"habit-hooks " + version};
    }
}
