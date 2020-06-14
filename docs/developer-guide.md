# How to create a theme

A theme is a Jenkins plugin that includes:

* 0 or more CSS files
* 0 or more JavaScript files

Theme discovery in Jenkins uses the [ThemeManagerFactory](https://github.com/jenkinsci/theme-manager-plugin/blob/master/src/main/java/io/jenkins/plugins/thememanager/ThemeManagerFactory.java)
extension point.

## Getting started

Create a plugin from the `empty plugin` [Jenkins archetype](https://github.com/jenkinsci/archetypes/).

In the properties section of your `pom.xml` add `<useBeta>true</useBeta>`

You will need to extend/implement:

* [ThemeManagerFactory](https://github.com/jenkinsci/theme-manager-plugin/blob/master/src/main/java/io/jenkins/plugins/thememanager/ThemeManagerFactory.java)
* [ThemeManagerFactoryDescriptor](https://github.com/jenkinsci/theme-manager-plugin/blob/master/src/main/java/io/jenkins/plugins/thememanager/ThemeManagerFactoryDescriptor.java)
* [UnprotectedRootAction](https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/model/UnprotectedRootAction.java)

Take a look at the [dark-theme-plugin](https://github.com/jenkinsci/dark-theme-plugin) for a sample implementation.

Don't forget to add `@Extension` to your `descriptor` and `UnprotectedRootAction`.

Add a symbol to your `descriptor` for a nice short name when using configuration-as-code, e.g. `@Symbol("neo2")`.

## Testing your theme

Run `mvn hpi:run` from your plugin directory, that will startup a test instance of Jenkins.

Visit http://localhost:8080/jenkins in your browser and configure your theme from there.

### Host your plugin

Follow the [hosting guide](https://www.jenkins.io/doc/developer/publishing/requesting-hosting/).

### Let others know about it

Send a pull request to this plugin's README to add it to the known plugins list (once the plugin is in the Jenkins update center).
