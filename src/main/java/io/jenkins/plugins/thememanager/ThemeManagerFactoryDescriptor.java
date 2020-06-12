package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ThemeManagerFactoryDescriptor extends Descriptor<ThemeManagerFactory> {

  @NonNull
  public abstract ThemeManagerFactory getInstance();

  @NonNull
  public abstract String getThemeId();

  @NonNull
  public String getThemeCssSuffix() {
    return "theme.css";
  };

  @NonNull
  public String getThemeJsSuffix() {
    return "theme.js";
  };

  public static DescriptorExtensionList<ThemeManagerFactory, ThemeManagerFactoryDescriptor> all() {
    return Jenkins.get().getDescriptorList(ThemeManagerFactory.class);
  }
}
