package ray.mintcat.blockhead;

import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.inject.TInject;

@Plugin.Version(5.34)
public class BlockHead extends Plugin {

    @TInject(value = "settings.yml", locale = "LOCALE-PRIORITY")
    public static final TConfig settings = null;
    public static String getTitle() {
        return settings.getString("plugin.title","System: ");
    }
}
