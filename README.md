# TabooLib SDK

## Basic information
```groovy
ext {
    main = 'me.skymc.exmaple'
    author = 'Bkm016'
    version = '1.0-SNAPSHOT'
    redefine = false // When you use Kotlin as the main class, it must be enabled.
}
```

### Main class
In `Java`
```java
import io.izzel.taboolib.loader.Plugin

@Plugin.Version(5.0)
public class Main extends Plugin {

    // ...
}
```

In `Kotlin`
```kotlin
import io.izzel.taboolib.loader.PluginRedefine

@Plugin.Version(5.0)
class Main : PluginRedefine() {

    // ...
}
```