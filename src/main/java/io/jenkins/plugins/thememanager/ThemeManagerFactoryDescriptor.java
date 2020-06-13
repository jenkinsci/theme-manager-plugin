package io.jenkins.plugins.thememanager;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * Definition of a Theme Manager
 *
 * @see ThemeManagerFactory
 */
public abstract class ThemeManagerFactoryDescriptor extends Descriptor<ThemeManagerFactory> {

  /**
   * Creates an instance of a {@link ThemeManagerFactory}.
   *
   * @return an instance of {@link ThemeManagerFactory}
   */
  public abstract ThemeManagerFactory getInstance();

  /**
   * A unique name for a theme plugin
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

  /**
   * All implementations of {@link ThemeManagerFactory} and {@link ThemeManagerFactoryDescriptor}.
   *
   * @return descriptor extension list containing all themes.
   */
  public static DescriptorExtensionList<ThemeManagerFactory, ThemeManagerFactoryDescriptor> all() {
    return Jenkins.get().getDescriptorList(ThemeManagerFactory.class);
  }
}
