package com.mrbysco.spreadem.handlers;

import com.mrbysco.spreadem.Reference;
import com.mrbysco.spreadem.SpreadEm;
import com.mrbysco.spreadem.config.SpreadConfig;
import com.mrbysco.spreadem.util.DirectoryUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.UUID;

public class SpreadHandler {
    public static final String FIRST_JOIN_TAG = Reference.MOD_PREFIX + "firstJoin";
    public static BlockPos worldSpawn = null;

    @SubscribeEvent
    public void playerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        if(!player.world.isRemote)
        {
            if(!event.isEndConquered()) {
                if(player.dimension == SpreadConfig.general.spreadDimension) {
                    if(player.getBedLocation() == null) {
                        setSpawn((EntityPlayerMP)player); //Get spawn from list otherwise spread player.
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void loggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if(!player.world.isRemote && player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP)player;
            NBTTagCompound playerData = playerMP.getEntityData();
            NBTTagCompound data = getTag(playerData, EntityPlayer.PERSISTED_NBT_TAG);

            if(worldSpawn == null) {
                worldSpawn = playerMP.getServerWorld().getSpawnPoint();
            }

            if(!data.getBoolean(FIRST_JOIN_TAG))
            {
                spreadPlayer(playerMP);
                data.setBoolean(FIRST_JOIN_TAG, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
    }

    private static NBTTagCompound getTag(NBTTagCompound tag, String key) {
        if(tag == null || !tag.hasKey(key)) {
            return new NBTTagCompound();
        }
        return tag.getCompoundTag(key);
    }

    public void setSpawn(EntityPlayerMP playerIn) {
        if(SpreadEm.instance.locationMap.isEmpty()) {
            spreadPlayer(playerIn);
        } else {
            if(DirectoryUtil.containsUUID(playerIn.getUniqueID())) {
                HashMap.Entry<UUID, BlockPos> storage = DirectoryUtil.getMatchingLocationStorage(playerIn.getUniqueID());
                BlockPos storedPos = storage.getValue();
                playerIn.setSpawnPoint(storedPos, true);

                playerIn.setPositionAndUpdate(storedPos.getX(), storedPos.getY(), storedPos.getZ());

                for(;;) {
                    playerIn.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
                    if (playerIn.getServerWorld().getCollisionBoxes(playerIn, playerIn.getEntityBoundingBox()).isEmpty() && !(playerIn.posY < 256.0D)) break;
                }

            } else {
                spreadPlayer(playerIn);
            }
        }
    }

    public void spreadPlayer(EntityPlayerMP playerIn) {
        if(SpreadEm.instance.locationMap.isEmpty()) {
            generateAndSpreadPlayer(playerIn);
        } else {
            if(DirectoryUtil.containsUUID(playerIn.getUniqueID())) {
                HashMap.Entry<UUID, BlockPos> storage = DirectoryUtil.getMatchingLocationStorage(playerIn.getUniqueID());
                if(storage != null) {
                    BlockPos pos = storage.getValue();
                    playerIn.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                }
            } else {
                generateAndSpreadPlayer(playerIn);
            }
        }
    }

    public static BlockPos generateAndSpreadPlayer(EntityPlayerMP playerIn) {        
        if(worldSpawn == null) {
            worldSpawn = playerIn.getServerWorld().getSpawnPoint();
        }
        if (SpreadEm.instance.locationMap.isEmpty()) {
            SpreadEm.locationMap = DirectoryUtil.buildLocationMap();
            BlockPos newPos = generateNewLocation(playerIn.getServerWorld(), worldSpawn);
            playerIn.setPositionAndUpdate(newPos.getX(), newPos.getY(), newPos.getZ());
            playerIn.setSpawnPoint(newPos, false);
            SpreadEm.logger.info(playerIn.getName() + "'s spawn has been set to: " + newPos);
            DirectoryUtil.addToLocations(newPos, playerIn.getUniqueID());
        } else {
            if (DirectoryUtil.containsUUID(playerIn.getUniqueID())) {
                HashMap.Entry<UUID, BlockPos> storage = DirectoryUtil.getMatchingLocationStorage(playerIn.getUniqueID());
                if (storage != null) {
                    BlockPos pos = storage.getValue();
                    playerIn.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                }
            } else {
                BlockPos newPos = generateNewLocation(playerIn.getServerWorld(), worldSpawn);
                SpreadEm.locationMap = DirectoryUtil.buildLocationMap();
                
                playerIn.setPositionAndUpdate(newPos.getX(), newPos.getY(), newPos.getZ());
                playerIn.setSpawnPoint(newPos, false);
                SpreadEm.logger.info(playerIn.getName() + "'s spawn has been set to: " + newPos);
                DirectoryUtil.addToLocations(newPos, playerIn.getUniqueID());
            }
        }

        return null;
    }

    public static BlockPos generateNewLocation(WorldServer worldIn, BlockPos oldLocation) {
        if(worldSpawn == null) {
            worldSpawn = worldIn.getSpawnPoint();
        }
        BlockPos newPos = randomLoc(worldIn, oldLocation);
        boolean flag = DirectoryUtil.containsPos(newPos) || worldIn.getBlockState(newPos).getBlock() instanceof BlockLiquid || newPos.equals(worldSpawn) || isInBlacklistedBiome(worldIn.getBiome(newPos).getRegistryName());
        for(;;) {
            newPos = randomLoc(worldIn, newPos);
            if (!flag) break;
        }
        return newPos;
    }

    public static boolean isInBlacklistedBiome(ResourceLocation biome) {
        return SpreadConfig._blacklistedBiomes.contains(biome);
    }

    public static BlockPos randomLoc(WorldServer worldIn, BlockPos oldLocation) {
        switch (worldIn.rand.nextInt(4)) {
            default://Towards X
                return oldLocation.add(SpreadConfig.general.spreadDistance, 0, 0);
            case 1: //Towards Z
                return oldLocation.add(0, 0, SpreadConfig.general.spreadDistance);
            case 2: //Away from X
                return oldLocation.add(-SpreadConfig.general.spreadDistance, 0, 0);
            case 3: //Away from Z
                return oldLocation.add(0, 0, -SpreadConfig.general.spreadDistance);
        }
    }
}
