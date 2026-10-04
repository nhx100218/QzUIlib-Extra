package com.gtnh.qzuilibenhance;

import com.gtnh.qzuilibenhance.tooltip.ModularUiTooltipHandler;
import com.gtnh.qzuilibenhance.tooltip.TooltipForgeHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // QzUILib Config 模块加载（scoped 客户端：配置只影响客户端渲染/交互）。
        Config.load(event);
        // 接管 NEI/GTNHLib 的 tooltip 渲染。
        TooltipForgeHandler.install();
        // 放宽 ModularUI2（格雷）tooltip 折行宽度。
        ModularUiTooltipHandler.install();
        // Ctrl+K 打开配置页
        FMLCommonHandler.instance().bus().register(new com.gtnh.qzuilibenhance.client.ConfigKeyHandler());
    }
}
