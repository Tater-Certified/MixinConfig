# MixinConfig
A library for toggling Mixin Classes and Methods using a config and Annotation.

# Installing
MixinConfig is available through Jitpack:

__Gradle__
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    include(implementation("com.github.Tater-Certified:MixinConstraints:Tag"))
}
```

MixinConfig is loader-independent, meaning it will work on any platform that has Mixin support.

Requires **Java 17 or greater**.

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
            FabricLoader.getInstance().getConfigDir().toString() + "modid", // Config Path
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
            FabricLoader.getInstance().getConfigDir().toString() + "modid",   // Config Path
            2,                                                                // Config Version
            FabricLoader.getInstance().getGameDir().resolve("mods/mymod.jar") // Path to your Jar
    );
    super(s);
}
```
# Config
```toml
Config Version = 2 # Config Version
[ Class0 = true ]  # Class
 Method01 = true   # Methods
 Method02 = false
 Method03 = true

[ Class1 = false ]
 Method11 = false
 Method12 = true

 Method21 = true   # Classless Methods
 Method22 = false
```

# License
This project is licensed on the MIT License

# Need Help?
Visit [our Discord](https://discord.gg/XGw3Te7QYr) for help!