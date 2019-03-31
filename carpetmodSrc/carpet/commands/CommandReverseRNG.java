package carpet.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

public class CommandReverseRNG extends CommandBase {
    @Override
    public String getName() {
        return "reverserng";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/reverserng";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final long a = 0x97be9f880aa9L;
        final long b = 0xeac471130bcaL;
        long[] bvec = {0xc8fbaf16b114L, 0xc2a36898b9feL, 0x13e60619c078L, 0xe7244157cb02L, 0x3771906241cL, 0x47d6c669fa46L, 0L};
        final long[] m6 = {0xff7392795dd6L, 0x1184f9b300L, 0xff79d1d659aeL, 0xff62f08153d5L, 0xfe64fe5e8622L, 0x24beb7ecc11L, 0x30c38f7adcL};
        final long[][] mm1 = {
                {-26, -81, -16, 50, 14, 22, -76},
                {-117, 4, -42, -45, -20, -61, -32},
                {3, 79, 1, 92, 5, 26, 8},
                {18, 28, 48, 53, -92, -33, -33},
                {43, 63, -68, -10, 33, 18, -57},
                {-21, -6, 91, -19, 56, 51, -12},
                {-44, 25, -6, -1, -44, 65, 34}
        };
        bvec = matmult(bvec, mm1);

        long[] lut = new long[7];
        long[] lutPrev = new long[7];

        Scoreboard scoreboard = server.worlds[0].getScoreboard();
        ScoreObjective lutVal = scoreboard.getObjective("lutVal");
        ScoreObjective lutValPrev = scoreboard.getObjective("lutValPrev");
        if (lutVal == null || lutValPrev == null)
            throw new CommandException("lutVal or lutValPrev doesn't exist");

        for (int shot = 0; shot < 7; shot++) {
            Entity entity = EntitySelector.matchOneEntity(sender, "@e[tag=lut,score_shot="+shot+",score_shot_min="+shot+"]", Entity.class);
            if (entity == null)
                throw new CommandException("Could not find LUT entity for shot " + shot);

            String entityName = entity instanceof EntityPlayerMP ? entity.getName() : entity.getCachedUniqueIdString();
            if (!scoreboard.entityHasObjective(entityName, lutVal) || !scoreboard.entityHasObjective(entityName, lutValPrev))
                throw new CommandException("LUT entity for shot "+shot+" didn't have the correct objectives");

            lut[shot] = scoreboard.getOrCreateScore(entityName, lutVal).getScorePoints();
            lutPrev[shot] = scoreboard.getOrCreateScore(entityName, lutValPrev).getScorePoints();
        }

        long[] penultimateVector = new long[7];
        for (int j = 0; j < 7; j++) {
            long[] v = new long[7];
            for (int k = 0; k < 7; k++) {
                if ((mm1[k][j] & 0x800000000000L) != 0) { // if it's negative mod 2^48
                    v[k] = lutPrev[k] & -(1L << ( 22 ));

                } else {
                    v[k] = lut[k]+(1L << ( 39 ));
                    // whatever meh it's just an april fools video
                    //if (detectorId == 0) {
                    //    v[k]+=(1L<<48);
                    //}
                }
            }
            long val = dotBig(v, mm1, j);
            val -= bvec[j];
            val >>= 48;
            penultimateVector[j] = val;

        }

        long seed = dot(m6, penultimateVector);
        seed = (seed * 0x5deece66dL + 0xbL) & 0xffffffffffffL;
        ScoreObjective messConstants = scoreboard.getObjective("messConstants");
        if (messConstants == null)
            throw new CommandException("messConstants doesn't exist");
        scoreboard.getOrCreateScore("seed", messConstants).setScorePoints(seed);

        notifyCommandListener(sender, this, "Reversed the seed");
    }

    private static long toBigInt48(long n) {
        if ((n & 0x800000000000L) != 0)
            return n | 0xffff000000000000L;
        else
            return n & 0xffffffffffffL;
        //return BigInteger.valueOf(n);
    }

    private static long dotBig(long[] v, long[][] mat, int col) {
        long result = 0;
        for (int i = 0; i < v.length; i++) {
            result += v[i] * toBigInt48(mat[i][col]);
        }
        return result;
    }

    private static long dot(long[] a, long[] b) {
        long result = 0;
        for (int i = 0; i < a.length; i++)
            result += a[i] * b[i];
        return result;
    }

    private static long[] matmult(long[] vec, long[][] mat) {
        long[] result = new long[mat.length];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                result[i] += vec[j] * mat[j][i];
            }
        }
        return result;
    }
}
