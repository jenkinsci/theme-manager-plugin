package io.jenkins.plugins.thememanager;

import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.ExtensionList;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.impl.configurators.GlobalConfigurationCategoryConfigurator;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Objects;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
class ThemeManagerJCasCTest {

    private static Mapping getAppearanceRoot(ConfigurationContext context) {
        GlobalConfigurationCategory category =
                ExtensionList.lookup(AppearanceCategory.class).get(0);
        GlobalConfigurationCategoryConfigurator configurator = new GlobalConfigurationCategoryConfigurator(category);
        return Objects.requireNonNull(configurator.describe(configurator.getTargetComponent(context), context))
                .asMapping();
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCode.yml")
    void testConfig(JenkinsConfiguredWithCodeRule j) {
        ThemeManagerPageDecorator decorator = ThemeManagerPageDecorator.get();

        UserTheme userTheme = decorator.getUserTheme();
        assertNotNull(userTheme);
        assertThat(userTheme, instanceOf(UserTheme.StaticTheme.class));
        assertThat(((UserTheme.StaticTheme) userTheme).getTheme(), instanceOf(NoOpThemeManagerFactory.class));

        assertThat(decorator.isDisableUserThemes(), is(true));
        assertThat(decorator.isRespectSystemAppearance(), is(false));
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCode.yml")
    void testExport(JenkinsConfiguredWithCodeRule j) throws Exception {
        ConfigurationContext context = new ConfigurationContext(ConfiguratorRegistry.get());
        CNode yourAttribute = getAppearanceRoot(context).get("themeManager");

        String exported = toYamlString(yourAttribute);

        String expected = toStringFromYamlFile(this, "ConfigurationAsCodeExport.yml");

        assertThat(exported, is(expected));
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeSystem.yml")
    void testSystemConfig(JenkinsConfiguredWithCodeRule j) {
        ThemeManagerPageDecorator decorator = ThemeManagerPageDecorator.get();

        UserTheme userTheme = decorator.getUserTheme();
        assertNotNull(userTheme);
        assertThat(userTheme, instanceOf(UserTheme.SystemTheme.class));
        UserTheme.SystemTheme systemTheme = (UserTheme.SystemTheme) userTheme;
        assertThat(systemTheme.getLight(), instanceOf(NoOpThemeManagerFactory.class));
        assertThat(systemTheme.getDark(), instanceOf(NoOpThemeManagerFactory.class));

        assertThat(decorator.isDisableUserThemes(), is(true));
        assertThat(decorator.isRespectSystemAppearance(), is(true));
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeSystem.yml")
    void testSystemExport(JenkinsConfiguredWithCodeRule j) throws Exception {
        ConfigurationContext context = new ConfigurationContext(ConfiguratorRegistry.get());
        CNode yourAttribute = getAppearanceRoot(context).get("themeManager");

        String exported = toYamlString(yourAttribute);

        String expected = toStringFromYamlFile(this, "ConfigurationAsCodeSystemExport.yml");

        assertThat(exported, is(expected));
    }

    @Test
    void testLegacyXmlMigration(JenkinsConfiguredWithCodeRule j) {
        String legacyDecoratorXml = "<io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>"
                + "<theme class=\"io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>";
        ThemeManagerPageDecorator decorator = (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(legacyDecoratorXml);
        assertThat(decorator.getUserTheme(), instanceOf(UserTheme.StaticTheme.class));
        assertThat(
                ((UserTheme.StaticTheme) decorator.getUserTheme()).getTheme(),
                instanceOf(NoOpThemeManagerFactory.class));

        String legacyUserPropertyXml = "<io.jenkins.plugins.thememanager.ThemeUserProperty>"
                + "<theme class=\"io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeUserProperty>";
        ThemeUserProperty userProperty = (ThemeUserProperty) Jenkins.XSTREAM2.fromXML(legacyUserPropertyXml);
        assertThat(userProperty.getUserTheme(), instanceOf(UserTheme.StaticTheme.class));
        assertThat(
                ((UserTheme.StaticTheme) userProperty.getUserTheme()).getTheme(),
                instanceOf(NoOpThemeManagerFactory.class));
    }
}
