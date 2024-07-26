package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.FluidPackageFillerMenu;

public record SetFluidAmountPacket(int amount) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(amount);
	}

	public static SetFluidAmountPacket decode(FriendlyByteBuf buf) {
		return new SetFluidAmountPacket(buf.readInt());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof FluidPackageFillerMenu menu) {
				menu.blockEntity.requiredAmount = amount;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
