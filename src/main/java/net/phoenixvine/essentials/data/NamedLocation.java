package net.phoenixvine.essentials.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NamedLocation {

    public ResourceKey<Level> dimension;
    public double x, y, z;
    public float yaw, pitch;

    public NamedLocation() {}

    public NamedLocation(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static NamedLocation of(ServerPlayer player) {
        return new NamedLocation(player.level().dimension(), player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", dimension.location().toString());
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        return tag;
    }

    public static NamedLocation deserialize(CompoundTag tag) {
        NamedLocation loc = new NamedLocation();
        loc.dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(tag.getString("dim")));
        loc.x = tag.getDouble("x");
        loc.y = tag.getDouble("y");
        loc.z = tag.getDouble("z");
        loc.yaw = tag.getFloat("yaw");
        loc.pitch = tag.getFloat("pitch");
        return loc;
    }
}
