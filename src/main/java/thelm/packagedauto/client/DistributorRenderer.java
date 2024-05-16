package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.tile.DistributorTile;

// Based on Botania, Scannables, and AE2
public class DistributorRenderer {

	public static final DistributorRenderer INSTANCE = new DistributorRenderer();
	public static final Vector3d BLOCK_SIZE = new Vector3d(1, 1, 1);
	public static final int BEAM_LIFETIME = 6;

	private DistributorRenderer() {}

	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		MinecraftForge.EVENT_BUS.addListener(this::onRenderWorldLast);
	}

	public void onRenderWorldLast(RenderWorldLastEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		for(Hand hand : Hand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if(stack.getItem() instanceof IDistributorMarkerItem) {
				renderMarker(event.getMatrixStack(), ((IDistributorMarkerItem)stack.getItem()).getDirectionalGlobalPos(stack));
			}
		}

		renderBeams(event.getMatrixStack(), event.getPartialTicks());
	}

	public void addBeam(Vector3d source, Vector3d delta) {
		beams.add(new BeamInfo(source, delta));
	}

	public void renderMarker(MatrixStack matrixStack, DirectionalGlobalPos globalPos) {
		if(globalPos == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if(!globalPos.dimension().equals(mc.level.dimension())) {
			return;
		}

		int range = 2*DistributorTile.range+2;
		BlockPos blockPos = globalPos.blockPos();
		Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		Vector3d distVec = cameraPos.subtract(Vector3d.atCenterOf(blockPos));
		if(Doubles.max(Math.abs(distVec.x), Math.abs(distVec.y), Math.abs(distVec.z)) > range) {
			return;
		}

		IRenderTypeBuffer.Impl buffers = RenderTypeHelper.BUFFERS;
		IVertexBuilder quadBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_QUAD);
		IVertexBuilder lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

		matrixStack.pushPose();
		matrixStack.translate(blockPos.getX()-cameraPos.x, blockPos.getY()-cameraPos.y, blockPos.getZ()-cameraPos.z);

		Direction direction = globalPos.direction();
		addMarkerVertices(matrixStack, quadBuffer, BLOCK_SIZE, direction, 0F, 1F, 1F, 0.5F);
		addMarkerVertices(matrixStack, lineBuffer, BLOCK_SIZE, null, 0F, 1F, 1F, 1F);

		matrixStack.popPose();

		RenderSystem.disableDepthTest();
		buffers.endBatch();
		RenderSystem.enableDepthTest();
	}

	public void renderBeams(MatrixStack matrixStack, float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		Minecraft mc = Minecraft.getInstance();
		Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		IRenderTypeBuffer.Impl buffers = RenderTypeHelper.BUFFERS;
		IVertexBuilder lineBuffer = buffers.getBuffer(RenderTypeHelper.BEAM_LINE_3);

		matrixStack.pushPose();
		for(BeamInfo beam : beams) {
			Vector3d source = beam.source;

			matrixStack.pushPose();
			matrixStack.translate(source.x-cameraPos.x, source.y-cameraPos.y, source.z-cameraPos.z);

			addBeamVertices(matrixStack, lineBuffer, beam.delta, 0F, 1F, 1F, beam.getAlpha(renderTick));

			matrixStack.popPose();
		}
		matrixStack.popPose();

		buffers.endBatch();
	}

	public void addMarkerVertices(MatrixStack matrixStack, IVertexBuilder buffer, Vector3d delta, Direction direction, float r, float g, float b, float a) {
		Matrix4f pose = matrixStack.last().pose();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		if(direction == null || direction == Direction.NORTH) {
			// Face North, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face North, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.SOUTH) {
			// Face South, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
			// Face South, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.WEST) {
			// Face West, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
			// Face West, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
		}
		if(direction == null || direction == Direction.EAST) {
			// Face East, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face East, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == Direction.DOWN) {
			// Face Down
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
		}
		if(direction == Direction.UP) {
			// Face Up
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).endVertex();
			// Face North, Edge East
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).endVertex();
			// Face South, Edge East
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
			// Face South, Edge West
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).endVertex();
		}
	}

	public void addBeamVertices(MatrixStack matrixStack, IVertexBuilder buffer, Vector3d delta, float r, float g, float b, float a) {
		Matrix4f pose = matrixStack.last().pose();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).endVertex();
		buffer.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
	}

	public static class BeamInfo {

		private Vector3d source;
		private Vector3d delta;
		private int startTick;

		public BeamInfo(Vector3d source, Vector3d delta) {
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

	public static class RenderTypeHelper extends RenderType {

		private RenderTypeHelper(String name, VertexFormat format, int mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
			super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
		}

		public static final RenderType MARKER_LINE_4;
		public static final RenderType MARKER_QUAD;
		public static final RenderType BEAM_LINE_3;
		public static final IRenderTypeBuffer.Impl BUFFERS;

		static {
			MARKER_LINE_4 = create("packagedauto:marker_line_4",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, false, false,
					State.builder().
					setLineState(new LineState(OptionalDouble.of(4))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			MARKER_QUAD = create("packagedauto:marker_quad",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 128, false, false,
					State.builder().
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			BEAM_LINE_3 = create("packagedauto:beam_line_3",
					DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 8192, false, false,
					State.builder().
					setLineState(new LineState(OptionalDouble.of(3))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setOutputState(ITEM_ENTITY_TARGET).
					setWriteMaskState(COLOR_DEPTH_WRITE).
					setCullState(NO_CULL).
					createCompositeState(false));
			BUFFERS = IRenderTypeBuffer.immediateWithBuffers(
					ImmutableMap.of(MARKER_LINE_4, new BufferBuilder(MARKER_LINE_4.bufferSize()),
							MARKER_QUAD, new BufferBuilder(MARKER_QUAD.bufferSize()),
							BEAM_LINE_3, new BufferBuilder(BEAM_LINE_3.bufferSize())),
					Tessellator.getInstance().getBuilder());
		}
	}
}
