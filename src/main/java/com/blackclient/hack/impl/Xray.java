package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.StringSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * X-ray: hides every block except the ones you want to see. By default only
 * ores, utility blocks (spawners, crafting stations, storage, ...), lava and
 * water are visible; every block is configurable via the <b>Extra blocks</b>
 * (reveal) and <b>Hidden blocks</b> (suppress) lists, which take
 * comma/space-separated block IDs like {@code minecraft:iron_ore} (the
 * {@code minecraft:} prefix is optional). <b>Fullbright</b> (default on) is
 * wired into the same lightmap override as NightVision so the revealed blocks
 * are actually lit underground. Re-renders chunks on toggle and on any setting
 * change.
 *
 * <p>Implementation: {@code BlockModelRenderer.render} is cancelled for hidden
 * blocks, {@code Block.shouldDrawSide} is forced to {@code true} so faces
 * touching hidden blocks are not culled (otherwise ores buried in stone would
 * become invisible), {@code FluidRenderer.render} is cancelled for hidden
 * fluids, and {@code BlockEntityRenderDispatcher.render} is cancelled for
 * hidden block entities (spawners, chests, ...).
 */
public class Xray extends Hack {

    private final BoolSetting showOres = add(new BoolSetting("Show ores", true));
    private final BoolSetting showUtility = add(new BoolSetting("Show utility", true));
    private final BoolSetting showLava = add(new BoolSetting("Show lava", true));
    private final BoolSetting showWater = add(new BoolSetting("Show water", true));
    private final StringSetting extraBlocks = add(new StringSetting("Extra blocks", ""));
    private final StringSetting hiddenBlocks = add(new StringSetting("Hidden blocks", ""));
    private final BoolSetting fullbright = add(new BoolSetting("Fullbright", true));

    private static final Set<String> ORES = Set.of(
            "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
            "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
            "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
            "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
            "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
            "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore",
            "minecraft:ancient_debris",
            "minecraft:raw_iron_block", "minecraft:raw_copper_block", "minecraft:raw_gold_block");

    private static final Set<String> UTILITY = Set.of(
            "minecraft:spawner", "minecraft:trial_spawner", "minecraft:vault",
            "minecraft:crafting_table", "minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker",
            "minecraft:campfire", "minecraft:soul_campfire",
            "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel",
            "minecraft:shulker_box",
            "minecraft:enchanting_table", "minecraft:anvil", "minecraft:chipped_anvil", "minecraft:damaged_anvil",
            "minecraft:grindstone", "minecraft:smithing_table", "minecraft:stonecutter", "minecraft:loom",
            "minecraft:cartography_table", "minecraft:brewing_stand", "minecraft:cauldron",
            "minecraft:beacon", "minecraft:hopper", "minecraft:dispenser", "minecraft:dropper",
            "minecraft:lectern", "minecraft:note_block", "minecraft:jukebox", "minecraft:composter",
            "minecraft:respawn_anchor", "minecraft:lodestone", "minecraft:bell", "minecraft:conduit");

    private static final Set<String> LAVA = Set.of("minecraft:lava");
    private static final Set<String> WATER = Set.of("minecraft:water");

    private Set<String> extraCache = Set.of();
    private Set<String> hiddenCache = Set.of();
    private String fingerprint;

    public Xray() {
        super("Xray", "See through blocks — only ores, utility blocks, lava and water are visible");
    }

    public boolean shouldRender(BlockState state) {
        if (state.isAir()) {
            return true; // air is never drawn anyway
        }
        String id = Registries.BLOCK.getId(state.getBlock()).toString();
        if (hiddenSet().contains(id)) {
            return false;
        }
        if (extraSet().contains(id)) {
            return true;
        }
        if (showOres.getValue() && ORES.contains(id)) {
            return true;
        }
        if (showUtility.getValue() && UTILITY.contains(id)) {
            return true;
        }
        if (showLava.getValue() && LAVA.contains(id)) {
            return true;
        }
        return showWater.getValue() && WATER.contains(id);
    }

    public boolean fullbright() {
        return fullbright.getValue();
    }

    @Override
    public void onEnable() {
        scheduleUpdate();
    }

    @Override
    public void onDisable() {
        scheduleUpdate();
    }

    @Override
    public void onTick() {
        String fp = fingerprint();
        if (!fp.equals(fingerprint)) {
            fingerprint = fp;
            scheduleUpdate();
        }
    }

    private String fingerprint() {
        return showOres.getValue() + "|" + showUtility.getValue() + "|" + showLava.getValue() + "|" + showWater.getValue()
                + "|" + extraBlocks.getValue() + "|" + hiddenBlocks.getValue() + "|" + fullbright.getValue();
    }

    private Set<String> extraSet() {
        String raw = extraBlocks.getValue();
        if (!raw.equals(extraCacheRaw)) {
            extraCache = parse(raw);
            extraCacheRaw = raw;
        }
        return extraCache;
    }

    private Set<String> hiddenSet() {
        String raw = hiddenBlocks.getValue();
        if (!raw.equals(hiddenCacheRaw)) {
            hiddenCache = parse(raw);
            hiddenCacheRaw = raw;
        }
        return hiddenCache;
    }

    private String extraCacheRaw = "";
    private String hiddenCacheRaw = "";

    private static Set<String> parse(String raw) {
        Set<String> set = new HashSet<>();
        if (raw == null) {
            return set;
        }
        for (String part : raw.split("[,\\s]+")) {
            part = part.trim().toLowerCase(Locale.ROOT);
            if (part.isEmpty()) {
                continue;
            }
            if (!part.contains(":")) {
                part = "minecraft:" + part;
            }
            set.add(part);
        }
        return set;
    }

    private static void scheduleUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.worldRenderer != null) {
            mc.worldRenderer.scheduleTerrainUpdate();
        }
    }
}
