package com.gtnh.qzuilibenhance.client;

import java.util.ArrayList;
import java.util.List;

import com.gtnh.qzuilibenhance.Config;
import com.gtnh.qzuilibenhance.MyMod;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

/** 附属增强的原版风格配置页（Forge 自动生成）。 */
public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, elements(), MyMod.MODID, false, false, "Qz UILib Enhance");
    }

    private static List<IConfigElement> elements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        Configuration cfg = Config.getConfiguration();
        if (cfg != null) {
            for (String category : cfg.getCategoryNames()) {
                list.add(new ConfigElement(cfg.getCategory(category)));
            }
        }
        return list;
    }
}
