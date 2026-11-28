package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public final class TestThemes {

    private TestThemes() {}

    public static class Dark extends ThemeManagerFactory {
        @DataBoundConstructor
        public Dark() {}

        @Override
        public Theme getTheme() {
            return Theme.builder()
                    .withProperty("prism-api", "theme", "tomorrow")
                    .build();
        }

        @Extension
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

    public static class Light extends ThemeManagerFactory {
        @DataBoundConstructor
        public Light() {}

        @Override
        public Theme getTheme() {
            return Theme.builder().withProperty("prism-api", "theme", "prism").build();
        }

        @Extension
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
}
