package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.PageDecorator;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.util.HttpResponses;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol("themeManager")
public class ThemeManagerPageDecorator extends PageDecorator {

    private UserTheme userTheme;
    private ThemeManagerFactory theme;
    private boolean disableUserThemes;

    public ThemeManagerPageDecorator() {
        load();
    }

    protected Object readResolve() {
        if (theme != null && userTheme == null) {
            userTheme = UserTheme.of(theme);
            theme = null;
        }
        return this;
    }

    public static ThemeManagerPageDecorator get() {
        return ExtensionList.lookupSingleton(ThemeManagerPageDecorator.class);
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject formData) {
        req.bindJSON(this, formData);
        save();
        return true;
    }

    @NonNull
    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(AppearanceCategory.class);
    }

    @DataBoundSetter
    public void setUserTheme(UserTheme userTheme) {
        this.userTheme = userTheme;
    }

    public UserTheme getUserTheme() {
        if (userTheme == null) {
            return UserTheme.of(new NoOpThemeManagerFactory());
        }
        return userTheme;
    }

    @Deprecated
    @DataBoundSetter
    public void setTheme(ThemeManagerFactory theme) {
        this.userTheme = theme != null ? UserTheme.of(theme) : null;
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

    public boolean isDisableUserThemes() {
        return disableUserThemes;
    }

    @DataBoundSetter
    public void setDisableUserThemes(boolean disableUserThemes) {
        this.disableUserThemes = disableUserThemes;
    }

    @NonNull
    public UserTheme findUserTheme() {
        if (!disableUserThemes) {
            UserTheme currentUserTheme = ThemeUserProperty.forCurrentUserTheme();
            if (currentUserTheme != null) {
                return currentUserTheme;
            }
        }

        if (userTheme != null) {
            return userTheme;
        }
        return UserTheme.of(new NoOpThemeManagerFactory());
    }

    /**
     * Finds the active theme. Checks User and then global theme.
     *
     * @return the active theme, or a no-op theme if not selected
     */
    @Deprecated
    @NonNull
    public Theme findTheme() {
        return findThemeFactory().getTheme();
    }

    @Deprecated
    @NonNull
    public ThemeManagerFactory findThemeFactory() {
        UserTheme activeUserTheme = findUserTheme();
        if (activeUserTheme instanceof UserTheme.StaticTheme staticTheme) {
            return staticTheme.getTheme();
        }
        if (activeUserTheme instanceof UserTheme.SystemTheme systemTheme) {
            return systemTheme.getLight();
        }
        return new NoOpThemeManagerFactory();
    }

    public boolean shouldShowAccountThemePicker() {
        return User.current() != null && !isDisableUserThemes();
    }

    /** Get the complete header HTML for all configured theme elements. */
    public String getHeaderHtml() {
        boolean injectCss = shouldInjectCss();
        Set<String> namespacedThemes = ThemeManagerFactoryDescriptor.all().stream()
                .filter(ThemeManagerFactoryDescriptor::isNamespaced)
                .map(desc -> desc.getInstance().getTheme().generateHeaderElements(injectCss))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        ThemeManagerFactory themeManagerFactory = findThemeFactory();
        namespacedThemes.add(themeManagerFactory.getTheme().generateProperties());

        if (!themeManagerFactory.getDescriptor().isNamespaced()) {
            Set<String> data =
                    new LinkedHashSet<>(themeManagerFactory.getTheme().generateHeaderElements(injectCss));
            data.addAll(namespacedThemes);
            return String.join("\n", data);
        }

        return String.join("\n", namespacedThemes);
    }

    @SuppressWarnings("unused") // called by jelly
    public String getThemeKey() {
        ThemeManagerFactory themeFactory = findThemeFactory();

        return themeFactory.getDescriptor().getThemeKey();
    }

    @SuppressWarnings("unused") // called by jelly
    public boolean isRespectSystemAppearance() {
        return findUserTheme() instanceof UserTheme.SystemTheme;
    }

    /**
     * Filter to only inject CSS into "normal" Jenkins pages. Some plugins replace the whole layout of
     * Jenkins and we don't want to disturb them.
     *
     * @return true if it is okay to inject CSS
     */
    public boolean shouldInjectCss() {
        StaplerRequest2 req = Stapler.getCurrentRequest2();
        if (req == null) {
            return false;
        }

        List<Ancestor> ancestors = req.getAncestors();
        return ancestors != null && !ancestors.isEmpty();
    }

    @Extension
    public static class ThemeAction implements RootAction {
        public String getUrlName() {
            return "theme";
        }

        public String getDisplayName() {
            return null;
        }

        public String getIconFileName() {
            return null;
        }

        @RequirePOST
        public HttpResponse doSet(@QueryParameter String value) throws IOException {
            if (ThemeManagerPageDecorator.get().isDisableUserThemes()) {
                throw new RuntimeException("Setting user themes is disabled");
            }

            User user = User.current();

            if (user == null) {
                throw new RuntimeException("User is not signed in");
            }

            ThemeUserProperty p = user.getProperty(ThemeUserProperty.class);
            p.setUserTheme(UserTheme.of(ThemeManagerFactoryDescriptor.all().stream()
                    .filter(e -> e.getThemeKey().equals(value))
                    .findFirst()
                    .orElseThrow()
                    .getInstance()));
            user.save();

            return HttpResponses.ok();
        }
    }
}
