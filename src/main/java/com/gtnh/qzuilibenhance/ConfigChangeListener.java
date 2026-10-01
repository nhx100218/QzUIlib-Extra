package com.gtnh.qzuilibenhance;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** 配置页保存后即时回灌运行态字段。 */
public class ConfigChangeListener {

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MyMod.MODID.equals(event.modID)) {
            Config.apply(Config.getConfiguration());
            MyMod.LOG.info("附属增强配置已即时生效");
        }
    }
}
