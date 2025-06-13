# MixinConfig
A library for toggling Mixin Classes and Methods using a config and Annotation.

# Installing
MixinConfig is available through Jitpack:

[![](https://jitpack.io/v/Tater-Certified/MixinConfig.svg)](https://jitpack.io/#Tater-Certified/MixinConfig)

__Gradle__
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    include(implementation('com.github.Tater-Certified:MixinConfig:Tag'))
}
```

MixinConfig is loader-independent, meaning it will work on any platform that has Mixin support.

Requires **<u>Java 17 or greater</u>**.

Next you will need to add the MixinPluginConfig to your project.
The easiest way is to add it to your modid.mixins.json:
```json5
{
    "plugin": "com.github.tatercertified.mixin_config.MCMixinConfigPlugin",
}
```

If you already have a MixinConfigPlugin, you can add the code from [MCMixinConfigPlugin](https://github.com/Tater-Certified/MixinConfig/blob/main/src/main/java/com/github/tatercertified/mixin_config/MCMixinConfigPlugin.java)
to your Plugin.

# Using the library
The library contains the [@Config](https://github.com/Tater-Certified/MixinConfig/blob/main/src/main/java/com/github/tatercertified/mixin_config/annotations/Config.java) 
Annotation. This marks a Class or Method to be added to the config file.

Here is an example:
```java
@Config(name = "Example Name", defaultVal = true, dependencies = {"Dependency 1", "Dependency 2"})
@Mixin(MathHelper.class)
public MathHelperMixin {
}
```
The <b>name</b> specifies the name to appear in the config.

The <b>defaultVal</b> specifies the default toggle state of the Mixin (true = enabled).

The <b>dependencies</b> specifies all immediate dependencies of other Mixin Classes/Methods on this Mixin.

Also ensure that these values are set by your code in the MixinConfigPlugin class **in the onLoad() method before the super call**:
```java
@Override
public void onLoad(String s) {
    MixinConfig.init(
            FabricLoader.getInstance().getConfigDir().resolve("modid.txt"), // Config Path
            2,                                                              // Config Version
            MyClass.class                                                   // Class in Your Project
    );
    super(s);
}
```
or alternatively
```java
@Override
public void onLoad(String s) {
    MixinConfig.init(
            FabricLoader.getInstance().getConfigDir().resolve("modid.txt"),   // Config Path
            2,                                                                // Config Version
            FabricLoader.getInstance().getGameDir().resolve("mods/mymod.jar") // Path to your Jar
    );
    super(s);
}
```
# Config
```toml
Config Version = 0     # Current Config Version

[ Mixin Class ] = true # Mixin Class Declaration
  Method 1 = true      # Mixin Class Method Declaration
  Method 2 = false
  Method 3 = true

  Method 4 = true      # Mixin Classless Method Declaration
  Method 5 = true
```

# Debugging
You can use the JVM argument `-Dmixinconfig.verbose=true` to enable debug printing in the console

# License
This project is licensed on the MIT License

# Need Help?
Visit [our Discord](https://discord.gg/XGw3Te7QYr) for help!