package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public record SaveRecipeListPacket(boolean single) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(single);
	}

	public static SaveRecipeListPacket decode(FriendlyByteBuf buf) {
		return new SaveRecipeListPacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.blockEntity.saveRecipeList(single);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
