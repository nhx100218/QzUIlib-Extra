package com.gtnh.qzuilibenhance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * QzUILib 附属增强模组：圆角 tooltip（含 NEI/GT/匠魂炉/Waila）、平滑滚动与界面切换、
 * 内置 Inter Frozen + 思源黑体、以及相关配置自修正。不侵入 QzUILib 内核。
 */
@Mod(
        modid = MyMod.MODID,
        version = "1.0",
        name = "Qz UILib Enhance",
        acceptedMinecraftVersions = "[1.7.10]",
        guiFactory = "com.gtnh.qzuilibenhance.client.EnhanceGuiFactory",
        dependencies = "after:qz_uilib")
public class MyMod {

    public static final String MODID = "qzuilibenhance";
    public static final Logger LOG = LogManager.getLogger("QzUILibEnhance");

    @SidedProxy(clientSide = "com.gtnh.qzuilibenhance.ClientProxy", serverSide = "com.gtnh.qzuilibenhance.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
