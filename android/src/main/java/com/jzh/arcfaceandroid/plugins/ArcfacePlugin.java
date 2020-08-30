package com.jzh.arcfaceandroid.plugins;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * <p></p>
 * <p></p>
 *
 * @author jinzhenhua
 * @version 1.0  ,create at:2020/7/23 10:01
 */
public class ArcfacePlugin {
    public static void registerWith(final PluginRegistry.Registrar registrar) {
        (new MethodChannel(registrar.messenger(), ArcfaceCheckImageHandler.CHANNEL)).
                setMethodCallHandler(new ArcfaceCheckImageHandler(registrar.activity(), registrar.context(), registrar.messenger()));
    }
}
