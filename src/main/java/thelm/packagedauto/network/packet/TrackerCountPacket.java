package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.UnpackagerMenu;

public class TrackerCountPacket {

	private boolean decrease;

	public TrackerCountPacket(boolean decrease) {
		this.decrease = decrease;
	}

	public static void encode(TrackerCountPacket pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.decrease);
	}

	public static TrackerCountPacket decode(FriendlyByteBuf buf) {
		return new TrackerCountPacket(buf.readBoolean());
	}

	public static void handle(TrackerCountPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerMenu menu) {
				menu.blockEntity.changeTrackerCount(pkt.decrease);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
