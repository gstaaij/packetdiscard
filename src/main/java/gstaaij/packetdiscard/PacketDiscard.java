// Copyright (c) 2023-2024 gstaaij
// This code is licensed under LGPLv3

package gstaaij.packetdiscard;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketDiscard implements ModInitializer {
    public static final String MOD_ID = "packetdiscard";
    public static final String MOD_NAME = "PacketDiscard";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Display a warning message about the fact that clients will experience very weird behaviour. */
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.warn(MOD_NAME + " is installed. You will experience very weird behaviour.");
    }
}