package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Collections;
import java.util.Set;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension(optional = true)
@Restricted(NoExternalUse.class)
public class JenkinsThemeConfigurator implements Configurator<JenkinsTheme> {

    @Override
    public Class<JenkinsTheme> getTarget() {
        return JenkinsTheme.class;
    }

    @NonNull
    @Override
    public Set<Attribute<JenkinsTheme, ?>> describe() {
        return Collections.emptySet();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public JenkinsTheme configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
        if (config == null) {
            return new JenkinsTheme.StaticTheme(new NoOpThemeManagerFactory());
        }

        if (config.getType() == CNode.Type.MAPPING) {
            Mapping mapping = config.asMapping();
            if (mapping.size() == 1) {
                if (mapping.containsKey("system")) {
                    Configurator<JenkinsTheme.SystemTheme> systemConfigurator =
                            context.lookupOrFail(JenkinsTheme.SystemTheme.class);
                    return systemConfigurator.configure(mapping.get("system"), context);
                }
                if (mapping.containsKey("static")) {
                    Configurator<JenkinsTheme.StaticTheme> staticConfigurator =
                            context.lookupOrFail(JenkinsTheme.StaticTheme.class);
                    return staticConfigurator.configure(mapping.get("static"), context);
                }
            }
        }

        Configurator<ThemeManagerFactory> factoryConfigurator = context.lookupOrFail(ThemeManagerFactory.class);
        ThemeManagerFactory factory = factoryConfigurator.configure(config, context);
        return new JenkinsTheme.StaticTheme(factory);
    }

    @Override
    public JenkinsTheme check(CNode config, ConfigurationContext context) throws ConfiguratorException {
        return configure(config, context);
    }

    @CheckForNull
    @Override
    @SuppressWarnings("unchecked")
    public CNode describe(JenkinsTheme instance, ConfigurationContext context) throws Exception {
        return switch (instance) {
            case null -> null;
            case JenkinsTheme.StaticTheme(var theme) -> {
                Configurator<ThemeManagerFactory> factoryConfigurator = context.lookupOrFail(ThemeManagerFactory.class);
                yield factoryConfigurator.describe(theme, context);
            }
            case JenkinsTheme.SystemTheme sy -> {
                Configurator<JenkinsTheme.SystemTheme> systemConfigurator =
                        context.lookupOrFail(JenkinsTheme.SystemTheme.class);
                CNode node = systemConfigurator.describe(sy, context);
                Mapping mapping = new Mapping();
                mapping.put("system", node);
                yield mapping;
            }
        };
    }
}
