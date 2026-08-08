package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.NumberSetting;
import com.blackclient.mixin.ClientPlayerInteractionManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Takes control of the player: locks the camera to the direction the player
 * was facing when enabled, forces the player to walk forward, mines a
 * 1-wide x N-tall tunnel ahead, and bridges 1-block drops when a block item
 * is available. Movement and mining are stopped before any lava is mined
 * into or walked into.
 */
public class Tunneler extends Hack {

    private final NumberSetting height = add(new NumberSetting("Height", 2, 1, 4, 1));
    private final BoolSetting placeBlocks = add(new BoolSetting("Place blocks", true));
    private final BoolSetting stopAtLava = add(new BoolSetting("Stop at lava", true));

    private float frozenYaw;
    private boolean yawFrozen;

    public Tunneler() {
        super("Tunneler", "Mines a tunnel ahead and walks forward; stops before lava");
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            frozenYaw = mc.player.getYaw();
            yawFrozen = true;
        }
    }

    @Override
    public void onDisable() {
        yawFrozen = false;
    }

    /**
     * Called every tick from the KeyboardInput mixin, once the hack has taken
     * over the movement input.
     *
     * @return true if the player may walk forward, false if blocked by lava
     */
    public boolean onControlTick(ClientPlayerEntity player) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.interactionManager == null || !player.isAlive() || player.hasVehicle()) {
            return false;
        }

        if (!yawFrozen) {
            frozenYaw = player.getYaw();
            yawFrozen = true;
        }

        // Lock the camera to the tunnel direction.
        player.setYaw(MathHelper.wrapDegrees(frozenYaw));
        player.setHeadYaw(MathHelper.wrapDegrees(frozenYaw));
        player.setPitch(0.0F);

        float yawRad = (float) Math.toRadians(frozenYaw);
        int dx = (int) Math.round(-MathHelper.sin(yawRad));
        int dz = (int) Math.round(MathHelper.cos(yawRad));
        BlockPos front = player.getBlockPos().add(dx, 0, dz);

        boolean blockedByLava = stopAtLava.getValue() && isLavaNearby(mc.world, front);
        if (!blockedByLava) {
            mineTunnel(mc, player, front);
            bridgeGap(mc, player, front);
        }
        return !blockedByLava;
    }

    private void mineTunnel(MinecraftClient mc, ClientPlayerEntity player, BlockPos front) {
        ClientPlayerInteractionManager interaction = mc.interactionManager;

        // Mine one block per tick: lowest non-air block of the front column first.
        for (int i = 0; i < height.getValueInt(); i++) {
            BlockPos target = front.up(i);
            BlockState state = mc.world.getBlockState(target);
            if (state.isAir() || state.getFluidState().isIn(FluidTags.LAVA)) {
                continue;
            }
            if (state.calcBlockBreakingDelta(player, mc.world, target) <= 0.0F) {
                continue; // unbreakable (bedrock, ...)
            }
            // Direction.fromVector(int,int,int) returns null for diagonal
            // vectors (it only handles axis-aligned ones), which would crash
            // the netty encoder when the packet is sent. Compute the face
            // toward the player instead, which is always valid.
            Direction face = faceTowardPlayer(player, target);
            ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor) interaction;
            if (accessor.blackclient$isCurrentlyBreaking(target)) {
                interaction.updateBlockBreakingProgress(target, face);
            } else {
                interaction.attackBlock(target, face);
            }
            return;
        }
    }

    /** The face of {@code pos} that points back toward the player's eyes. Never returns null. */
    private static Direction faceTowardPlayer(ClientPlayerEntity player, BlockPos pos) {
        Vec3d from = Vec3d.ofCenter(pos);
        Vec3d to = player.getEyePos();
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        if (ay >= ax && ay >= az) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
        if (az >= ax) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return dx > 0 ? Direction.EAST : Direction.WEST;
    }

    private void bridgeGap(MinecraftClient mc, ClientPlayerEntity player, BlockPos front) {
        if (!placeBlocks.getValue() || player.getAbilities().flying) {
            return;
        }
        BlockPos below = front.down(1);
        BlockState belowState = mc.world.getBlockState(below);
        if (!belowState.isAir() || belowState.getFluidState().isIn(FluidTags.LAVA)) {
            return; // floor already exists (or lava, handled by the lava check)
        }
        if (mc.world.getBlockState(below.down(1)).isAir()) {
            return; // drop deeper than one block: nothing to place against
        }

        // Pick a block item from the hotbar (keep the current slot if it already holds one).
        var inventory = player.getInventory();
        int slot = inventory.getStack(inventory.selectedSlot).getItem() instanceof BlockItem
                ? inventory.selectedSlot
                : -1;
        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                if (inventory.getStack(i).getItem() instanceof BlockItem) {
                    slot = i;
                    break;
                }
            }
        }
        if (slot == -1) {
            return; // no block items in the hotbar
        }

        inventory.selectedSlot = slot;
        BlockPos support = below.down(1);
        BlockHitResult hit = new BlockHitResult(
                new Vec3d(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5),
                Direction.UP, support, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        player.swingHand(Hand.MAIN_HAND);
    }

    /** True if lava is in, next to, or directly under the tunnel column ahead. */
    private boolean isLavaNearby(ClientWorld world, BlockPos front) {
        for (int i = 0; i < height.getValueInt(); i++) {
            BlockPos target = front.up(i);
            if (isLava(world, target)) {
                return true;
            }
            for (Direction direction : Direction.values()) {
                if (isLava(world, target.offset(direction))) {
                    return true;
                }
            }
        }
        // An open pit ahead leading down into lava stops the tunnel (bridging
        // only covers a 1-block drop). Lava under a solid floor is not a
        // hazard, so only stop when every block above the lava is air.
        for (int y = 1; y <= 4; y++) {
            BlockPos below = front.down(y);
            if (!isLava(world, below)) {
                continue;
            }
            boolean openPit = true;
            for (int z = 1; z < y; z++) {
                if (!world.getBlockState(front.down(z)).isAir()) {
                    openPit = false;
                    break;
                }
            }
            if (openPit) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLava(ClientWorld world, BlockPos pos) {
        return world.getBlockState(pos).getFluidState().isIn(FluidTags.LAVA);
    }
}
