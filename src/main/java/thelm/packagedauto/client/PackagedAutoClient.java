package thelm.packagedauto.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.client.event.ClientEventHandler;

@Mod(value = PackagedAuto.MOD_ID, dist = Dist.CLIENT)
public class PackagedAutoClient {

	public PackagedAutoClient(IEventBus modEventBus) {
		ClientEventHandler.getInstance().onConstruct(modEventBus);
	}
}
