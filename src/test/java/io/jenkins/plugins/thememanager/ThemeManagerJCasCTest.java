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
import io.jenkins.plugins.casc.SchemaGeneration;
import io.jenkins.plugins.casc.impl.configurators.GlobalConfigurationCategoryConfigurator;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import java.util.Objects;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import org.json.JSONObject;
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
    void testSchemaIncludesLightAndDark(JenkinsConfiguredWithCodeRule j) {
        JSONObject schema = SchemaGeneration.generateSchema();
        JSONObject definitions = schema.getJSONObject("definitions");
        JSONObject themeSelection = definitions.getJSONObject(ThemeSelection.class.getName());
        JSONObject properties = themeSelection.getJSONObject("properties");

        String factoryRef = "#/definitions/" + ThemeManagerFactory.class.getName();
        assertThat(properties.getJSONObject("light").getString("$ref"), is(factoryRef));
        assertThat(properties.getJSONObject("dark").getString("$ref"), is(factoryRef));
        assertThat(definitions.has(ThemeManagerFactory.class.getName()), is(true));
    }
}
