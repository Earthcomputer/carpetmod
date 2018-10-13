package carpet.logging.logHelpers;

import carpet.CarpetServer;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class EndstoneReporter
{

    public static void onGatewayTeleport(TileEntityEndGateway gateway, Entity entity, BlockPos targetPos, boolean generated)
    {
        if (LoggerRegistry.__endGateway)
        {
            LoggerRegistry.getLogger("endGateway").log((option) -> {
                if ("generated".equals(option) && !generated)
                    return null;
                return new ITextComponent[] {
                        Messenger.m(null,
                                "g  tick ", "w " + gateway.getWorld().getTotalWorldTime(),
                                "g  gateway at ", String.format("w (%d, %d, %d)", gateway.getPos().getX(), gateway.getPos().getY(), gateway.getPos().getZ()),
                                "g  teleports to ", String.format("w (%d, %d, %d)", targetPos.getX(), targetPos.getY(), targetPos.getZ()),
                                "g  item pos ", String.format("w (%f, %f, %f)", entity.posX, entity.posY, entity.posZ))
                };
            },
            "GATEWAY_X", gateway.getPos().getX(),
            "GATEWAY_Y", gateway.getPos().getY(),
            "GATEWAY_Z", gateway.getPos().getZ(),
            "ENTITY", entity.getCachedUniqueIdString(),
            "TARGET_X", targetPos.getX(),
            "TARGET_Y", targetPos.getY(),
            "TARGET_Z", targetPos.getZ(),
            "GENERATED", generated);
        }
    }
    
    public static void onDatalessTEQueried(TileEntity te)
    {
        if (LoggerRegistry.__datalessTEUpdated)
        {
            LoggerRegistry.getLogger("datalessTEUpdated").log(() -> {
               return new ITextComponent[] {
                       Messenger.m(null,
                               "g  dataless TE updated ",
                               "g  tick ", "w " + CarpetServer.minecraft_server.worlds[0].getTotalWorldTime(),
                               "g  pos ", String.format("w (%d, %d, %d)", te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()))
               };
            },
            "X", te.getPos().getX(),
            "Y", te.getPos().getY(),
            "Z", te.getPos().getZ());
        }
    }
    
}
