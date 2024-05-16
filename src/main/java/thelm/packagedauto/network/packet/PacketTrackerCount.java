package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketTrackerCount implements ISelfHandleMessage<IMessage> {

	private boolean decrease;

	public PacketTrackerCount() {}

	public PacketTrackerCount(boolean decrease) {
		this.decrease = decrease;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(decrease);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		decrease = buf.readBoolean();
	}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerUnpackager) {
				ContainerUnpackager container = (ContainerUnpackager)player.openContainer;
				container.tile.changeTrackerCount(decrease);
			}
		});
		return null;
	}
}
