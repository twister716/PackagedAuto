package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.client.DistributorRenderer;

public record DistributorBeamPacket(Vec3 source, Vec3 delta) implements CustomPacketPayload {

	public static final Type<DistributorBeamPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:distributor_beam"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DistributorBeamPacket> STREAM_CODEC = StreamCodec.composite(
			StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3), DistributorBeamPacket::source,
			StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3), DistributorBeamPacket::delta,
			DistributorBeamPacket::new);

	@Override
	public Type<DistributorBeamPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(()->{
			DistributorRenderer.INSTANCE.addBeam(source, delta);	
		});
	}

	public static void sendBeam(ServerLevel level, Vec3 source, Vec3 delta, double range) {
		PacketDistributor.sendToPlayersNear(level, null, source.x, source.y, source.z, range, new DistributorBeamPacket(source, delta));
	}
}
