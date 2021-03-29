package com.mrbysco.spreadem.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.mrbysco.spreadem.SpreadEm;

import net.minecraft.util.math.BlockPos;

public class DirectoryUtil {
    public static File spreadLocations;

    public static void addToLocations(BlockPos pos, UUID uuid)
    {
        String locationString = pos.toLong() + "," + uuid.toString();
        try {
            if (spreadLocations.exists())
            {
                FileWriter fileWriter = new FileWriter(spreadLocations, true);
                if(!containsLocation(locationString))
                {
                    fileWriter.write(locationString);
                    fileWriter.write("\n");
                }
                fileWriter.close();
            }
            else
            {
                FileWriter fileWriter = new FileWriter(spreadLocations);
                fileWriter.write(locationString);
                fileWriter.write("\n");
                fileWriter.close();
            }

            SpreadEm.locationMap = buildLocationMap();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeLocation(UUID uuid)
    {
        if(containsUUID(uuid)) {
            HashMap<UUID, BlockPos> currentList = buildLocationMap();

            try {
                String tempPath = spreadLocations.getAbsolutePath() + ".tmp";
                File tempFile = new File(tempPath);
                FileWriter fileWriter = new FileWriter(tempFile);

                for (HashMap.Entry<UUID, BlockPos> entry : currentList.entrySet()) {
                    fileWriter.write(entry.getValue().toLong() + "," + entry.getKey().toString());
                    fileWriter.write("\n");
                    fileWriter.close();
                }

                tempFile.renameTo(spreadLocations);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            SpreadEm.locationMap = buildLocationMap();
        }
    }

    public static boolean containsLocation(String locationString)
    {
        try {
            Scanner scanner = new Scanner(spreadLocations);

            //now read the file line by line...
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase();
                if(line.contains(locationString)) {
                    scanner.close();
                    return true;
                }
            }
            scanner.close();
        }
        catch(IOException e) {
            //handle this
        }

        return false;
    }

    public static HashMap<UUID, BlockPos> buildLocationMap() {
        HashMap<UUID, BlockPos> locationMap = new HashMap<>();
        try {
            String line;
            InputStream locationStream = FileUtils.openInputStream(spreadLocations);
            BufferedReader locReader = new BufferedReader(new InputStreamReader(locationStream));
            if(locReader != null)
            {
                while ((line = locReader.readLine()) != null)
                {
                    String[] separated = line.split(",");
                    if(separated.length == 2) {
                        BlockPos spreadLoc = BlockPos.fromLong(Long.valueOf(separated[0]));
                        UUID playerUUID = UUID.fromString(separated[1]);
                        locationMap.put(playerUUID, spreadLoc);
                    }
                }

                locReader.close();
                locReader = null;
                locationStream = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationMap;
    }

    public static boolean containsUUID(UUID uuid) {
        return SpreadEm.locationMap.containsKey(uuid);
    }

    public static boolean containsPos(BlockPos pos) {
        return SpreadEm.locationMap.containsValue(pos);
    }

    public static HashMap.Entry<UUID, BlockPos> getMatchingLocationStorage(UUID uuid) {
        for (HashMap.Entry<UUID, BlockPos> entry : SpreadEm.locationMap.entrySet()) {
            if(entry.getKey().equals(uuid)) {
                return entry;
            }
        }

        return null;
    }

    public static HashMap.Entry<UUID, BlockPos> getMatchingLocationStorage(BlockPos pos) {
        for (HashMap.Entry<UUID, BlockPos> entry : SpreadEm.locationMap.entrySet()) {
            if(entry.getValue().equals(pos)) {
                return entry;
            }
        }

        return null;
    }
}
