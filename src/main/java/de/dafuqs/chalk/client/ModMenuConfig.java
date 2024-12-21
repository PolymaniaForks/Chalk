package de.dafuqs.chalk.client;

import com.terraformersmc.modmenu.api.*;
import de.dafuqs.chalk.config.*;
import me.shedaniel.autoconfig.*;
import net.fabricmc.api.*;

@Environment(EnvType.CLIENT)
public class ModMenuConfig implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(ChalkConfig.class, parent).get();
	}

}