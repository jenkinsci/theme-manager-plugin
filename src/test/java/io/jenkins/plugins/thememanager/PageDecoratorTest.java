package io.jenkins.plugins.thememanager;

import jenkins.model.Jenkins;
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
}
