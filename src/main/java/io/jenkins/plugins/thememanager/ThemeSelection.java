package io.jenkins.plugins.thememanager;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.kohsuke.stapler.DataBoundConstructor;
import tools.jackson.databind.ObjectMapper;

public final class ThemeSelection {

    private static final String JSON_HTML = "<script id=\"theme-manager-{0}\" type=\"application/json\">{1}</script>";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ThemeManagerFactory light;
    private final ThemeManagerFactory dark;

    @DataBoundConstructor
    public ThemeSelection(ThemeManagerFactory light, ThemeManagerFactory dark) {
        this.light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
        this.dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
    }

    private Object readResolve() {
        return new ThemeSelection(light, dark);
    }

    public static ThemeSelection of(ThemeManagerFactory theme) {
        return new ThemeSelection(theme, theme);
    }

    public ThemeManagerFactory getLight() {
        return light;
    }

    public ThemeManagerFactory getDark() {
        return dark;
    }

    Set<String> generateHeaderElements(boolean injectCss) {
        Set<String> headerElements = new LinkedHashSet<>();
        if (!light.getDescriptor().isNamespaced()) {
            headerElements.addAll(light.getTheme().generateHeaderElements(injectCss, "(prefers-color-scheme: light)"));
        }
        if (!dark.getDescriptor().isNamespaced()) {
            headerElements.addAll(dark.getTheme().generateHeaderElements(injectCss, "(prefers-color-scheme: dark)"));
        }
        return headerElements;
    }

    String generateTheme() {
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("light", light.getDescriptor().getThemeKey());
        keys.put("dark", dark.getDescriptor().getThemeKey());
        return generateJsonScript("theme", keys);
    }

    String generateProperties() {
        Map<String, Map<String, String>> combined = new LinkedHashMap<>();
        combined.put("light", light.getTheme().getProperties());
        combined.put("dark", dark.getTheme().getProperties());
        return generateJsonScript("properties", combined);
    }

    private static String generateJsonScript(String id, Object value) {
        return MessageFormat.format(JSON_HTML, id, OBJECT_MAPPER.writeValueAsString(value));
    }
}
