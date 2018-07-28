package carpet.helpers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class ChunkHelper
{

    public static Chunk getLoadedChunkNoDequeue(World world, int chunkX, int chunkZ)
    {
        IChunkProvider chunkProvider = world.getChunkProvider();
        if (chunkProvider instanceof ChunkProviderServer)
            return ((ChunkProviderServer) chunkProvider).id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
        else
            return chunkProvider.getLoadedChunk(chunkX, chunkZ);
    }
    
    public static IBlockState getBlockStateNoDequeue(World world, BlockPos pos)
    {
        Chunk chunk = getLoadedChunkNoDequeue(world, pos.getX() / 16, pos.getZ() / 16);
        if (chunk == null)
            return Blocks.AIR.getDefaultState();
        else
            return chunk.getBlockState(pos);
    }
    
    public static TileEntity getTileEntityNoDequeue(World world, BlockPos pos)
    {
        Chunk chunk = getLoadedChunkNoDequeue(world, pos.getX() / 16, pos.getZ() / 16);
        if (chunk == null)
            return null;
        else
            return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }
    
}
