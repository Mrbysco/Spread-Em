package com.mrbysco.spreadem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrbysco.spreadem.commands.CommandSpreadTree;
import com.mrbysco.spreadem.config.SpreadConfig;
import com.mrbysco.spreadem.handlers.SpreadHandler;
import com.mrbysco.spreadem.util.DirectoryUtil;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS,
        acceptableRemoteVersions = "*"//,
//        serverSideOnly = true
        )

public class SpreadEm
{
    @Mod.Instance(Reference.MOD_ID)
    public static SpreadEm instance;

    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public static HashMap<UUID, BlockPos> locationMap = new HashMap<>();

    @EventHandler
    public void PreInit(FMLPreInitializationEvent event)
    {
        logger.info("Registering config");
        MinecraftForge.EVENT_BUS.register(new SpreadConfig());
    }

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		try {
            DirectoryUtil.spreadLocations = new File(DimensionManager.getCurrentSaveRootDirectory(), "spreadlocations.txt");
            if(!DirectoryUtil.spreadLocations.exists()) {
                DirectoryUtil.spreadLocations.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
        locationMap = DirectoryUtil.buildLocationMap();
	}

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("Registering event handlers");
        MinecraftForge.EVENT_BUS.register(new SpreadHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandSpreadTree());
    }
}
