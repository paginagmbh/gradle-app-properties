# Gradle App Properties

A Gradle plugin that bakes build-time metadata — version, name, build date, and copyright holder — into a `meta.properties` file on the classpath, and exposes it through a lightweight `AppProperties` utility class at runtime.


## How it works

At build time the plugin registers a `createProperties` task that writes `build/resources/main/meta.properties`. The companion `AppProperties` class (added to your `implementation` classpath automatically) reads that file via `ResourceBundle` at runtime.

The plugin and the runtime utility are intentionally split into two artifacts so that the Gradle API never leaks onto your application's runtime classpath:

| Artifact                | Classpath                           |
|:------------------------|:------------------------------------|
| `app-properties-plugin` | Gradle buildscript only             |
| `app-properties-lib`    | Your application's `implementation` |


## Requirements

- Gradle 7.4+
- Java 11+


## Usage

### Apply the plugin

```kotlin
// build.gradle.kts
plugins {
    id("gmbh.pagina.tools.gradle.app-properties") version "1.1"
}
```

```groovy
// build.gradle
plugins {
    id 'gmbh.pagina.tools.gradle.app-properties' version '1.1'
}
```

That's it.
The `app-properties-lib` jar is added to your `implementation` dependencies automatically — no extra `dependencies { }` block needed.


### Configure (optional)

All parameters are optional.

| Parameter          | Type      | Default          | Description                                                                                                                     |
|:-------------------|:----------|:-----------------|:--------------------------------------------------------------------------------------------------------------------------------|
| `displayName`      | `String`  | `project.name`   | Human-readable application name written as `name` in `meta.properties`                                                          |
| `copyrightHolder`  | `String`  | `""` (omitted)   | Copyright holder, e.g. `"pagina GmbH, Tübingen"`. Empty string suppresses the copyright notice entirely                         |
| `copyrightFromYear`| `Integer` | absent (omitted) | First year of the copyright range. When set and earlier than the build year, produces `"© 2020-2026 …"` instead of `"© 2026 …"` |

```kotlin 
appProperties {
    displayName       = "My Application"        // default: project.name
    copyrightHolder   = "pagina GmbH, Tübingen" // default: "" (omitted)
    copyrightFromYear = 2020                    // default: absent — shows build year only
}
```


## Runtime API

`AppProperties` is a final utility class with the following `public static final` fields:

| Field                | Type        | Content                                                                                                                                          |
|:---------------------|:------------|:-------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`            | `String`    | `project.version` at build time                                                                                                                  |
| `name`               | `String`    | Value of `displayName`                                                                                                                           |
| `buildDate`          | `LocalDate` | Date of the build                                                                                                                                |
| `copyrightHolder`    | `String`    | Value of `copyrightHolder`, or `""` if not set                                                                                                   |
| `copyrightFromYear`  | `String`    | Value of `copyrightFromYear` as a string, or `""` if not set                                                                                     |
| `copyrightString`    | `String`    | `"© <year> <holder>"` or `"© <fromYear>-<year> <holder>"` when `copyrightFromYear` is set and earlier than the build year; `""` if holder is empty |
| `versionDescriptor`  | `String`    | `"<name> <version>, built <date>"`, with `copyrightString` appended when non-empty                                                               |

```java
// copyrightHolder set, no copyrightFromYear
System.out.println(AppProperties.copyrightString);
// © 2026 pagina GmbH, Tübingen

// copyrightHolder set, copyrightFromYear = 2020
System.out.println(AppProperties.copyrightString);
// © 2020-2026 pagina GmbH, Tübingen

System.out.println(AppProperties.versionDescriptor);
// My Application 1.2.0, built 2026-03-31 © 2020-2026 pagina GmbH, Tübingen

System.out.println(AppProperties.buildDate.getYear());
// 2026
```


## Project structure

```
app-properties/
├── plugin/       # Gradle plugin  (AppPropertiesPlugin, CreatePropertiesTask, AppPropertiesExtension)
└── lib/          # Runtime utility (AppProperties)
```


## (internal only) Publish New Versions

A merge into *main* automatically publishes the next version to Artifactory.
A push to *development* instead publishes a *-SNAPSHOT* version.
For this the pagian artifactory access needs to be setup locally.
This repository contains a git submodule to configure this.
It is not needed when not deploying snapshots.
You can also publish to Artifactory locally:

```sh
./gradlew publish
```

For local testing, use:

```sh
./gradlew publishToMavenLocal
```
