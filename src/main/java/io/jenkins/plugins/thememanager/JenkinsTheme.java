package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/// Designates the choice of themes the user has available.
/// It is either going to be a single theme, or if they are following the system then a light/dark mode theme
public sealed interface JenkinsTheme extends Describable<JenkinsTheme>, ExtensionPoint {

    @Override
    @SuppressWarnings("unchecked")
    default Descriptor<JenkinsTheme> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    record StaticTheme(ThemeManagerFactory theme) implements JenkinsTheme {
        @DataBoundConstructor
        public StaticTheme {
            theme = Objects.requireNonNullElseGet(theme, NoOpThemeManagerFactory::new);
        }

        public ThemeManagerFactory getTheme() {
            return theme;
        }

        @Extension
        @Symbol("static")
        public static class DescriptorImpl extends Descriptor<JenkinsTheme> {
            public DescriptorImpl() {
                super(StaticTheme.class);
            }

            @NonNull
            @Override
            public String getDisplayName() {
                return "Static";
            }
        }
    }

    record SystemTheme(ThemeManagerFactory light, ThemeManagerFactory dark) implements JenkinsTheme {
        @DataBoundConstructor
        public SystemTheme {
            light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
            dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
        }

        public ThemeManagerFactory getLight() {
            return light;
        }

        public ThemeManagerFactory getDark() {
            return dark;
        }

        @Extension
        @Symbol("system")
        public static class DescriptorImpl extends Descriptor<JenkinsTheme> {
            public DescriptorImpl() {
                super(SystemTheme.class);
            }

            @NonNull
            @Override
            public String getDisplayName() {
                return "System";
            }
        }
    }
}
