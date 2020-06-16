package io.jenkins.plugins.thememanager;

import hudson.Extension;
import io.jenkins.blueocean.BluePageDecorator;
import jenkins.model.SimplePageDecorator;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension(optional = true)
@Restricted(NoExternalUse.class)
public class ThemeManagerBluePageDecorator extends BluePageDecorator {

  public String getHeaderHtml() {
    return ThemeManagerPageDecorator.get().getHeaderHtml();
  }
}
