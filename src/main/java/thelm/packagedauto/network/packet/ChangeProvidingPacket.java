package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.PackagingProviderMenu;

public record ChangeProvidingPacket(PackagingProviderBlockEntity.Type type) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeEnum(type);
	}

	public static ChangeProvidingPacket decode(FriendlyByteBuf buf) {
		return new ChangeProvidingPacket(buf.readEnum(PackagingProviderBlockEntity.Type.class));
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof PackagingProviderMenu menu) {
				menu.blockEntity.changeProvideType(type);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
