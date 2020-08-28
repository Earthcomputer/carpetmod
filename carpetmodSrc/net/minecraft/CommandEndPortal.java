package net.minecraft;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

public class CommandEndPortal extends CommandBase {
    @Override
    public String getName() {
        return "end_portal";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/end_portal <reset|chunks|hash_size|stage>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        switch (args[0]) {
            case "reset":
                EndPortalFrameItem.stage = 0;
                notifyCommandListener(sender, this, "Reset the end portal stuff");
                break;
            case "chunks":
                int numChunks = ((WorldServer) sender.getEntityWorld()).getChunkProvider().loadedChunks.size();
                notifyCommandListener(sender, this, "Number of loaded chunks: " + numChunks);
                break;
            case "hash_size":
                int hashSize = ((WorldServer) sender.getEntityWorld()).getChunkProvider().loadedChunks.n;
                notifyCommandListener(sender, this, "Hash size " + hashSize);
                break;
            case "stage":
                notifyCommandListener(sender, this, "Current stage: " + EndPortalFrameItem.stage);
                break;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reset", "chunks", "hash_size", "stage");
        }
        return Collections.emptyList();
    }
}
