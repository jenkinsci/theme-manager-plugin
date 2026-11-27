package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.model.userproperty.UserPropertyCategory;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Restricted(NoExternalUse.class)
public class ThemeUserProperty extends UserProperty {

    private JenkinsTheme jenkinsTheme;
    private ThemeManagerFactory theme;

    @DataBoundConstructor
    public ThemeUserProperty() {}

    // Migrates legacy <theme> XStream XML entries into JenkinsTheme
    protected Object readResolve() {
        if (theme != null && jenkinsTheme == null) {
            jenkinsTheme = new JenkinsTheme.StaticTheme(theme);
            theme = null;
        }
        return this;
    }

    public JenkinsTheme getTheme() {
        if (jenkinsTheme == null) {
            return new JenkinsTheme.StaticTheme(new NoOpThemeManagerFactory());
        }
        return jenkinsTheme;
    }

    @DataBoundSetter
    public void setTheme(JenkinsTheme theme) {
        this.jenkinsTheme = theme;
    }

    @Deprecated
    public void setTheme(ThemeManagerFactory theme) {
        this.jenkinsTheme = theme != null ? new JenkinsTheme.StaticTheme(theme) : null;
    }

    @CheckForNull
    public static JenkinsTheme forCurrentUserTheme() {
        final User current = User.current();
        if (current == null) {
            return null;
        }

        ThemeUserProperty property = current.getProperty(ThemeUserProperty.class);
        if (property == null || property.jenkinsTheme == null) {
            return null;
        }

        return property.getTheme();
    }

    @Deprecated
    @CheckForNull
    public static Theme forCurrentUser() {
        ThemeManagerFactory factory = forCurrentUserFactory();
        if (factory == null) {
            return null;
        }
        return factory.getTheme();
    }

    @Deprecated
    @CheckForNull
    public static ThemeManagerFactory forCurrentUserFactory() {
        return switch (forCurrentUserTheme()) {
            case null -> null;
            case JenkinsTheme.StaticTheme st -> st.getTheme();
            case JenkinsTheme.SystemTheme sy -> sy.getLight();
        };
    }

    @Extension(ordinal = Integer.MAX_VALUE)
    public static class ThemeUserPropertyDescriptor extends UserPropertyDescriptor {

        @Override
        public boolean isEnabled() {
            return !ThemeManagerPageDecorator.get().isDisableUserThemes();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Theme";
        }

        @Override
        public UserProperty newInstance(User user) {
            return new ThemeUserProperty();
        }

        @Override
        public @NonNull UserPropertyCategory getUserPropertyCategory() {
            return UserPropertyCategory.get(UserPropertyCategory.Appearance.class);
        }
    }
}
