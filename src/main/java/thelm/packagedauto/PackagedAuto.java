package thelm.packagedauto;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import thelm.packagedauto.event.CommonEventHandler;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";

	public PackagedAuto(IEventBus modEventBus) {
		CommonEventHandler.getInstance().onConstruct(modEventBus);
	}
}
