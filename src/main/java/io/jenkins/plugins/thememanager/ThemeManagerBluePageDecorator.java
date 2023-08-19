package io.jenkins.plugins.thememanager;

import hudson.Extension;
import io.jenkins.blueocean.BluePageDecorator;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension(optional = true)
@Restricted(NoExternalUse.class)
public class ThemeManagerBluePageDecorator extends BluePageDecorator {

    public String getHeaderHtml() {
        ThemeManagerPageDecorator themeManagerPageDecorator = ThemeManagerPageDecorator.get();
        Theme theme = themeManagerPageDecorator.findTheme();

        if (theme.isBlueOceanCompatible()) {
            return themeManagerPageDecorator.getHeaderHtml();
        }
        return null;
    }

    public String getThemeKey() {
        ThemeManagerPageDecorator themeManagerPageDecorator = ThemeManagerPageDecorator.get();
        Theme theme = themeManagerPageDecorator.findTheme();

        if (theme.isBlueOceanCompatible()) {
            return themeManagerPageDecorator.getThemeKey();
        }
        return null;
    }

    @SuppressWarnings("unused") // called by jelly
    public boolean isRespectSystemAppearance() {
        ThemeManagerPageDecorator themeManagerPageDecorator = ThemeManagerPageDecorator.get();
        Theme theme = themeManagerPageDecorator.findTheme();

        if (theme.isBlueOceanCompatible()) {
            return themeManagerPageDecorator.isRespectSystemAppearance();
        }
        return false;
    }
}
