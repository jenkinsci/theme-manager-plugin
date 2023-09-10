# Theme Manager plugin for Jenkins

[![Build Status](https://ci.jenkins.io/job/Plugins/job/theme-manager-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/theme-manager-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/theme-manager-plugin.svg)](https://github.com/jenkinsci/theme-manager-plugin/graphs/contributors)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/theme-manager.svg)](https://plugins.jenkins.io/theme-manager)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/theme-manager-plugin.svg?label=changelog)](https://github.com/jenkinsci/theme-manager-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/theme-manager.svg?color=blue)](https://plugins.jenkins.io/theme-manager)


## Introduction

Adds theme management to Jenkins, at a global or user level.

## Getting started

There's normally no need to install this plugin directly as the theme you install will depend on this.

First you will want to install a theme plugin through the Jenkins Update Center.

### Known themes

* [Dark theme](https://github.com/jenkinsci/dark-theme-plugin)
* [Solarized theme](https://plugins.jenkins.io/solarized-theme/)
* [Material theme](https://plugins.jenkins.io/material-theme/)

_Just send a pull request to add your theme to the list if create one_

### Configuring the plugin

You can configure this plugin globally, or each user can configure it in their configuration as well.

### Global

Manage Jenkins → Configure System → Themes

![Global configuration](docs/images/global-theme-manager.png)

You can stop users from being able to change their theme by selecting the 
'Do not allow users to select a different theme' checkbox

### User

'Your name' profile link (in top right) → Configure → Themes

### Configuration as Code example

From Jenkins 2.421:
```yaml
appearance:
  themeManager:
    disableUserThemes: true
    theme: "noOp" # noOp is no theme, change this to the name of the theme plugin you're using, i.g. 'darkSystem'
```

Previously:
```yaml
unclassified:
  themeManager:
    disableUserThemes: true
    theme: "noOp" # noOp is no theme, change this to the name of the theme plugin you're using, i.g. 'darkSystem'
```

## Creating a new theme

See our [developer guide](docs/developer-guide.md).

## Contributing

Refer to our [contribution guidelines](.github/CONTRIBUTING.md).

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md).
