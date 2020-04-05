package pokmon987.hammerandvil;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pokmon987.hammerandvil.blocks.ModBlocks;
import pokmon987.hammerandvil.items.ModItems;
import pokmon987.hammerandvil.recipes.ModRecipes;
import pokmon987.hammerandvil.tileentity.ModTiles;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.init();
		ModItems.init();
		ModTiles.init();
	}
	public void init(FMLInitializationEvent event) {
		ModRecipes.init();
	}
	public void postInit(FMLPostInitializationEvent event) {
		
	}
}