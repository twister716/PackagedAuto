package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class LoadRecipeListPacket {

	private boolean single;

	public LoadRecipeListPacket(boolean single) {
		this.single = single;
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(single);
	}

	public static LoadRecipeListPacket decode(PacketBuffer buf) {
		return new LoadRecipeListPacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				EncoderContainer container = (EncoderContainer)player.containerMenu;
				container.tile.loadRecipeList(single);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
