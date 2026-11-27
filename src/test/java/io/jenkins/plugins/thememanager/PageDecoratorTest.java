package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
            HtmlRadioButtonInput darkRadio = page.querySelector("input[type='radio'][data-theme='dark-test']");
            assertThat(darkRadio, notNullValue());
            darkRadio.setChecked(true);

            HtmlForm form = page.getFormByName("config");
            j.submit(form);

            JenkinsTheme jenkinsTheme = ThemeManagerPageDecorator.get().getTheme();
            assertThat(jenkinsTheme, instanceOf(JenkinsTheme.StaticTheme.class));
            assertThat(
                    ((JenkinsTheme.StaticTheme) jenkinsTheme).theme(), instanceOf(DarkTestThemeManagerFactory.class));

            HtmlPage reloaded = wc.goTo("manage/appearance");
            HtmlRadioButtonInput reloadedDarkRadio =
                    reloaded.querySelector("input[type='radio'][data-theme='dark-test']");
            assertThat(reloadedDarkRadio.isChecked(), is(true));
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
            HtmlRadioButtonInput darkRadio = page.querySelector("input[type='radio'][data-theme='dark-test']");
            assertThat(darkRadio, notNullValue());
            darkRadio.setChecked(true);

            HtmlForm form = page.getFormByName("config");
            j.submit(form);

            ThemeUserProperty prop = alice.getProperty(ThemeUserProperty.class);
            assertThat(prop, notNullValue());
            JenkinsTheme userTheme = prop.getTheme();
            assertThat(userTheme, instanceOf(JenkinsTheme.StaticTheme.class));
            assertThat(((JenkinsTheme.StaticTheme) userTheme).theme(), instanceOf(DarkTestThemeManagerFactory.class));

            HtmlPage reloaded = wc.goTo("user/alice/appearance");
            HtmlRadioButtonInput reloadedDarkRadio =
                    reloaded.querySelector("input[type='radio'][data-theme='dark-test']");
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
            JenkinsTheme userTheme = prop.getTheme();
            assertThat(userTheme, instanceOf(JenkinsTheme.StaticTheme.class));
            assertThat(((JenkinsTheme.StaticTheme) userTheme).theme(), instanceOf(DarkTestThemeManagerFactory.class));

            HtmlPage updatedPage = wc.goTo("");
            String updatedHtml = updatedPage.getWebResponse().getContentAsString();
            assertThat(updatedHtml, containsString("\"id\": \"dark-test\""));
            assertThat(updatedHtml, containsString("Dark Test"));
        }
    }
}
