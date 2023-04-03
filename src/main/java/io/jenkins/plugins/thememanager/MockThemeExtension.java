package io.jenkins.plugins.thememanager;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class MockThemeExtension extends ThemeExtension {

    private final String hello;

    @DataBoundConstructor
    public MockThemeExtension(String hello) {
        this.hello = hello;
    }

    public String getHello() {
        return hello;
    }

    @Extension
    public static class DescriptorImpl extends ThemeExtensionDescriptor {

    }
}
