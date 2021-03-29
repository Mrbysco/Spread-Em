package com.mrbysco.spreadem.commands;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandSpreadTree extends CommandTreeBase
{
    public CommandSpreadTree()
    {
        super.addSubcommand(new CommandSpreadSet());
        super.addSubcommand(new CommandSpreadRemove());
    }

    @Override
    public String getName()
    {
        return "spreadem";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "commands.spreadem.usage";
    }
}