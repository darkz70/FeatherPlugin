# 🪶 FeatherPlugin

> All-in-one Gradle plugin for Minecraft mod development with **Fabric · Quilt · Forge · NeoForge** support.
> Inspired by [MossyPlugin] by LopyMine(https://github.com/darkz/FeatherPlugin) by darkz — extended with a full multi-loader system.

---

## Modules

| Plugin ID | Module | Applied in | Purpose |
|---|---|---|---|
| `net.darkz.feather.feather-core` | `core` | `build.gradle` | Registers the `feather {}` DSL, sets default repos |
| `net.darkz.feather.feather-loader` | `loader` | `build.gradle` | Applies the correct loader toolchain (Loom / ForgeGradle / ModDevGradle) |
| `net.darkz.feather.feather-stonecutter` | `stonecutter` | `build.gradle` | Injects loader boolean constants into Stonecutter |
| `net.darkz.feather.feather-settings` | `settings` | `settings.gradle` | DSL to declare MC version × loader matrix for Stonecutter |

---

## Quick start

### 1. `settings.gradle`

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url "https://maven.kikugie.dev/releases" }
        maven { url "https://maven.darkz.com/releases" }
        // loader-specific repos …
        maven { url "https://maven.fabricmc.net/" }
        maven { url "https://maven.neoforged.net/releases/" }
    }
}

plugins {
    id 'dev.kikugie.stonecutter'               version '0.5.4'
    id 'net.darkz.feather.feather-settings' version '1.0.0'
}

featherVersions {
    version("1.21.1", "fabric", "quilt", "neoforge")
    version("1.20.1", "fabric", "forge")
}

stonecutter {
    centralScript = "build.gradle"
    create rootProject
}
```

### 2. `build.gradle` (controller)

```groovy
plugins {
    id 'dev.kikugie.stonecutter'
    id 'net.darkz.feather.feather-core'        version '1.0.0' apply false
    id 'net.darkz.feather.feather-loader'      version '1.0.0' apply false
    id 'net.darkz.feather.feather-stonecutter' version '1.0.0' apply false
}
```

### 3. `versions/build.gradle` (each versioned sub-project)

```groovy
plugins {
    id 'net.darkz.feather.feather-core'
    id 'net.darkz.feather.feather-loader'
    id 'net.darkz.feather.feather-stonecutter'
}

def (mcVersion, loaderName) = stonecutter.current.project.split('-', 2) as List

feather {
    loader           = loaderName
    minecraftVersion = mcVersion
    loaderVersion    = property("${loaderName}_version")
    modId            = "my_mod"
}
```

### 4. Loader-conditional code with Stonecutter

```java
//? if fabric {
FabricLoader.getInstance().getObjectShare().put(MY_KEY, value);
//?}

//? if neoforge {
ModList.get().getModContainerById(MOD_ID).ifPresent(c -> c.registerConfig(...));
//?}

//? if forge_like {
// Applies to both forge AND neoforge
//?}

//? if fabric_like {
// Applies to both fabric AND quilt
//?}
```

---

## `feather { }` DSL reference

| Property | Type | Required | Description |
|---|---|---|---|
| `loader` | `String` | ✅ | One of: `fabric`, `quilt`, `forge`, `neoforge` |
| `minecraftVersion` | `String` | ✅ | e.g. `"1.21.1"` |
| `loaderVersion` | `String` | ✅ | Fabric Loader / Quilt Loader / Forge / NeoForge version |
| `modId` | `String` | — | Mod ID for metadata processing |
| `mavenGroup` | `String` | — | Overrides `project.group` for publishing |
| `includeDefaultRepositories` | `Boolean` | — | Add loader repos automatically (default `true`) |

---

## Project structure

```
FeatherPlugin/
├── build.gradle                   ← root – configures all sub-projects
├── settings.gradle                ← includes module:* sub-projects
├── gradle.properties              ← versions for all deps
│
├── module/
│   ├── common/                    ← ModLoader enum, FeatherExtension, FeatherBasePlugin
│   ├── core/                      ← feather-core plugin
│   ├── loader/                    ← feather-loader plugin + per-loader configurators
│   │   └── configurator/
│   │       ├── FabricConfigurator.java
│   │       ├── QuiltConfigurator.java
│   │       ├── ForgeConfigurator.java
│   │       └── NeoForgeConfigurator.java
│   ├── stonecutter/               ← feather-stonecutter plugin
│   └── settings/                  ← feather-settings plugin + FeatherVersionsExtension
│
└── example-mod/                   ← ready-to-use example mod project
    ├── settings.gradle
    ├── build.gradle
    ├── gradle.properties
    └── versions/build.gradle
```

---

## Building

```bash
./gradlew buildAll        # build all module JARs → ./libs/
./gradlew publishToMaven  # publish to mavenLocal
```

---

## License

LGPL-3.0 – same as MossyPlugin.
