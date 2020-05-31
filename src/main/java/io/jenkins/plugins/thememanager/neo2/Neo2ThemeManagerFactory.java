package io.jenkins.plugins.thememanager.neo2;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import io.jenkins.plugins.thememanager.ThemeManagerFactoryDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class Neo2ThemeManagerFactory extends ThemeManagerFactory {

    private static final String THEME = "https://tobix.github.io/jenkins-neo2-theme/dist/neo-light.css";

    @DataBoundConstructor
    public Neo2ThemeManagerFactory() {
    }

    @Override
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrl(THEME)
                .build();
    }
    
    @Extension
    public static class Neo2ThemeManagerFactoryDescriptor extends ThemeManagerFactoryDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Neo2";
        }

        @Override
        public ThemeManagerFactory getInstance() {
            return new Neo2ThemeManagerFactory();
        }
    }
}
