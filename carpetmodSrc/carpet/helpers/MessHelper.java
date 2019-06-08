package carpet.helpers;

import carpet.commands.CommandReverseRNG;
import carpet.patches.BlockWool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class MessHelper {

    private static SecureRandom rand = new SecureRandom();

    public static boolean lockdown = false;

    public static boolean nextTesting = false;
    private static boolean testing = false;
    private static int testStartTime = 0;
    private static long startingSeed;
    private static int passedSeeds = 0;
    public static List<Long> failedSeeds = new ArrayList<>();
    private static List<Boolean> last32Passed = new ArrayList<>();
    private static List<Long> breakingCandidateSeeds;

    public static final int MESS_DETECTOR_TIME = 3870;

    public static void startTest(MinecraftServer server) {
        testStartTime = server.getTickCounter() + 1;
        nextTesting = true;
    }

    public static void tick(MinecraftServer server) {
        if ((server.getTickCounter() - testStartTime) % MESS_DETECTOR_TIME == 0) {
            if (testing) {
                verifySeed(server);
                if (!nextTesting) {
                    printVerificationOutput(server);
                    testing = false;
                } else {
                    triggerMessDetector(server);
                }
            } else {
                if (nextTesting) {
                    passedSeeds = 0;
                    failedSeeds.clear();
                    last32Passed.clear();
                    breakingCandidateSeeds = null;
                    triggerMessDetector(server);
                    testing = true;
                }
            }
        }
    }

    private static void verifySeed(MinecraftServer server) {
        long seed = 0;
        for (int i = 0; i < 12; i++) {
            seed = 16 * seed + ((BlockWool) Blocks.WOOL).getWoolPower(server, EnumDyeColor.byMetadata(11 - i));
        }
        if (seed == CommandReverseRNG.getMathRandomSeed()) {
            passedSeeds++;
            last32Passed.add(true);
        } else {
            failedSeeds.add(startingSeed);
            last32Passed.add(false);
        }
        if (last32Passed.size() > 32) {
            last32Passed.remove(0);

            if (breakingCandidateSeeds == null) {
                int amtPassed = 0;
                for (boolean passed : last32Passed)
                    if (passed)
                        amtPassed++;
                if (amtPassed < 16) {
                    breakingCandidateSeeds = new ArrayList<>();
                    for (int i = Math.max(0, failedSeeds.size() - (32 - amtPassed)); i < failedSeeds.size(); i++)
                        breakingCandidateSeeds.add(failedSeeds.get(i));
                }
            }
        }
    }

    private static void triggerMessDetector(MinecraftServer server) {
        startingSeed = rand.nextLong() & 0xffffffffffffL;
        CommandReverseRNG.getMathRandom().setSeed(startingSeed ^ 0x5deece66dL);

        World world = server.worlds[0];
        BlockPos pos = new BlockPos(0, 84, 2);
        IBlockState state = world.getBlockState(pos);
        //noinspection ConstantConditions
        state.getBlock().onBlockActivated(world, pos, state, null, EnumHand.MAIN_HAND, EnumFacing.DOWN, 0, 0, 0);
    }

    private static void printVerificationOutput(MinecraftServer server) {
        PlayerList pl = server.getPlayerList();
        pl.sendMessage(new TextComponentString((passedSeeds + failedSeeds.size()) + " seeds tested"));
        pl.sendMessage(new TextComponentString("" + TextFormatting.GREEN + passedSeeds + " passes" + TextFormatting.RESET + ", " + TextFormatting.RED + failedSeeds.size() + " failures"));
        if (breakingCandidateSeeds != null) {
            pl.sendMessage(new TextComponentString(TextFormatting.RED + "The mess detector may have broken." + TextFormatting.RESET + " Here are some candidate seeds which may have broken it:"));
            for (long candidate : breakingCandidateSeeds)
                pl.sendMessage(new TextComponentString("- " + Long.toHexString(candidate)));
        }
        if (!failedSeeds.isEmpty())
            pl.sendMessage(new TextComponentString("Use " + TextFormatting.ITALIC + "/mess seefailed" + TextFormatting.RESET + " to see all failed seeds"));
    }

}
