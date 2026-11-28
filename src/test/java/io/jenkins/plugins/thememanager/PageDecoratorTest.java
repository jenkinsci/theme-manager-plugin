package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.User;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlRadioButtonInput;
import org.htmlunit.util.NameValuePair;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PageDecoratorTest {

    private static User configureUser(JenkinsRule j) {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice"));
        User alice = User.getById("alice", true);
        assertThat(alice, notNullValue());
        return alice;
    }

    private static HtmlRadioButtonInput findThemeRadio(HtmlPage page, String role, String themeKey) {
        HtmlRadioButtonInput radio = page.querySelector(
                ".app-theme-picker__input[data-theme-role='" + role + "'][data-theme='" + themeKey + "']");
        assertThat(radio, notNullValue());
        return radio;
    }

    private static void postTheme(JenkinsRule j, JenkinsRule.WebClient wc, String value, @CheckForNull String mode)
            throws Exception {
        WebRequest request =
                new WebRequest(j.getURL().toURI().resolve("theme/set").toURL(), HttpMethod.POST);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("value", value));
        if (mode != null) {
            params.add(new NameValuePair("mode", mode));
        }
        request.setRequestParameters(params);
        wc.addCrumb(request);
        WebResponse response = wc.loadWebResponse(request);
        assertThat(response.getStatusCode(), is(200));
    }

    private static void assertActiveThemeApplied(HtmlPage page, boolean isDark) {
        assertThat(
                page.getWebResponse().getContentAsString(),
                containsString("{\"light\":\"light-test\",\"dark\":\"dark-test\"}"));
        assertThat(
                page.executeJavaScript("document.documentElement.dataset.theme").getJavaScriptResult(),
                is(isDark ? "dark-test" : "light-test"));
        assertThat(page.executeJavaScript("window.isSystemRespectingTheme").getJavaScriptResult(), is(true));
        assertThat(
                page.executeJavaScript("window.getThemeManagerProperty('prism-api', 'theme')")
                        .getJavaScriptResult(),
                is(isDark ? "tomorrow" : "prism"));
    }

    @Test
    void testJavaScriptLoadsOnSimpleAndRegularPages(JenkinsRule j) throws Exception {
        configureUser(j);
        ThemeManagerPageDecorator.get().setTheme(new ThemeSelection(new TestThemes.Light(), new TestThemes.Dark()));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            // HtmlUnit does not emulate prefers-color-scheme toggling, so assert against whichever mode it reports
            HtmlPage loginPage = wc.goTo("login");
            boolean isDark = Boolean.TRUE.equals(loginPage
                    .executeJavaScript("window.matchMedia('(prefers-color-scheme: dark)').matches")
                    .getJavaScriptResult());
            assertActiveThemeApplied(loginPage, isDark);

            wc.login("alice");
            HtmlPage regularPage = wc.goTo("");
            assertActiveThemeApplied(regularPage, isDark);
        }
    }

    @Test
    void testAppearanceFormRoundtrip(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("manage/appearance");
            assertThat(
                    page.querySelectorAll(".app-theme-picker__item"),
                    hasSize(ThemeManagerFactoryDescriptor.all().size()));

            findThemeRadio(page, "light", "light-test").setChecked(true);
            findThemeRadio(page, "dark", "dark-test").setChecked(true);
            j.submit(page.getFormByName("config"));

            ThemeSelection themeSelection = ThemeManagerPageDecorator.get().getTheme();
            assertThat(themeSelection.getLight(), instanceOf(TestThemes.Light.class));
            assertThat(themeSelection.getDark(), instanceOf(TestThemes.Dark.class));

            HtmlPage reloaded = wc.goTo("manage/appearance");
            assertThat(findThemeRadio(reloaded, "light", "light-test").isChecked(), is(true));
            assertThat(findThemeRadio(reloaded, "dark", "dark-test").isChecked(), is(true));

            // Select the same theme for both light and dark
            findThemeRadio(reloaded, "light", "dark-test").setChecked(true);
            j.submit(reloaded.getFormByName("config"));

            ThemeSelection singleTheme = ThemeManagerPageDecorator.get().getTheme();
            assertThat(singleTheme.getLight(), instanceOf(TestThemes.Dark.class));
            assertThat(singleTheme.getDark(), instanceOf(TestThemes.Dark.class));
        }
    }

    @Test
    void testUserAppearanceFormRoundtrip(JenkinsRule j) throws Exception {
        User alice = configureUser(j);

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.login("alice");
            HtmlPage page = wc.goTo("user/alice/appearance");
            findThemeRadio(page, "light", "light-test").setChecked(true);
            findThemeRadio(page, "dark", "dark-test").setChecked(true);
            j.submit(page.getFormByName("config"));

            ThemeUserProperty prop = alice.getProperty(ThemeUserProperty.class);
            assertThat(prop, notNullValue());
            assertThat(prop.getTheme().getLight(), instanceOf(TestThemes.Light.class));
            assertThat(prop.getTheme().getDark(), instanceOf(TestThemes.Dark.class));

            HtmlPage reloaded = wc.goTo("user/alice/appearance");
            assertThat(findThemeRadio(reloaded, "light", "light-test").isChecked(), is(true));
            assertThat(findThemeRadio(reloaded, "dark", "dark-test").isChecked(), is(true));
        }
    }

    @Test
    void testHeaderDropdownThemeSelection(JenkinsRule j) throws Exception {
        User alice = configureUser(j);
        ThemeManagerPageDecorator.get().setTheme(ThemeSelection.of(new TestThemes.Light()));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.login("alice");
            HtmlPage initialPage = wc.goTo("");
            assertThat(
                    initialPage.getWebResponse().getContentAsString(), containsString("account-theme-picker-template"));

            // Updating dark mode preserves the inherited global light theme
            postTheme(j, wc, "dark-test", "dark");
            ThemeUserProperty prop = alice.getProperty(ThemeUserProperty.class);
            assertThat(prop, notNullValue());
            assertThat(prop.getTheme().getLight(), instanceOf(TestThemes.Light.class));
            assertThat(prop.getTheme().getDark(), instanceOf(TestThemes.Dark.class));

            // Updating light mode preserves the user's dark theme
            postTheme(j, wc, "none", "light");
            assertThat(prop.getTheme().getLight().getDescriptor().getThemeKey(), is("none"));
            assertThat(prop.getTheme().getDark(), instanceOf(TestThemes.Dark.class));

            // Omitting mode sets both slots for backwards compatibility
            postTheme(j, wc, "dark-test", null);
            assertThat(prop.getTheme().getLight(), instanceOf(TestThemes.Dark.class));
            assertThat(prop.getTheme().getDark(), instanceOf(TestThemes.Dark.class));
        }
    }
}
