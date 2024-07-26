package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public record CycleRecipeTypePacket(boolean reverse) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(reverse);
	}

	public static CycleRecipeTypePacket decode(FriendlyByteBuf buf) {
		return new CycleRecipeTypePacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.patternItemHandler.cycleRecipeType(reverse);
				menu.setupSlots();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
