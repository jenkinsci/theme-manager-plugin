package io.jenkins.plugins.thememanager;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Pluggable ability to add additional built-in themes. The descriptor's display name will be used
 * for the UI label
 *
 * <p>Since the user can configure this class, you must have a {@link DataBoundConstructor}.
 *
 * @see ThemeManagerPageDecorator
 * @see ThemeManagerPageDecorator
 */
@Restricted(Beta.class)
public abstract class ThemeManagerFactory extends AbstractDescribableImpl<ThemeManagerFactory>
    implements ExtensionPoint {

  public abstract Theme getTheme();

  /**
   * Expected CSS URL assuming your CSS file is named after your theme. You can also change the CSS
   * file name by overriding: {@link ThemeManagerFactoryDescriptor#getThemeCssSuffix()}
   *
   * <p>See {@link #toAssetUrl(String)} for arbitrary files
   *
   * @return CSS url in the form '$JENKINS_URL/theme-$themeId/theme.css.
   */
  public String getCssUrl() {
    ThemeManagerFactoryDescriptor descriptor = getDescriptor();
    return toAssetUrl(descriptor.getThemeCssSuffix());
  }

  /**
   * Arbitrary asset URL. Useful if you want additional css or js files
   *
   * @param asset additional-stylesheet.css
   * @return Asset url in the form '$JENKINS_URL/theme-$themeId/$asset-parameter
   */
  public String toAssetUrl(String asset) {
    ThemeManagerFactoryDescriptor descriptor = getDescriptor();
    return Jenkins.get().getRootUrlFromRequest() + "theme-" + descriptor.getThemeId() + "/" + asset;
  }

  /**
   * Expected JavaScript URL assuming your JavaScript file is named after your theme. You can also
   * change the CSS file name by overriding: {@link
   * ThemeManagerFactoryDescriptor#getThemeJsSuffix()}
   *
   * <p>See {@link #toAssetUrl(String)} for arbitrary files
   *
   * @return JavaScript url in the form '$JENKINS_URL/theme-$themeId/theme.js.
   */
  public String getJavaScriptUrl() {
    ThemeManagerFactoryDescriptor descriptor = getDescriptor();
    return toAssetUrl(descriptor.getThemeJsSuffix());
  }

  @Override
  public ThemeManagerFactoryDescriptor getDescriptor() {
    return (ThemeManagerFactoryDescriptor) super.getDescriptor();
  }
}
