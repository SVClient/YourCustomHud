package org.tovasha.ych.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "yourcustomhud")
@Getter
@Setter
public class MainConfig implements ConfigData {
    private boolean enabled = true;
    private boolean renderAboveAll = false;
    private String activePreset = "default";
}
