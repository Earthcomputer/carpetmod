package carpet.commands;

import carpet.helpers.MessHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class CommandMess extends CommandBase {

    @Override
    public String getName() {
        return "mess";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/mess (lockdown)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        switch (args[0]) {
            case "lockdown":
                toggleLockdown(sender);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private void toggleLockdown(ICommandSender sender) {
        if (MessHelper.lockdown) {
            MessHelper.lockdown = false;
            notifyCommandListener(sender, this, "Server is no longer under lockdown");
        } else {
            MessHelper.lockdown = true;
            notifyCommandListener(sender, this, "Server is under lockdown");
        }
    }
}
