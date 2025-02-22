package thelm.packagedauto.network;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.network.packet.ChangePackagingPacket;
import thelm.packagedauto.network.packet.ChangeProvidingPacket;
import thelm.packagedauto.network.packet.CycleRecipeTypePacket;
import thelm.packagedauto.network.packet.DistributorBeamPacket;
import thelm.packagedauto.network.packet.LoadRecipeListPacket;
import thelm.packagedauto.network.packet.SaveRecipeListPacket;
import thelm.packagedauto.network.packet.SetFluidAmountPacket;
import thelm.packagedauto.network.packet.SetItemStackPacket;
import thelm.packagedauto.network.packet.SetPatternIndexPacket;
import thelm.packagedauto.network.packet.SetRecipePacket;
import thelm.packagedauto.network.packet.SyncEnergyPacket;
import thelm.packagedauto.network.packet.TrackerCountPacket;

public class PacketHandler {

	public static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("packagedauto", PROTOCOL_VERSION),
			()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static void registerPackets() {
		int id = 0;
		INSTANCE.registerMessage(id++, SyncEnergyPacket.class,
				SyncEnergyPacket::encode, SyncEnergyPacket::decode,
				SyncEnergyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		INSTANCE.registerMessage(id++, SetItemStackPacket.class,
				SetItemStackPacket::encode, SetItemStackPacket::decode,
				SetItemStackPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, SetPatternIndexPacket.class,
				SetPatternIndexPacket::encode, SetPatternIndexPacket::decode,
				SetPatternIndexPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, CycleRecipeTypePacket.class,
				CycleRecipeTypePacket::encode, CycleRecipeTypePacket::decode,
				CycleRecipeTypePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, SaveRecipeListPacket.class,
				SaveRecipeListPacket::encode, SaveRecipeListPacket::decode,
				SaveRecipeListPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, SetRecipePacket.class,
				SetRecipePacket::encode, SetRecipePacket::decode,
				SetRecipePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, LoadRecipeListPacket.class,
				LoadRecipeListPacket::encode, LoadRecipeListPacket::decode,
				LoadRecipeListPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, ChangeBlockingPacket.class,
				ChangeBlockingPacket::encode, ChangeBlockingPacket::decode,
				ChangeBlockingPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, SetFluidAmountPacket.class,
				SetFluidAmountPacket::encode, SetFluidAmountPacket::decode,
				SetFluidAmountPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, ChangePackagingPacket.class,
				ChangePackagingPacket::encode, ChangePackagingPacket::decode,
				ChangePackagingPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, TrackerCountPacket.class,
				TrackerCountPacket::encode, TrackerCountPacket::decode,
				TrackerCountPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		INSTANCE.registerMessage(id++, DistributorBeamPacket.class,
				DistributorBeamPacket::encode, DistributorBeamPacket::decode,
				DistributorBeamPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		INSTANCE.registerMessage(id++, ChangeProvidingPacket.class,
				ChangeProvidingPacket::encode, ChangeProvidingPacket::decode,
				ChangeProvidingPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}
}
