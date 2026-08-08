package com.blackclient;

import com.blackclient.config.Config;
import com.blackclient.hack.HackManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackClient implements ClientModInitializer {

    public static final String MOD_ID = "blackclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        HackManager.INSTANCE.registerDefaults();
        LOGGER.info("[BlackClient] initialized. Press right Shift to open the menu.");
    }
}
