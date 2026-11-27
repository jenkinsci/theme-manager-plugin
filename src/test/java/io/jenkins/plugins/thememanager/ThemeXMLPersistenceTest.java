package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ThemeXMLPersistenceTest {

    @Test
    void testLegacyXmlMigration(JenkinsRule j) {
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
    void testXStreamRoundtrip(JenkinsRule j) {
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

    @Test
    void testMissingThemeClassFallsBackToNoOp(JenkinsRule j) {
        String missingSingleSlotXml = "<io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>"
                + "<themeSelection>"
                + "<light class=\"io.jenkins.plugins.thememanager.MissingTheme\"/>"
                + "<dark class=\"io.jenkins.plugins.thememanager.TestThemes$Dark\"/>"
                + "</themeSelection>"
                + "</io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>";
        ThemeManagerPageDecorator partial = (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(missingSingleSlotXml);
        assertThat(partial.getTheme().getLight(), instanceOf(NoOpThemeManagerFactory.class));
        assertThat(partial.getTheme().getDark(), instanceOf(TestThemes.Dark.class));
        assertThat(
                partial.getTheme().generateTheme(),
                is(
                        "<script id=\"theme-manager-theme\" type=\"application/json\">{\"light\":\"none\",\"dark\":\"dark-test\"}</script>"));

        String missingReferencedXml = "<io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>"
                + "<themeSelection>"
                + "<light class=\"io.jenkins.plugins.thememanager.MissingTheme\"/>"
                + "<dark reference=\"../light\"/>"
                + "</themeSelection>"
                + "</io.jenkins.plugins.thememanager.ThemeManagerPageDecorator>";
        ThemeManagerPageDecorator bothMissing =
                (ThemeManagerPageDecorator) Jenkins.XSTREAM2.fromXML(missingReferencedXml);
        assertThat(bothMissing.getTheme().getLight(), instanceOf(NoOpThemeManagerFactory.class));
        assertThat(bothMissing.getTheme().getDark(), instanceOf(NoOpThemeManagerFactory.class));
    }
}
