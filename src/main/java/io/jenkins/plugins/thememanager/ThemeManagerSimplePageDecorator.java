package io.jenkins.plugins.thememanager;

import hudson.Extension;
import jenkins.model.SimplePageDecorator;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class ThemeManagerSimplePageDecorator extends SimplePageDecorator {

    public ThemeManagerSimplePageDecorator() {
        load();
    }

    public String getHeaderHtml() {
        return ThemeManagerPageDecorator.get().getHeaderHtml();
    }

    public String getThemeKey() {
        return ThemeManagerPageDecorator.get().getThemeKey();
    }
}
