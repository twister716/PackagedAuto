package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.UnpackagerMenu;

public record TrackerCountPacket(boolean decrease) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(decrease);
	}

	public static TrackerCountPacket decode(FriendlyByteBuf buf) {
		return new TrackerCountPacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerMenu menu) {
				menu.blockEntity.changeTrackerCount(decrease);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
