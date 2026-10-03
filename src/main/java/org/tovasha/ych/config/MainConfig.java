package org.tovasha.ych.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "yourcustomhud")
public class MainConfig implements ConfigData {
    public boolean enabled = true;
}
