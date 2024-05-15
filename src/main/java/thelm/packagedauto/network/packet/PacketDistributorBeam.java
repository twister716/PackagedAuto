package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.client.DistributorRenderer;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.network.PacketHandler;

public class PacketDistributorBeam implements ISelfHandleMessage<IMessage> {

	private Vec3d source;
	private Vec3d delta;

	public PacketDistributorBeam() {}

	public PacketDistributorBeam(Vec3d source, Vec3d delta) {
		this.source = source;
		this.delta = delta;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeDouble(delta.x);
		buf.writeDouble(delta.y);
		buf.writeDouble(delta.z);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		source = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		delta = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(()->{
			DistributorRenderer.INSTANCE.addBeam(source, delta);	
		});
		return null;
	}

	public static void sendBeam(Vec3d source, Vec3d delta, int dimension, double range) {
		PacketHandler.INSTANCE.sendToAllAround(new PacketDistributorBeam(source, delta), new TargetPoint(dimension, source.x, source.y, source.z, range));
	}
}
