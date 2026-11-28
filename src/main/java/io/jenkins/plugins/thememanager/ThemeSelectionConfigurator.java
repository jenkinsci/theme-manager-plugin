package io.jenkins.plugins.thememanager;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.impl.attributes.DescribableAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Set;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension(optional = true)
@Restricted(NoExternalUse.class)
public class ThemeSelectionConfigurator implements Configurator<ThemeSelection> {

    @Override
    public Class<ThemeSelection> getTarget() {
        return ThemeSelection.class;
    }

    @NonNull
    @Override
    public Set<Attribute<ThemeSelection, ?>> describe() {
        return Set.of(
                new DescribableAttribute<ThemeSelection, ThemeManagerFactory>("light", ThemeManagerFactory.class)
                        .getter(ThemeSelection::getLight),
                new DescribableAttribute<ThemeSelection, ThemeManagerFactory>("dark", ThemeManagerFactory.class)
                        .getter(ThemeSelection::getDark));
    }

    @NonNull
    @Override
    public ThemeSelection configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
        if (config == null) {
            return ThemeSelection.none();
        }

        Configurator<ThemeManagerFactory> factoryConfigurator = context.lookupOrFail(ThemeManagerFactory.class);

        if (config.getType() == CNode.Type.MAPPING) {
            Mapping mapping = config.asMapping();
            if (mapping.containsKey("light") || mapping.containsKey("dark")) {
                for (String key : mapping.keySet()) {
                    if (!key.equals("light") && !key.equals("dark")) {
                        throw new ConfiguratorException(
                                this, "Invalid configuration element for ThemeSelection: " + key);
                    }
                }
                ThemeManagerFactory light = mapping.containsKey("light")
                        ? factoryConfigurator.configure(mapping.get("light"), context)
                        : new NoOpThemeManagerFactory();
                ThemeManagerFactory dark = mapping.containsKey("dark")
                        ? factoryConfigurator.configure(mapping.get("dark"), context)
                        : new NoOpThemeManagerFactory();
                return new ThemeSelection(light, dark);
            }
        }

        ThemeManagerFactory factory = factoryConfigurator.configure(config, context);
        return ThemeSelection.of(factory);
    }

    @Override
    public ThemeSelection check(CNode config, ConfigurationContext context) throws ConfiguratorException {
        return configure(config, context);
    }

    @CheckForNull
    @Override
    public CNode describe(ThemeSelection instance, ConfigurationContext context) throws Exception {
        if (instance == null) {
            return null;
        }

        Configurator<ThemeManagerFactory> factoryConfigurator = context.lookupOrFail(ThemeManagerFactory.class);
        Mapping mapping = new Mapping();
        mapping.put("dark", factoryConfigurator.describe(instance.getDark(), context));
        mapping.put("light", factoryConfigurator.describe(instance.getLight(), context));
        return mapping;
    }
}
