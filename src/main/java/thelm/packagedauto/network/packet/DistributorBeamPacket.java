package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.client.DistributorRenderer;
import thelm.packagedauto.network.PacketHandler;

public record DistributorBeamPacket(Vec3 source, Vec3 delta) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeDouble(delta.x);
		buf.writeDouble(delta.y);
		buf.writeDouble(delta.z);
	}

	public static DistributorBeamPacket decode(FriendlyByteBuf buf) {
		Vec3 source = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		Vec3 delta = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new DistributorBeamPacket(source, delta);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			DistributorRenderer.INSTANCE.addBeam(source, delta);	
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendBeam(Vec3 source, Vec3 delta, ResourceKey<Level> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(source.x, source.y, source.z, range, dimension)), new DistributorBeamPacket(source, delta));
	}
}
