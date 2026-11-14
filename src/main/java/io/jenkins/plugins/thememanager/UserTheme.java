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
public sealed interface UserTheme extends Describable<UserTheme>, ExtensionPoint {

    static StaticTheme of(ThemeManagerFactory theme) {
        return new StaticTheme(theme);
    }

    static SystemTheme of(ThemeManagerFactory light, ThemeManagerFactory dark) {
        return new SystemTheme(light, dark);
    }

    @Override
    default Descriptor<UserTheme> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    final class StaticTheme implements UserTheme {
        private final ThemeManagerFactory theme;

        @DataBoundConstructor
        public StaticTheme(ThemeManagerFactory theme) {
            this.theme = Objects.requireNonNullElseGet(theme, NoOpThemeManagerFactory::new);
        }

        public ThemeManagerFactory getTheme() {
            return theme;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StaticTheme that)) return false;
            return Objects.equals(theme, that.theme);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(theme);
        }

        @Extension
        @Symbol("static")
        public static class DescriptorImpl extends Descriptor<UserTheme> {
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

    final class SystemTheme implements UserTheme {
        private final ThemeManagerFactory light;
        private final ThemeManagerFactory dark;

        @DataBoundConstructor
        public SystemTheme(ThemeManagerFactory light, ThemeManagerFactory dark) {
            this.light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
            this.dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
        }

        public ThemeManagerFactory getLight() {
            return light;
        }

        public ThemeManagerFactory getDark() {
            return dark;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SystemTheme that)) return false;
            return Objects.equals(light, that.light) && Objects.equals(dark, that.dark);
        }

        @Override
        public int hashCode() {
            return Objects.hash(light, dark);
        }

        @Extension
        @Symbol("system")
        public static class DescriptorImpl extends Descriptor<UserTheme> {
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
