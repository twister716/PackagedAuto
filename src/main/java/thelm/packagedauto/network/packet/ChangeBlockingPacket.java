package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.UnpackagerMenu;

public record ChangeBlockingPacket() {

	public static final ChangeBlockingPacket INSTANCE = new ChangeBlockingPacket();

	public void encode(FriendlyByteBuf buf) {}

	public static ChangeBlockingPacket decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerMenu menu) {
				menu.blockEntity.changeBlockingMode();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
