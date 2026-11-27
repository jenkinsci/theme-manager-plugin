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

    private ThemeSelection themeSelection;
    // Retained for backwards compatibility with legacy <theme> XStream XML configs
    private ThemeManagerFactory theme;
    private boolean disableUserThemes;

    public ThemeManagerPageDecorator() {
        load();
    }

    // Migrates legacy <theme> XStream XML entries into ThemeSelection
    protected Object readResolve() {
        if (theme != null && themeSelection == null) {
            themeSelection = ThemeSelection.of(theme);
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
    public void setTheme(ThemeSelection theme) {
        this.themeSelection = theme;
    }

    @Deprecated
    public void setTheme(ThemeManagerFactory theme) {
        this.themeSelection = theme != null ? ThemeSelection.of(theme) : null;
    }

    public ThemeSelection getTheme() {
        if (themeSelection == null) {
            return ThemeSelection.of(new NoOpThemeManagerFactory());
        }
        return themeSelection;
    }

    public boolean isDisableUserThemes() {
        return disableUserThemes;
    }

    @DataBoundSetter
    public void setDisableUserThemes(boolean disableUserThemes) {
        this.disableUserThemes = disableUserThemes;
    }

    @NonNull
    public ThemeSelection findThemeSelection() {
        if (!disableUserThemes) {
            ThemeSelection currentUserSelection = ThemeUserProperty.forCurrentUserSelection();
            if (currentUserSelection != null) {
                return currentUserSelection;
            }
        }

        if (themeSelection != null) {
            return themeSelection;
        }
        return ThemeSelection.of(new NoOpThemeManagerFactory());
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
        return findThemeSelection().getLight();
    }

    public boolean shouldShowAccountThemePicker() {
        return User.current() != null && !isDisableUserThemes();
    }

    /** Get the complete header HTML for all configured theme elements. */
    public String getHeaderHtml() {
        boolean injectCss = shouldInjectCss();
        ThemeSelection selection = findThemeSelection();

        Set<String> headerElements = new LinkedHashSet<>();
        headerElements.add(selection.generateTheme());
        headerElements.addAll(selection.generateHeaderElements(injectCss));

        ThemeManagerFactoryDescriptor.all().stream()
                .filter(ThemeManagerFactoryDescriptor::isNamespaced)
                .map(desc -> desc.getInstance().getTheme().generateHeaderElements(injectCss))
                .flatMap(Set::stream)
                .forEach(headerElements::add);

        headerElements.add(selection.generateProperties());

        return String.join("\n", headerElements);
    }

    @SuppressWarnings("unused") // called by jelly
    public String getThemeKey() {
        ThemeManagerFactory themeFactory = findThemeSelection().getLight();

        return themeFactory.getDescriptor().getThemeKey();
    }

    @Deprecated
    public boolean isRespectSystemAppearance() {
        return true;
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
            p.setTheme(ThemeSelection.of(ThemeManagerFactoryDescriptor.all().stream()
                    .filter(e -> e.getThemeKey().equals(value))
                    .findFirst()
                    .orElseThrow()
                    .getInstance()));
            user.save();

            return HttpResponses.ok();
        }
    }
}
