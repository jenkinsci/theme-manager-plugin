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

    private UserTheme userTheme;
    private ThemeManagerFactory theme;

    @DataBoundConstructor
    public ThemeUserProperty() {}

    protected Object readResolve() {
        if (theme != null && userTheme == null) {
            userTheme = UserTheme.of(theme);
            theme = null;
        }
        return this;
    }

    public UserTheme getUserTheme() {
        if (userTheme == null) {
            return UserTheme.of(new NoOpThemeManagerFactory());
        }
        return userTheme;
    }

    @DataBoundSetter
    public void setUserTheme(UserTheme userTheme) {
        this.userTheme = userTheme;
    }

    @Deprecated
    public ThemeManagerFactory getTheme() {
        if (userTheme instanceof UserTheme.StaticTheme staticTheme) {
            return staticTheme.getTheme();
        }
        if (userTheme instanceof UserTheme.SystemTheme systemTheme) {
            return systemTheme.getLight();
        }
        return new NoOpThemeManagerFactory();
    }

    @Deprecated
    @DataBoundSetter
    public void setTheme(ThemeManagerFactory theme) {
        this.userTheme = theme != null ? UserTheme.of(theme) : null;
    }

    @CheckForNull
    public static UserTheme forCurrentUserTheme() {
        final User current = User.current();
        if (current == null) {
            return null;
        }

        ThemeUserProperty property = current.getProperty(ThemeUserProperty.class);
        if (property == null || property.userTheme == null) {
            return null;
        }

        return property.getUserTheme();
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
        UserTheme currentTheme = forCurrentUserTheme();
        if (currentTheme instanceof UserTheme.StaticTheme staticTheme) {
            return staticTheme.getTheme();
        }
        if (currentTheme instanceof UserTheme.SystemTheme systemTheme) {
            return systemTheme.getLight();
        }
        return null;
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
