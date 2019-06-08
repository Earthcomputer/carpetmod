package carpet.commands;

import carpet.helpers.MessHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandMess extends CommandBase {

    @Override
    public String getName() {
        return "mess";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/mess (lockdown|test|seefailed)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        switch (args[0]) {
            case "lockdown":
                toggleLockdown(sender);
                break;
            case "test":
                toggleVerify(server, sender);
                break;
            case "seefailed":
                seeFailed(sender);
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

    private void toggleVerify(MinecraftServer server, ICommandSender sender) {
        if (MessHelper.nextTesting) {
            MessHelper.nextTesting = false;
            notifyCommandListener(sender, this, "Stopping mess detector test...");
        } else {
            MessHelper.startTest(server);
            notifyCommandListener(sender, this, "Starting mess detector test...");
        }
    }

    private void seeFailed(ICommandSender sender) throws CommandException {
        if (MessHelper.failedSeeds.isEmpty())
            throw new CommandException("No failed seeds");
        for (long failedSeed : MessHelper.failedSeeds) {
            sender.sendMessage(new TextComponentString("- " + Long.toHexString(failedSeed)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "lockdown", "test", "seefailed");
        else
            return Collections.emptyList();
    }
}
