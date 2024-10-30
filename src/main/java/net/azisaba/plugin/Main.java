package net.azisaba.plugin;

import com.github.bea4dev.artgui.ArtGUI;

public interface Main {

    void registerCommands();

    void registerListeners();

    void registerClockTimer();

    ArtGUI getArtGUI();
}
