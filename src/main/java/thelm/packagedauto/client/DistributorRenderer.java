package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.primitives.Doubles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.tile.TileDistributor;

// Based on Botania, Scannables, and AE2
public class DistributorRenderer {

	public static final DistributorRenderer INSTANCE = new DistributorRenderer();
	public static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat(DefaultVertexFormats.POSITION_COLOR).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
	public static final Vec3d BLOCK_SIZE = new Vec3d(1, 1, 1);
	public static final int BEAM_LIFETIME = 6;

	private DistributorRenderer() {}

	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		for(EnumHand hand : EnumHand.values()) {
			ItemStack stack = player.getHeldItem(hand);
			if(stack.getItem() instanceof IDistributorMarkerItem) {
				renderMarker(((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack));
			}
		}

		renderBeams(event.getPartialTicks());
	}

	public void addBeam(Vec3d source, Vec3d delta) {
		beams.add(new BeamInfo(source, delta));
	}

	public void renderMarker(DirectionalGlobalPos globalPos) {
		if(globalPos == null) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if(globalPos.dimension() != mc.world.provider.getDimension()) {
			return;
		}

		int range = 2*TileDistributor.range+2;
		BlockPos blockPos = globalPos.blockPos();
		double viewerPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
		double viewerPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
		double viewerPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;
		double distX = viewerPosX-blockPos.getX()-0.5;
		double distY = viewerPosY-blockPos.getY()-0.5;
		double distZ = viewerPosZ-blockPos.getZ()-0.5;
		if(Doubles.max(distX, distY, distZ) > range) {
			return;
		}

		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GL11.glLineWidth(4);
		GlStateManager.translate(blockPos.getX()-viewerPosX, blockPos.getY()-viewerPosY, blockPos.getZ()-viewerPosZ);

		EnumFacing direction = globalPos.direction();
		drawMarker(BLOCK_SIZE, direction, 0F, 1F, 1F, 0.5F);
		drawMarker(BLOCK_SIZE, null, 0F, 1F, 1F, 1F);

		GlStateManager.enableDepth();
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	public void renderBeams(float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		double viewerPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
		double viewerPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
		double viewerPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GL11.glLineWidth(3);

		for(BeamInfo beam : beams) {
			Vec3d source = beam.source;

			GlStateManager.pushMatrix();
			GlStateManager.translate(source.x-viewerPosX, source.y-viewerPosY, source.z-viewerPosZ);

			drawBeam(beam.delta, 0F, 1F, 1F, beam.getAlpha(renderTick));

			GlStateManager.popMatrix();
		}

		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();
	}

	public void drawMarker(Vec3d delta, EnumFacing direction, float r, float g, float b, float a) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.begin(direction == null ? GL11.GL_LINES : GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		if(direction == null || direction == EnumFacing.NORTH) {
			// Face North, Edge Bottom
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face North, Edge Top
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.SOUTH) {
			// Face South, Edge Bottom
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
			// Face South, Edge Top
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.WEST) {
			// Face West, Edge Bottom
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
			// Face West, Edge Top
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == EnumFacing.EAST) {
			// Face East, Edge Bottom
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face East, Edge Top
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == EnumFacing.DOWN) {
			// Face Down
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
		}
		if(direction == EnumFacing.UP) {
			// Face Up
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
			buffer.pos(0, y, 0).color(r, g, b, a).endVertex();
			// Face North, Edge East
			buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
			buffer.pos(x, 0, 0).color(r, g, b, a).endVertex();
			// Face South, Edge East
			buffer.pos(x, 0, z).color(r, g, b, a).endVertex();
			buffer.pos(x, y, z).color(r, g, b, a).endVertex();
			// Face South, Edge West
			buffer.pos(0, y, z).color(r, g, b, a).endVertex();
			buffer.pos(0, 0, z).color(r, g, b, a).endVertex();
		}
		tessellator.draw();
	}

	public void drawBeam(Vec3d delta, float r, float g, float b, float a) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
		buffer.pos(x, y, z).color(r, g, b, a).endVertex();
		tessellator.draw();
	}

	public static class BeamInfo {
		private Vec3d source;
		private Vec3d delta;
		private int startTick;

		public BeamInfo(Vec3d source, Vec3d delta) {
			this.source = source;
			this.delta = delta;
			startTick = RenderTimer.INSTANCE.getTicks();
		}

		public boolean shouldRemove(int currentTick) {
			if(currentTick < startTick) {
				currentTick += 0x1FFFFF;
			}
			return currentTick-startTick >= BEAM_LIFETIME;
		}

		public float getAlpha(float renderTick) {
			float diff = renderTick-startTick;
			if(diff < 0) {
				diff += 0x1FFFFF;
			}
			float factor = diff/BEAM_LIFETIME;
			return 1-factor*factor;
		}
	}
}
