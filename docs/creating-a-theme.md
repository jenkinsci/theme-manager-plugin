# Creating a theme

A theme is a Jenkins plugin that includes:

* 1 or more CSS files
* 0 or more JavaScript files

Theme discovery in Jenkins uses the [ThemeManagerFactory](https://github.com/jenkinsci/theme-manager-plugin/blob/master/src/main/java/io/jenkins/plugins/thememanager/ThemeManagerFactory.java)
extension point.

## Getting started

Create a plugin from the `theme plugin` [Jenkins archetype](https://github.com/jenkinsci/archetypes/).

You will need to update the TODO sections your `theme.css`, found in `src/main/webapp`.

Take a look at the [dark-theme-plugin](https://github.com/jenkinsci/dark-theme-plugin) or [catppuccin-theme-plugin](https://github.com/jenkinsci/catppuccin-theme-plugin) for a sample implementation.

You can find all the available variables provided by Jenkins core in [theme.less](https://github.com/jenkinsci/jenkins/blob/master/war/src/main/less/abstracts/theme.less). Plugins may also define their own variables so this list may not be complete, but it should cover most of them.

## Testing your theme

Run `mvn hpi:run` from your plugin directory, that will startup a test instance of Jenkins.

Visit http://localhost:8080/jenkins in your browser and configure your theme from there.

### Host your plugin

Follow the [hosting guide](https://www.jenkins.io/doc/developer/publishing/requesting-hosting/).

### Let others know about it

Send a pull request to this plugin's README to add it to the known plugins list (once the plugin is in the Jenkins update center).
