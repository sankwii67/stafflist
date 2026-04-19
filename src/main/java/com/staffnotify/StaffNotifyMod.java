package com.staffnotify;

import com.staffnotify.command.StaffCommand;
import com.staffnotify.config.StaffNotifyConfig;
import com.staffnotify.event.StaffEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class StaffNotifyMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        StaffNotifyConfig.load();
        StaffCommand.register();
        StaffEventHandler.register();
        ClientTickEvents.END_CLIENT_TICK.register(StaffEventHandler::onClientTick);
    }
}
