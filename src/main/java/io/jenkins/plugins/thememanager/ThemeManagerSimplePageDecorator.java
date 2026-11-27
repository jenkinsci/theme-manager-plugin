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

    @SuppressWarnings("unused") // called by jelly
    public String getLightThemeKey() {
        return ThemeManagerPageDecorator.get().getLightThemeKey();
    }

    @SuppressWarnings("unused") // called by jelly
    public String getDarkThemeKey() {
        return ThemeManagerPageDecorator.get().getDarkThemeKey();
    }

    public String getThemeKey() {
        return ThemeManagerPageDecorator.get().getThemeKey();
    }
}
