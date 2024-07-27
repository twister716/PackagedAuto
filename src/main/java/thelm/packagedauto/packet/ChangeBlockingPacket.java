package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.UnpackagerMenu;

public record ChangeBlockingPacket() implements CustomPacketPayload {

	public static final Type<ChangeBlockingPacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:change_blocking"));
	public static final ChangeBlockingPacket INSTANCE = new ChangeBlockingPacket();
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangeBlockingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ChangeBlockingPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof UnpackagerMenu menu) {
					menu.blockEntity.changeBlockingMode();
				}
			});
		}
	}
}
