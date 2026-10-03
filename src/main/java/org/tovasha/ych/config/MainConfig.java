package org.tovasha.ych.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "yourcustomhud")
@Getter
@Setter
public class MainConfig implements ConfigData {
    public boolean enabled = true;
    public boolean renderAboveAll = false;
    public boolean lightTheme = false;
    public String activePreset = "default";
}
