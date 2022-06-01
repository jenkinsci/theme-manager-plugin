package io.jenkins.plugins.thememanager;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ThemeExtensionDescriptor extends Descriptor<ThemeExtension> {

    /**
     * All implementations of {@link ThemeExtension} and {@link ThemeExtensionDescriptor}.
     *
     * @return descriptor extension list containing all themes.
     */
    public static DescriptorExtensionList<ThemeExtension, ThemeExtensionDescriptor> all() {
        return Jenkins.get().getDescriptorList(ThemeExtension.class);
    }
}
