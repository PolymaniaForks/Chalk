package de.dafuqs.chalk.client;

import de.dafuqs.chalk.common.*;
import net.fabricmc.api.*;

public class ChalkClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        for (ChalkRegistry.ChalkVariant chalkVariant : ChalkRegistry.chalkVariants.values()) {
            chalkVariant.registerClient();
        }
    }
}
