package io.jenkins.plugins.thememanager.dark;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import io.jenkins.plugins.thememanager.ThemeManagerFactoryDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class DarkThemeManagerFactory extends ThemeManagerFactory {

    private static final String THEME = "https://cdn.jsdelivr.net/gh/jenkinsci/dark-theme@e8d2b0b883a0711da84bce9d04ae89e8e8530e78/theme.css";

    @DataBoundConstructor
    public DarkThemeManagerFactory() {
    }

    @Override
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrl(THEME)
                .build();
    }
    
    @Extension
    public static class DarkThemeManagerFactoryDescriptor extends ThemeManagerFactoryDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Dark";
        }

        @Override
        public ThemeManagerFactory getInstance() {
            return new DarkThemeManagerFactory();
        }
    }
}
