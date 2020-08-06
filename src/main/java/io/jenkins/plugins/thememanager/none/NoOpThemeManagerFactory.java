package io.jenkins.plugins.thememanager.none;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import io.jenkins.plugins.thememanager.ThemeManagerFactoryDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/** Disables any theming. */
@Restricted(NoExternalUse.class)
public class NoOpThemeManagerFactory extends ThemeManagerFactory {

  @DataBoundConstructor
  public NoOpThemeManagerFactory() {}

  @Override
  public Theme getTheme() {
    return Theme.builder().build();
  }

  @Extension
  public static class NoOpThemeManagerFactoryDescriptor extends ThemeManagerFactoryDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Default";
    }

    @Override
    public ThemeManagerFactory getInstance() {
      return new NoOpThemeManagerFactory();
    }

    @Override
    public String getThemeId() {
      return "none";
    }
  }
}
