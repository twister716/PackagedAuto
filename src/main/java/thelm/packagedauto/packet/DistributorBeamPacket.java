package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor.TargetPoint;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.client.DistributorRenderer;

public record DistributorBeamPacket(Vec3 source, Vec3 delta) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:distributor_beam");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVec3(source);
		buf.writeVec3(delta);
	}

	public static DistributorBeamPacket read(FriendlyByteBuf buf) {
		return new DistributorBeamPacket(buf.readVec3(), buf.readVec3());
	}

	public void handle(PlayPayloadContext ctx) {
		ctx.workHandler().execute(()->{
			DistributorRenderer.INSTANCE.addBeam(source, delta);	
		});
	}

	public static void sendBeam(Vec3 source, Vec3 delta, ResourceKey<Level> dimension, double range) {
		PacketDistributor.NEAR.
		with(new TargetPoint(source.x, source.y, source.z, range, dimension)).
		send(new DistributorBeamPacket(source, delta));
	}
}
