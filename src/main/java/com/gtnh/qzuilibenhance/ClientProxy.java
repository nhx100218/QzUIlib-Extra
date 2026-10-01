package com.gtnh.qzuilibenhance;

import com.gtnh.qzuilibenhance.tooltip.ModularUiTooltipHandler;
import com.gtnh.qzuilibenhance.tooltip.TooltipForgeHandler;

import com.gtnh.qzuilibenhance.ConfigChangeListener;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // 接管 NEI/GTNHLib 的 tooltip 渲染。
        TooltipForgeHandler.install();
        // 放宽 ModularUI2（格雷）tooltip 折行宽度。
        ModularUiTooltipHandler.install();
        // 配置页保存后即时生效
        FMLCommonHandler.instance().bus().register(new ConfigChangeListener());
        // Ctrl+K 打开配置页
        FMLCommonHandler.instance().bus().register(new com.gtnh.qzuilibenhance.client.ConfigKeyHandler());
    }
}
