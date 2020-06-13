package io.jenkins.plugins.thememanager;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.PageDecorator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol("themeManager")
@Restricted(NoExternalUse.class)
public class ThemeManagerPageDecorator extends PageDecorator {

  private ThemeManagerFactory theme;
  private boolean disableUserThemes;

  public ThemeManagerPageDecorator() {
    load();
  }

  public static ThemeManagerPageDecorator get() {
    return ExtensionList.lookupSingleton(ThemeManagerPageDecorator.class);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) {
    req.bindJSON(this, formData);
    save();
    return true;
  }

  @DataBoundSetter
  public void setTheme(ThemeManagerFactory theme) {
    this.theme = theme;
  }

  public ThemeManagerFactory getTheme() {
    return theme;
  }

  public boolean isDisableUserThemes() {
    return disableUserThemes;
  }

  @DataBoundSetter
  public void setDisableUserThemes(boolean disableUserThemes) {
    this.disableUserThemes = disableUserThemes;
  }

  /** Get the complete header HTML for all configured theme elements. */
  public String getHeaderHtml() {
    if (!disableUserThemes) {
      Theme theme = ThemeUserProperty.forCurrentUser();
      if (theme != null) {
        boolean injectCss = shouldInjectCss();
        Set<String> data = new LinkedHashSet<>(theme.generateHeaderElements(injectCss));
        return StringUtils.join(data, "\n");
      }
    }

    if (theme != null) {
      boolean injectCss = shouldInjectCss();
      Set<String> data = new LinkedHashSet<>(theme.getTheme().generateHeaderElements(injectCss));
      return StringUtils.join(data, "\n");
    }

    return null;
  }
  
  /**
   * Filter to only inject CSS into "normal" Jenkins pages. Some plugins replace the whole layout of
   * Jenkins and we don't want to disturb them.
   *
   * @return true if it is okay to inject CSS
   */
  public boolean shouldInjectCss() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req == null) {
      return false;
    }

    List<Ancestor> ancestors = req.getAncestors();
    if (ancestors == null || ancestors.size() == 0) {
      return false;
    }

    Ancestor a = ancestors.get(ancestors.size() - 1);
    Object o = a.getObject();

    // We don't want to style the build-monitor-plugin
    return !o.getClass().getName().startsWith("com.smartcodeltd.jenkinsci.plugins.buildmonitor");
  }

}
