package io.jenkins.plugins.thememanager;

import hudson.Extension;
import jenkins.model.SimplePageDecorator;

@Extension
public class ThemeManagerSimplePageDecorator extends SimplePageDecorator {

  public ThemeManagerSimplePageDecorator() {
    load();
  }

  public String getHeaderHtml() {
    return ThemeManagerPageDecorator.get().getHeaderHtml();
  }
}
