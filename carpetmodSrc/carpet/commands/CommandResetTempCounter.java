package carpet.commands;

import carpet.logging.logHelpers.DamageReporter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandResetTempCounter extends CommandBase {

    @Override
    public String getName() {
        return "resettempcounter";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/resettempcounter";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        DamageReporter.itemsKilledCount = 0;
    }

}
