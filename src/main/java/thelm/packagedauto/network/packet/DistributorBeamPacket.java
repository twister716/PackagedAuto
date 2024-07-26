package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.client.DistributorRenderer;
import thelm.packagedauto.network.PacketHandler;

public class DistributorBeamPacket {

	private Vector3d source;
	private Vector3d delta;

	public DistributorBeamPacket(Vector3d source, Vector3d delta) {
		this.source = source;
		this.delta = delta;
	}

	public void encode(PacketBuffer buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeDouble(delta.x);
		buf.writeDouble(delta.y);
		buf.writeDouble(delta.z);
	}

	public static DistributorBeamPacket decode(PacketBuffer buf) {
		Vector3d source = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		Vector3d delta = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new DistributorBeamPacket(source, delta);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			DistributorRenderer.INSTANCE.addBeam(source, delta);	
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendBeam(Vector3d source, Vector3d delta, RegistryKey<World> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(source.x, source.y, source.z, range, dimension)), new DistributorBeamPacket(source, delta));
	}
}
