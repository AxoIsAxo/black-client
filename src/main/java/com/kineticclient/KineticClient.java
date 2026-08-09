package com.kineticclient;

import com.kineticclient.config.Config;
import com.kineticclient.hack.HackManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KineticClient implements ClientModInitializer {

    public static final String MOD_ID = "kineticclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        HackManager.INSTANCE.registerDefaults();
        LOGGER.info("[KineticClient] initialized. Press right Shift to open the menu.");
    }
}
