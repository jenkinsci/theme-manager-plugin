package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.User;
import java.net.URL;
import java.util.List;
import jenkins.model.Jenkins;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlRadioButtonInput;
import org.htmlunit.util.NameValuePair;
import org.jenkinsci.Symbol;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.DataBoundConstructor;

@WithJenkins
class PageDecoratorTest {

    public static class DarkTestThemeManagerFactory extends ThemeManagerFactory {
        @DataBoundConstructor
        public DarkTestThemeManagerFactory() {}

        @Override
        public Theme getTheme() {
            return Theme.builder()
                    .withProperty("prism-api", "theme", "tomorrow")
                    .build();
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
            return Theme.builder().withProperty("prism-api", "theme", "prism").build();
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

    @Test
    void testJavaScriptLoadsOnSimpleAndRegularPages(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy auth =
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(auth);

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.login("alice");
            wc.goTo("");
        }
    }

    @Test
    void testAppearanceFormRoundtrip(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("manage/appearance");
            assertThat(
                    page.querySelectorAll(".app-theme-picker__item"),
                    hasSize(ThemeManagerFactoryDescriptor.all().size()));

            HtmlRadioButtonInput lightRadio =
                    page.querySelector(".app-theme-picker__input[data-theme-role='light'][data-theme='light-test']");
            HtmlRadioButtonInput darkRadio =
                    page.querySelector(".app-theme-picker__input[data-theme-role='dark'][data-theme='dark-test']");
            assertThat(lightRadio, notNullValue());
            assertThat(darkRadio, notNullValue());
            lightRadio.setChecked(true);
            darkRadio.setChecked(true);

            j.submit(page.getFormByName("config"));

            ThemeSelection themeSelection = ThemeManagerPageDecorator.get().getTheme();
            assertThat(themeSelection.getLight(), instanceOf(LightTestThemeManagerFactory.class));
            assertThat(themeSelection.getDark(), instanceOf(DarkTestThemeManagerFactory.class));

            // Reload and confirm light/dark selections persist
            HtmlPage reloaded = wc.goTo("manage/appearance");
            HtmlRadioButtonInput reloadedLightRadio = reloaded.querySelector(
                    ".app-theme-picker__input[data-theme-role='light'][data-theme='light-test']");
            HtmlRadioButtonInput reloadedDarkRadio =
                    reloaded.querySelector(".app-theme-picker__input[data-theme-role='dark'][data-theme='dark-test']");
            assertThat(reloadedLightRadio.isChecked(), is(true));
            assertThat(reloadedDarkRadio.isChecked(), is(true));

            // Select the same theme for both light and dark
            HtmlRadioButtonInput sameLightRadio =
                    reloaded.querySelector(".app-theme-picker__input[data-theme-role='light'][data-theme='dark-test']");
            sameLightRadio.setChecked(true);
            j.submit(reloaded.getFormByName("config"));

            ThemeSelection singleTheme = ThemeManagerPageDecorator.get().getTheme();
            assertThat(singleTheme.getLight(), instanceOf(DarkTestThemeManagerFactory.class));
            assertThat(singleTheme.getDark(), instanceOf(DarkTestThemeManagerFactory.class));
        }
    }

    @Test
    void testUserAppearanceFormRoundtrip(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy auth =
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(auth);

        User alice = User.getById("alice", true);
        assertThat(alice, notNullValue());

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.login("alice");
            HtmlPage page = wc.goTo("user/alice/appearance");
            HtmlRadioButtonInput lightRadio =
                    page.querySelector(".app-theme-picker__input[data-theme-role='light'][data-theme='light-test']");
            HtmlRadioButtonInput darkRadio =
                    page.querySelector(".app-theme-picker__input[data-theme-role='dark'][data-theme='dark-test']");
            assertThat(lightRadio, notNullValue());
            assertThat(darkRadio, notNullValue());
            lightRadio.setChecked(true);
            darkRadio.setChecked(true);

            HtmlForm form = page.getFormByName("config");
            j.submit(form);

            ThemeUserProperty prop = alice.getProperty(ThemeUserProperty.class);
            assertThat(prop, notNullValue());
            ThemeSelection userTheme = prop.getTheme();
            assertThat(userTheme.getLight(), instanceOf(LightTestThemeManagerFactory.class));
            assertThat(userTheme.getDark(), instanceOf(DarkTestThemeManagerFactory.class));

            HtmlPage reloaded = wc.goTo("user/alice/appearance");
            HtmlRadioButtonInput reloadedLightRadio = reloaded.querySelector(
                    ".app-theme-picker__input[data-theme-role='light'][data-theme='light-test']");
            HtmlRadioButtonInput reloadedDarkRadio =
                    reloaded.querySelector(".app-theme-picker__input[data-theme-role='dark'][data-theme='dark-test']");
            assertThat(reloadedLightRadio.isChecked(), is(true));
            assertThat(reloadedDarkRadio.isChecked(), is(true));
        }
    }

    @Test
    void testHeaderDropdownThemeSelection(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy auth =
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(auth);

        User alice = User.getById("alice", true);
        assertThat(alice, notNullValue());

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.login("alice");
            HtmlPage initialPage = wc.goTo("");
            String initialHtml = initialPage.getWebResponse().getContentAsString();
            assertThat(initialHtml, containsString("account-theme-picker-template"));
            assertThat(initialHtml, containsString("value=\"dark-test\""));

            WebRequest request = new WebRequest(new URL(j.getURL(), "theme/set"), HttpMethod.POST);
            request.setRequestParameters(List.of(new NameValuePair("value", "dark-test")));
            wc.addCrumb(request);
            WebResponse response = wc.loadWebResponse(request);
            assertThat(response.getStatusCode(), is(200));

            ThemeUserProperty prop = alice.getProperty(ThemeUserProperty.class);
            assertThat(prop, notNullValue());
            ThemeSelection userTheme = prop.getTheme();
            assertThat(userTheme.getLight(), instanceOf(DarkTestThemeManagerFactory.class));
            assertThat(userTheme.getDark(), instanceOf(DarkTestThemeManagerFactory.class));

            HtmlPage updatedPage = wc.goTo("");
            String updatedHtml = updatedPage.getWebResponse().getContentAsString();
            assertThat(updatedHtml, containsString("{\"light\":\"dark-test\",\"dark\":\"dark-test\"}"));
            assertThat(updatedHtml, containsString("Dark Test"));
        }
    }

    public static class CssLightTestThemeManagerFactory extends ThemeManagerFactory {
        @DataBoundConstructor
        public CssLightTestThemeManagerFactory() {}

        @Override
        public Theme getTheme() {
            return Theme.builder()
                    .withCssUrl("/theme-css-light/theme.css")
                    .withProperty("prism-api", "theme", "prism")
                    .build();
        }

        @TestExtension
        public static class DescriptorImpl extends ThemeManagerFactoryDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "CSS Light Test";
            }

            @Override
            public String getThemeId() {
                return "css-light-test";
            }
        }
    }

    public static class CssDarkTestThemeManagerFactory extends ThemeManagerFactory {
        @DataBoundConstructor
        public CssDarkTestThemeManagerFactory() {}

        @Override
        public Theme getTheme() {
            return Theme.builder()
                    .withCssUrl("/theme-css-dark/theme.css")
                    .withProperty("prism-api", "theme", "tomorrow")
                    .build();
        }

        @TestExtension
        public static class DescriptorImpl extends ThemeManagerFactoryDescriptor {
            @NonNull
            @Override
            public String getDisplayName() {
                return "CSS Dark Test";
            }

            @Override
            public String getThemeId() {
                return "css-dark-test";
            }
        }
    }

    @Test
    void testHeaderEmitsLightAndDarkThemesOnRegularAndSimplePages(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy auth =
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(auth);

        ThemeManagerPageDecorator.get()
                .setTheme(new ThemeSelection(new LightTestThemeManagerFactory(), new DarkTestThemeManagerFactory()));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage loginPage = wc.goTo("login");
            String loginHtml = loginPage.getWebResponse().getContentAsString();
            assertThat(loginHtml, containsString("{\"light\":\"light-test\",\"dark\":\"dark-test\"}"));
            boolean isDark = Boolean.TRUE.equals(loginPage
                    .executeJavaScript("window.matchMedia('(prefers-color-scheme: dark)').matches")
                    .getJavaScriptResult());
            assertThat(
                    loginPage
                            .executeJavaScript("document.documentElement.dataset.theme")
                            .getJavaScriptResult(),
                    is(isDark ? "dark-test" : "light-test"));
            assertThat(
                    loginPage
                            .executeJavaScript("window.isSystemRespectingTheme")
                            .getJavaScriptResult(),
                    is(true));
            assertThat(
                    loginPage
                            .executeJavaScript("window.getThemeManagerProperty('prism-api', 'theme')")
                            .getJavaScriptResult(),
                    is(isDark ? "tomorrow" : "prism"));

            wc.login("alice");
            HtmlPage regularPage = wc.goTo("");
            String regularHtml = regularPage.getWebResponse().getContentAsString();
            assertThat(regularHtml, containsString("{\"light\":\"light-test\",\"dark\":\"dark-test\"}"));
            assertThat(
                    regularPage
                            .executeJavaScript("document.documentElement.dataset.theme")
                            .getJavaScriptResult(),
                    is(isDark ? "dark-test" : "light-test"));
            assertThat(
                    regularPage
                            .executeJavaScript("window.isSystemRespectingTheme")
                            .getJavaScriptResult(),
                    is(true));
            assertThat(
                    regularPage
                            .executeJavaScript("window.getThemeManagerProperty('prism-api', 'theme')")
                            .getJavaScriptResult(),
                    is(isDark ? "tomorrow" : "prism"));
        }
    }

    @Test
    void testNonNamespacedCssUsesMediaQueriesWhenThemesDiffer(JenkinsRule j) throws Exception {
        ThemeManagerPageDecorator.get()
                .setTheme(new ThemeSelection(
                        new CssLightTestThemeManagerFactory(), new CssDarkTestThemeManagerFactory()));

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.getOptions().setCssEnabled(false);
            HtmlPage splitPage = wc.goTo("");
            String splitHtml = splitPage.getWebResponse().getContentAsString();
            assertThat(
                    splitHtml,
                    containsString(
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-css-light/theme.css\" media=\"(prefers-color-scheme: light)\"/>"));
            assertThat(
                    splitHtml,
                    containsString(
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-css-dark/theme.css\" media=\"(prefers-color-scheme: dark)\"/>"));
            boolean isDark = Boolean.TRUE.equals(splitPage
                    .executeJavaScript("window.matchMedia('(prefers-color-scheme: dark)').matches")
                    .getJavaScriptResult());
            assertThat(
                    splitPage
                            .executeJavaScript("window.getThemeManagerProperty('prism-api', 'theme')")
                            .getJavaScriptResult(),
                    is(isDark ? "tomorrow" : "prism"));

            ThemeManagerPageDecorator.get().setTheme(ThemeSelection.of(new CssDarkTestThemeManagerFactory()));
            HtmlPage samePage = wc.goTo("");
            String sameHtml = samePage.getWebResponse().getContentAsString();
            assertThat(
                    sameHtml,
                    containsString(
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-css-dark/theme.css\" media=\"(prefers-color-scheme: light)\"/>"));
            assertThat(
                    sameHtml,
                    containsString(
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-css-dark/theme.css\" media=\"(prefers-color-scheme: dark)\"/>"));
            assertThat(
                    samePage.executeJavaScript("window.isSystemRespectingTheme").getJavaScriptResult(), is(true));
            assertThat(
                    samePage.executeJavaScript("window.getThemeManagerProperty('prism-api', 'theme')")
                            .getJavaScriptResult(),
                    is("tomorrow"));
        }
    }
}
