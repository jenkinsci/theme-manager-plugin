package io.jenkins.plugins.thememanager;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

public class PageDecoratorTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testJavaScriptLoadsOnSimpleAndRegularPages() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy auth =
                new MockAuthorizationStrategy().grant(Jenkins.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(auth);

        JenkinsRule.WebClient wc = j.createWebClient();

        wc.login("alice");
        wc.goTo("");
    }
}
