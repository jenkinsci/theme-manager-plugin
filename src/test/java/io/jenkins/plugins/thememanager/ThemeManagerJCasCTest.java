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

        ThemeSelection themeSelection = decorator.getTheme();
        assertNotNull(themeSelection);
        assertThat(themeSelection.getLight(), instanceOf(TestThemes.Dark.class));
        assertThat(themeSelection.getDark(), instanceOf(TestThemes.Dark.class));

        assertThat(decorator.isDisableUserThemes(), is(true));
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

        ThemeSelection themeSelection = decorator.getTheme();
        assertNotNull(themeSelection);
        assertThat(themeSelection.getLight(), instanceOf(TestThemes.Light.class));
        assertThat(themeSelection.getDark(), instanceOf(TestThemes.Dark.class));

        assertThat(decorator.isDisableUserThemes(), is(true));
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
                + "<theme class=\"io.jenkins.plugins.thememanager.TestThemes$Dark\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>";
        ThemeManagerPageDecorator decorator = (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(legacyDecoratorXml);
        assertThat(decorator.getTheme().getLight(), instanceOf(TestThemes.Dark.class));
        assertThat(decorator.getTheme().getDark(), instanceOf(TestThemes.Dark.class));

        String legacyUserPropertyXml = "<io.jenkins.plugins.thememanager.ThemeUserProperty>"
                + "<theme class=\"io.jenkins.plugins.thememanager.TestThemes$Dark\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeUserProperty>";
        ThemeUserProperty userProperty = (ThemeUserProperty) Jenkins.XSTREAM2.fromXML(legacyUserPropertyXml);
        assertThat(userProperty.getTheme().getLight(), instanceOf(TestThemes.Dark.class));
        assertThat(userProperty.getTheme().getDark(), instanceOf(TestThemes.Dark.class));
    }

    @Test
    void testXStreamRoundtrip(JenkinsConfiguredWithCodeRule j) {
        ThemeManagerPageDecorator singleDecorator = new ThemeManagerPageDecorator();
        singleDecorator.setTheme(ThemeSelection.of(new TestThemes.Dark()));
        ThemeManagerPageDecorator deserializedSingle =
                (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(Jenkins.XSTREAM2.toXML(singleDecorator));
        assertThat(deserializedSingle.getTheme().getLight(), instanceOf(TestThemes.Dark.class));
        assertThat(deserializedSingle.getTheme().getDark(), instanceOf(TestThemes.Dark.class));

        ThemeManagerPageDecorator splitDecorator = new ThemeManagerPageDecorator();
        splitDecorator.setTheme(new ThemeSelection(new TestThemes.Light(), new TestThemes.Dark()));
        ThemeManagerPageDecorator deserializedSplit =
                (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(Jenkins.XSTREAM2.toXML(splitDecorator));
        assertThat(deserializedSplit.getTheme().getLight(), instanceOf(TestThemes.Light.class));
        assertThat(deserializedSplit.getTheme().getDark(), instanceOf(TestThemes.Dark.class));
    }
}
