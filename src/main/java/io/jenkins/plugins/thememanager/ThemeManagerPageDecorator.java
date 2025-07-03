package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.PageDecorator;
import hudson.model.User;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;

@Extension
@Symbol("themeManager")
public class ThemeManagerPageDecorator extends PageDecorator {

    private ThemeManagerFactory theme;
    private boolean disableUserThemes;

    public ThemeManagerPageDecorator() {
        load();
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
    public void setTheme(ThemeManagerFactory theme) {
        this.theme = theme;
    }

    public ThemeManagerFactory getTheme() {
        return theme;
    }

    public boolean isDisableUserThemes() {
        return disableUserThemes;
    }

    @DataBoundSetter
    public void setDisableUserThemes(boolean disableUserThemes) {
        this.disableUserThemes = disableUserThemes;
    }

    /**
     * Finds the active theme. Checks User and then global theme.
     *
     * @return the active theme, or a no-op theme if not selected
     */
    @NonNull
    public Theme findTheme() {
        if (!disableUserThemes) {
            Theme userTheme = ThemeUserProperty.forCurrentUser();
            if (userTheme != null) {
                return userTheme;
            }
        }

        if (theme != null) {
            return theme.getTheme();
        }
        return new NoOpThemeManagerFactory().getTheme();
    }

    @NonNull
    public ThemeManagerFactory findThemeFactory() {
        if (!disableUserThemes) {
            ThemeManagerFactory userTheme = ThemeUserProperty.forCurrentUserFactory();
            if (userTheme != null) {
                return userTheme;
            }
        }

        if (theme != null) {
            return theme;
        }
        return new NoOpThemeManagerFactory();
    }

    @NonNull
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
            return StringUtils.join(data, "\n");
        }

        return StringUtils.join(namespacedThemes, "\n");
    }

    @SuppressWarnings("unused") // called by jelly
    public String getThemeKey() {
        ThemeManagerFactory themeFactory = findThemeFactory();

        return themeFactory.getDescriptor().getThemeKey();
    }

    @SuppressWarnings("unused") // called by jelly
    public boolean isRespectSystemAppearance() {
        ThemeManagerFactory themeFactory = findThemeFactory();

        return themeFactory.getTheme().isRespectSystemAppearance();
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
        if (ancestors == null || ancestors.size() == 0) {
            return false;
        }

        Ancestor a = ancestors.get(ancestors.size() - 1);
        Object o = a.getObject();

        // We don't want to style the build-monitor-plugin
        return !o.getClass().getName().startsWith("com.smartcodeltd.jenkinsci.plugins.buildmonitor");
    }
}
