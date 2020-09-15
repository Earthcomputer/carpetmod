package carpet.commands;

import carpet.utils.UnloadOrder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;

public class CommandChunkSize extends CommandBase {
    @Override
    public String getName() {
        return "chunksize";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/chunksize";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Chunk chunk = sender.getEntityWorld().getChunk(sender.getPosition());
        int chunkSize = UnloadOrder.getSavedChunkSize(chunk);
        notifyCommandListener(sender, this, "Chunk size: " + chunkSize);
    }
}
