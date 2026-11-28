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

    private ThemeSelection themeSelection;
    // Retained for backwards compatibility with legacy <theme> XStream XML configs
    private ThemeManagerFactory theme;

    @DataBoundConstructor
    public ThemeUserProperty() {}

    // Migrates legacy <theme> XStream XML entries into ThemeSelection
    protected Object readResolve() {
        if (theme != null && themeSelection == null) {
            themeSelection = ThemeSelection.of(theme);
            theme = null;
        }
        return this;
    }

    public ThemeSelection getTheme() {
        if (themeSelection == null) {
            return ThemeSelection.of(new NoOpThemeManagerFactory());
        }
        return themeSelection;
    }

    @DataBoundSetter
    public void setTheme(ThemeSelection theme) {
        this.themeSelection = theme;
    }

    @CheckForNull
    public static ThemeSelection forCurrentUser() {
        final User current = User.current();
        if (current == null) {
            return null;
        }

        ThemeUserProperty property = current.getProperty(ThemeUserProperty.class);
        if (property == null || property.themeSelection == null) {
            return null;
        }

        return property.getTheme();
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
