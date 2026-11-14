package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PageDecoratorTest {

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
            HtmlForm form = page.getFormByName("config");
            j.submit(form);

            JenkinsTheme jenkinsTheme = ThemeManagerPageDecorator.get().getTheme();
            assertThat(jenkinsTheme, instanceOf(JenkinsTheme.StaticTheme.class));
            assertThat(((JenkinsTheme.StaticTheme) jenkinsTheme).theme(), instanceOf(NoOpThemeManagerFactory.class));
        }
    }
}
