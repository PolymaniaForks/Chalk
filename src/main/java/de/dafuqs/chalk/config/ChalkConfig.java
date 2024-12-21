package de.dafuqs.chalk.config;

import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.*;
import net.minecraft.client.*;

import java.util.*;

@Config(name = "Chalk")
public class ChalkConfig implements ConfigData {
	
	public boolean EmitParticles = true;
	
	@Override
	public void validatePostLoad() {
	
	}
	
}
