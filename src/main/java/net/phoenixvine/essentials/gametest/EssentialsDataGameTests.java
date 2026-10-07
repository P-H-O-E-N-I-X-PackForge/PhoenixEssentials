package net.phoenixvine.essentials.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.data.NamedLocation;

@GameTestHolder(PhoenixEssentials.MOD_ID)
@PrefixGameTestTemplate(false)
public class EssentialsDataGameTests {

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void namedLocationSerializeDeserializeRoundTripsAllFields(GameTestHelper helper) {
        ResourceKey<Level> dim = helper.getLevel().dimension();
        NamedLocation original = new NamedLocation(dim, 12.5, 64.0, -300.25, 90.0f, -45.0f);

        CompoundTag tag = original.serialize();
        NamedLocation restored = NamedLocation.deserialize(tag);

        helper.assertTrue(original.dimension.equals(restored.dimension), "dimension should round-trip");
        helper.assertTrue(original.x == restored.x && original.y == restored.y && original.z == restored.z,
                "position should round-trip");
        helper.assertTrue(original.yaw == restored.yaw && original.pitch == restored.pitch,
                "yaw/pitch should round-trip");

        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void playerEssentialsDataSerializeDeserializeRoundTripsHomesAndBack(GameTestHelper helper) {
        ResourceKey<Level> dim = helper.getLevel().dimension();
        NamedLocation base = new NamedLocation(dim, 1, 64, 0, 0, 0);
        NamedLocation mine = new NamedLocation(dim, 2, 64, 0, 0, 0);
        NamedLocation backLoc = new NamedLocation(dim, 3, 64, 0, 0, 0);

        PlayerEssentialsData original = new PlayerEssentialsData();
        original.setHome("base", base);
        original.setHome("mine", mine);
        original.setBack(backLoc);

        CompoundTag tag = original.serializeNBT();
        PlayerEssentialsData restored = new PlayerEssentialsData();
        restored.deserializeNBT(tag);

        helper.assertTrue(restored.getHomes().size() == 2, "both homes should survive the round trip");
        helper.assertTrue(restored.getHome("base").x == base.x, "'base' home should keep its position");
        helper.assertTrue(restored.getHome("mine").x == mine.x, "'mine' home should keep its position");
        helper.assertTrue(restored.getBack() != null && restored.getBack().x == backLoc.x,
                "back location should survive the round trip");

        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void deserializeSkipsACorruptHomeEntryWithoutLosingTheOthers(GameTestHelper helper) {
        ResourceKey<Level> dim = helper.getLevel().dimension();
        NamedLocation good = new NamedLocation(dim, 1, 64, 0, 0, 0);

        PlayerEssentialsData original = new PlayerEssentialsData();
        original.setHome("good", good);

        CompoundTag tag = original.serializeNBT();
        var homesList = tag.getList("Homes", Tag.TAG_COMPOUND);
        CompoundTag corrupt = new CompoundTag();
        corrupt.putString("name", "broken");
        CompoundTag corruptLoc = new CompoundTag();
        corruptLoc.putString("dim", "not a valid resource location!!"); 
        corrupt.put("loc", corruptLoc);
        homesList.add(corrupt);
        tag.put("Homes", homesList);

        PlayerEssentialsData restored = new PlayerEssentialsData();
        restored.deserializeNBT(tag);

        helper.assertTrue(restored.getHomes().size() == 1,
                "the corrupt entry should be skipped, not crash or drop the good one");
        helper.assertTrue(restored.getHome("good").x == good.x, "the good home should survive intact");

        helper.succeed();
    }
}
