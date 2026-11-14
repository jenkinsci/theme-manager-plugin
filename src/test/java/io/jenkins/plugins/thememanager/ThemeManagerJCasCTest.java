package io.jenkins.plugins.thememanager;

import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.umd.cs.findbugs.annotations.NonNull;
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
import org.jenkinsci.Symbol;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

@WithJenkinsConfiguredWithCode
class ThemeManagerJCasCTest {

    public static class DarkTestThemeManagerFactory extends ThemeManagerFactory {
        @DataBoundConstructor
        public DarkTestThemeManagerFactory() {}

        @Override
        public Theme getTheme() {
            return Theme.builder().build();
        }

        @TestExtension
        @Symbol("darkTest")
        public static class DescriptorImpl extends ThemeManagerFactoryDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "Dark Test";
            }

            @Override
            public String getThemeId() {
                return "dark-test";
            }
        }
    }

    public static class LightTestThemeManagerFactory extends ThemeManagerFactory {
        @DataBoundConstructor
        public LightTestThemeManagerFactory() {}

        @Override
        public Theme getTheme() {
            return Theme.builder().build();
        }

        @TestExtension
        @Symbol("lightTest")
        public static class DescriptorImpl extends ThemeManagerFactoryDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "Light Test";
            }

            @Override
            public String getThemeId() {
                return "light-test";
            }
        }
    }

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

        JenkinsTheme jenkinsTheme = decorator.getTheme();
        assertNotNull(jenkinsTheme);
        assertThat(jenkinsTheme, instanceOf(JenkinsTheme.StaticTheme.class));
        assertThat(((JenkinsTheme.StaticTheme) jenkinsTheme).theme(), instanceOf(DarkTestThemeManagerFactory.class));

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

        JenkinsTheme jenkinsTheme = decorator.getTheme();
        assertNotNull(jenkinsTheme);
        assertThat(jenkinsTheme, instanceOf(JenkinsTheme.SystemTheme.class));
        JenkinsTheme.SystemTheme systemTheme = (JenkinsTheme.SystemTheme) jenkinsTheme;
        assertThat(systemTheme.light(), instanceOf(LightTestThemeManagerFactory.class));
        assertThat(systemTheme.dark(), instanceOf(DarkTestThemeManagerFactory.class));

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
                + "<theme class=\"io.jenkins.plugins.thememanager.ThemeManagerJCasCTest$DarkTestThemeManagerFactory\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>";
        ThemeManagerPageDecorator decorator = (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(legacyDecoratorXml);
        assertThat(decorator.getTheme(), instanceOf(JenkinsTheme.StaticTheme.class));
        assertThat(
                ((JenkinsTheme.StaticTheme) decorator.getTheme()).theme(),
                instanceOf(DarkTestThemeManagerFactory.class));

        String legacyUserPropertyXml = "<io.jenkins.plugins.thememanager.ThemeUserProperty>"
                + "<theme class=\"io.jenkins.plugins.thememanager.ThemeManagerJCasCTest$DarkTestThemeManagerFactory\"/>"
                + "</io.jenkins.plugins.thememanager.ThemeUserProperty>";
        ThemeUserProperty userProperty = (ThemeUserProperty) Jenkins.XSTREAM2.fromXML(legacyUserPropertyXml);
        assertThat(userProperty.getTheme(), instanceOf(JenkinsTheme.StaticTheme.class));
        assertThat(
                ((JenkinsTheme.StaticTheme) userProperty.getTheme()).theme(),
                instanceOf(DarkTestThemeManagerFactory.class));
    }
}
