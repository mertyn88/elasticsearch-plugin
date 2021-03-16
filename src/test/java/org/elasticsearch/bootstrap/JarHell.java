package org.elasticsearch.bootstrap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class JarHell {
    private JarHell() {}
    public static void checkJarHell(Consumer<String> output) throws IOException, URISyntaxException {}

    public static Set<URL> parseClassPath() { return Collections.emptySet(); }

    public static void checkJarHell(Set<URL> urls, Consumer<String> output) throws URISyntaxException, IOException {}

    public static void checkVersionFormat(String targetVersion) {}

    public static void checkJavaVersion(String resource, String targetVersion) {}

}