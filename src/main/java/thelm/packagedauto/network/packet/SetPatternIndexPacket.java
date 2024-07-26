package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public record SetPatternIndexPacket(int index) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(index);
	}

	public static SetPatternIndexPacket decode(FriendlyByteBuf buf) {
		return new SetPatternIndexPacket(buf.readByte());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.setPatternIndex(index);
				menu.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
