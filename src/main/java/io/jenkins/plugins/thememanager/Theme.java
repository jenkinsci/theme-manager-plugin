package io.jenkins.plugins.thememanager;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Holds URLs to CSS and JavaScript files.
 *
 * <p>Initialise it with {@link Theme#builder()}
 */
public class Theme {

  private static final String JS_HTML = "<script type=\"text/javascript\" src=\"{0}\"></script>";
  private static final String CSS_HTML =
      "<link type=\"text/css\" rel=\"stylesheet\" href=\"{0}\"/>";

  private final List<String> cssUrls;
  private final List<String> javascriptUrls;
  private final boolean blueOceanCompatible;
  private final Map<String, String> properties;

  private Theme(List<String> cssUrls, List<String> javascriptUrls, boolean blueOceanCompatible, Map<String, String> properties) {
    this.cssUrls = cssUrls;
    this.javascriptUrls = javascriptUrls;
    this.blueOceanCompatible = blueOceanCompatible;
    this.properties = properties;
  }

  @Restricted(NoExternalUse.class)
  Set<String> generateHeaderElements(boolean injectCss) {
    Set<String> headerElements = new HashSet<>();

    if (injectCss) {
      for (String cssUrl : cssUrls) {
        headerElements.add(MessageFormat.format(CSS_HTML, cssUrl));
      }
    }

    for (String javascriptUrl : javascriptUrls) {
      headerElements.add(MessageFormat.format(JS_HTML, javascriptUrl));
    }

    return headerElements;
  }

  /**
   * See {@link Builder#withCssUrl(String)}
   *
   * @return list of CSS URLs for the theme.
   */
  public List<String> getCssUrls() {
    return cssUrls;
  }

  /**
   * See {@link Builder#withJavascriptUrl(String)}.
   *
   * @return list of JavaScript URLs for the theme.
   */
  public List<String> getJavascriptUrls() {
    return javascriptUrls;
  }

  /**
   * Whether the theme should be served on Blue Ocean.
   *
   * @return if the theme is compatible with blue ocean.
   */
  public boolean isBlueOceanCompatible() {
    return blueOceanCompatible;
  }

  /**
   * Additional information that theme authors can provide to influence other plugins
   *
   * e.g. the Prism API plugin can read properties and use a default theme based on this information.
   * @param artifactId the plugin to retrieve the properties for
   * @return the properties associated with the plugin requested
   */
  public List<String> getProperties(String artifactId) {
    return properties.entrySet().stream()
            .filter(k -> k.getKey().startsWith(artifactId + ":"))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
  }

  /**
   * Additional information that theme authors can provide to influence other plugins
   *
   * e.g. the Prism API plugin can read properties and use a default theme based on this information.
   * @param artifactId the plugin to retrieve the properties for
   * @param propertyName the property to retrieve
   * @return the properties associated with the plugin requested
   */
  public Optional<String> getProperty(String artifactId, String propertyName) {
    return Optional.ofNullable(properties.get(artifactId + ":" + propertyName));
  }

  /**
   * Constructs the builder for the theme.
   *
   * @return an empty builder for building the theme.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for creating a theme. */
  public static class Builder {
    private List<String> cssUrls = emptyList();
    private List<String> javascriptUrls = emptyList();
    private boolean blueOceanCompatible = false;
    private final Map<String, String> properties = new HashMap<>();

    Builder() {}

    /**
     * A URL to a CSS file, this can be served by Jenkins or remote.
     *
     * <p>The URL must be absolute (i.e. not just contain the path)
     *
     * <p>It is recommended that you serve the CSS file with Jenkins so that users update the theme
     * by updating the plugin.
     *
     * <p>There is a convenience method {@link ThemeManagerFactory#getCssUrl()}, which will
     * determine the full url if you follow the convention: {@code
     * $JENKINS_URL/theme-$themeId/theme.css}.
     *
     * <p>The {@code theme.css} suffix can be changed by overriding {@link
     * ThemeManagerFactoryDescriptor#getThemeCssSuffix()}.
     *
     * @param cssUrl url to a CSS file that you want loaded.
     * @return the current builder with a CSS URL added to it.
     */
    public Builder withCssUrl(String cssUrl) {
      this.cssUrls = singletonList(cssUrl);
      return this;
    }

    /**
     * See {@link #withCssUrl(String)}
     *
     * @param cssUrls a list of urls to CSS files that you want loaded.
     * @return the current builder with CSS URLs added to it.
     */
    public Builder withCssUrls(List<String> cssUrls) {
      this.cssUrls = cssUrls;
      return this;
    }

    /**
     * Enables the theme on BlueOcean
     *
     * @return the current builder with BlueOcean enabled.
     */
    public Builder enableOnBlueOcean() {
      this.blueOceanCompatible = true;
      return this;
    }

    /**
     * Disables the theme on BlueOcean
     *
     * @return the current builder with BlueOcean disabled.
     */
    public Builder disableOnBlueOcean() {
      this.blueOceanCompatible = false;
      return this;
    }

    /**
     * Properties are a way a theme author can provide extra information to plugins.
     * e.g. the Prism API plugin can read properties and use a default theme based on this information.
     *
     * @param pluginId artifact ID of the plugin the property is associated with
     * @param name property name this will be namespaced with the artifactId automatically
     * @param value the property value
     * @return the current builder with the new property
     */
    public Builder withProperty(String pluginId, String name, String value) {
      this.properties.put(pluginId + ":" + name, value);
      return this;
    }

    /**
     * A URL to a JavaScript file, this can be served by Jenkins or remote.
     *
     * <p>The URL must be absolute (i.e. not just contain the path)
     *
     * <p>It is recommended that you serve the JavaScript file with Jenkins so that users update the
     * theme by updating the plugin.
     *
     * <p>There is a convenience method {@link ThemeManagerFactory#getJavaScriptUrl()}, which will
     * determine the full url if you follow the convention: {@code
     * $JENKINS_URL/theme-$themeId/theme.js}.
     *
     * <p>The {@code theme.css} suffix can be changed by overriding {@link
     * ThemeManagerFactoryDescriptor#getThemeJsSuffix()}.
     *
     * @param javascriptUrl url to a JavaScript file that you want loaded.
     * @return the current builder with a JavaScript URL added to it.
     */
    public Builder withJavascriptUrl(String javascriptUrl) {
      this.javascriptUrls = singletonList(javascriptUrl);
      return this;
    }

    /**
     * See {@link #withJavascriptUrl(String)}.
     *
     * @param javascriptUrls a list of urls to JavaScript files that you want loaded.
     * @return the current builder with JavaScript URLs added to it.
     */
    public Builder withJavascriptUrls(List<String> javascriptUrls) {
      this.javascriptUrls = javascriptUrls;
      return this;
    }

    /**
     * Constructs the theme
     *
     * @return the theme.
     */
    public Theme build() {
      return new Theme(cssUrls, javascriptUrls, blueOceanCompatible, properties);
    }
  }
}
