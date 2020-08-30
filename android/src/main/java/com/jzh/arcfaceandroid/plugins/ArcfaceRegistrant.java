package com.jzh.arcfaceandroid.plugins;

import io.flutter.plugin.common.PluginRegistry;

/**
 * <p></p>
 * <p></p>
 *
 * @author jinzhenhua
 * @version 1.0  ,create at:2020/7/23 11:21
 */
public class ArcfaceRegistrant {
    public static void registerWith(PluginRegistry registry) {
        if (alreadyRegisteredWith(registry)) {
            return;
        }
        ArcfacePlugin.registerWith(registry.registrarFor("ArcfacePlugin"));
    }

    private static boolean alreadyRegisteredWith(PluginRegistry registry) {
        final String key = ArcfaceRegistrant.class.getCanonicalName();
        if (registry.hasPlugin(key)) {
            return true;
        }
        registry.registrarFor(key);
        return false;
    }
}
