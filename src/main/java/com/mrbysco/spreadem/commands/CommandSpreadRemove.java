package com.mrbysco.spreadem.commands;

import com.mrbysco.spreadem.SpreadEm;
import com.mrbysco.spreadem.util.DirectoryUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class CommandSpreadRemove extends CommandBase {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.spread.remove.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1)
        {
            throw new WrongUsageException("commands.spread.remove.usage", new Object[0]);
        }
        else
        {
            UUID playerUUID = UUID.fromString(args[0]);

            DirectoryUtil.removeLocation(playerUUID);
            SpreadEm.locationMap = DirectoryUtil.buildLocationMap();
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length > 0 && args.length <= 3)
        {
            return getTabCompletionCoordinate(args, 0, targetPos);
        }
        else
        {
            return super.getTabCompletions(server, sender, args, targetPos);
        }
    }
}