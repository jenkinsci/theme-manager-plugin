package io.jenkins.plugins.thememanager;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.jenkins.ui.icon.IconSpec;

/**
 * Definition of a Theme Manager
 *
 * @see ThemeManagerFactory
 */
public abstract class ThemeManagerFactoryDescriptor extends Descriptor<ThemeManagerFactory> implements IconSpec {

    public ThemeManagerFactory getInstance() {
        try {
            //noinspection deprecation
            return this.clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    /**
     * A name for a theme plugin
     *
     * <p>This should be all lower case and URL safe, i.e. 'dark', 'neo2'
     *
     * <p>The ID can be re-used inside of your plugin if you are serving multiple variations of the
     * theme, i.e. 'Dark', 'Dark (System)' could both be 'dark'.
     *
     * <p>Used in generating URLs for Jenkins to find your theme files.
     *
     * @return the theme ID.
     */
    public abstract String getThemeId();

    /**
     * A unique key for a theme plugin
     *
     * <p>This should be all lower case and URL safe, i.e. 'dark-system', 'neo2'
     *
     * <p>Used in the html dataset namespace for the theme.
     *
     * @return the theme key
     */
    public String getThemeKey() {
        return getThemeId();
    }

    /**
     * If the theme's CSS will only be selected under the '[data-theme=theme-key]' selector
     *
     * <p>It will be served on all pages even if not activated, ensure all selectors are behind the
     * dataset selector.
     *
     * <p>All new themes should be namespaced
     *
     * @return if it's namespaced.
     */
    public boolean isNamespaced() {
        return false;
    }

    /**
     * Name of the theme resource file.
     *
     * <p>Used in generating URLs for Jenkins to find your theme files.
     *
     * @return name of the theme resource file.
     */
    public String getThemeCssSuffix() {
        return "theme.css";
    }

    /**
     * Name of the theme resource file.
     *
     * <p>Used in generating URLs for Jenkins to find your theme files.
     *
     * @return name of the theme resource file.
     */
    public String getThemeJsSuffix() {
        return "theme.js";
    }

    @Override
    public String getIconClassName() {
        return "symbol-water-outline plugin-ionicons-api";
    }

    /**
     * All implementations of {@link ThemeManagerFactory} and {@link ThemeManagerFactoryDescriptor}.
     *
     * @return descriptor extension list containing all themes.
     */
    public static DescriptorExtensionList<ThemeManagerFactory, ThemeManagerFactoryDescriptor> all() {
        return Jenkins.get().getDescriptorList(ThemeManagerFactory.class);
    }
}
