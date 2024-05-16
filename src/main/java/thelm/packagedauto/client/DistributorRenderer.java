package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.block.entity.DistributorBlockEntity;

// Based on Botania, Scannables, and AE2
@SuppressWarnings("removal")
public class DistributorRenderer {

	public static final DistributorRenderer INSTANCE = new DistributorRenderer();
	public static final Vec3 BLOCK_SIZE = new Vec3(1, 1, 1);
	public static final int BEAM_LIFETIME = 6;

	private DistributorRenderer() {}

	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelLast);
	}

	public void onRenderLevelLast(RenderLevelLastEvent event) {
		Player player = Minecraft.getInstance().player;
		for(InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if(stack.getItem() instanceof IDistributorMarkerItem marker) {
				renderMarker(event.getPoseStack(), marker.getDirectionalGlobalPos(stack));
			}
		}

		renderBeams(event.getPoseStack(), event.getPartialTick());
	}

	public void addBeam(Vec3 source, Vec3 delta) {
		beams.add(new BeamInfo(source, delta));
	}

	public void renderMarker(PoseStack poseStack, DirectionalGlobalPos globalPos) {
		if(globalPos == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if(!globalPos.dimension().equals(mc.level.dimension())) {
			return;
		}

		int range = 2*DistributorBlockEntity.range+2;
		BlockPos blockPos = globalPos.blockPos();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		Vec3 distVec = cameraPos.subtract(Vec3.atCenterOf(blockPos));
		if(Doubles.max(Math.abs(distVec.x), Math.abs(distVec.y), Math.abs(distVec.z)) > range) {
			return;
		}

		MultiBufferSource.BufferSource buffers = RenderTypeHelper.BUFFERS;
		VertexConsumer quadBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_QUAD);
		VertexConsumer lineBuffer = buffers.getBuffer(RenderTypeHelper.MARKER_LINE_4);

		poseStack.pushPose();
		poseStack.translate(blockPos.getX()-cameraPos.x, blockPos.getY()-cameraPos.y, blockPos.getZ()-cameraPos.z);

		Direction direction = globalPos.direction();
		addMarkerVertices(poseStack, quadBuffer, BLOCK_SIZE, direction, 0F, 1F, 1F, 0.5F);
		addMarkerVertices(poseStack, lineBuffer, BLOCK_SIZE, null, 0F, 1F, 1F, 1F);

		poseStack.popPose();

		RenderSystem.disableDepthTest();
		buffers.endBatch();
		RenderSystem.enableDepthTest();
	}

	public void renderBeams(PoseStack poseStack, float partialTick) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+partialTick;
		Minecraft mc = Minecraft.getInstance();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		MultiBufferSource.BufferSource buffers = RenderTypeHelper.BUFFERS;
		VertexConsumer lineBuffer = buffers.getBuffer(RenderTypeHelper.BEAM_LINE_3);

		for(BeamInfo beam : beams) {
			Vec3 source = beam.source();

			poseStack.pushPose();
			poseStack.translate(source.x-cameraPos.x, source.y-cameraPos.y, source.z-cameraPos.z);

			addBeamVertices(poseStack, lineBuffer, beam.delta(), 0F, 1F, 1F, beam.getAlpha(renderTick));

			poseStack.popPose();
		}

		buffers.endBatch();
	}

	public void addMarkerVertices(PoseStack poseStack, VertexConsumer buffer, Vec3 delta, Direction direction, float r, float g, float b, float a) {
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		if(direction == null || direction == Direction.NORTH) {
			// Face North, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			// Face North, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == null || direction == Direction.SOUTH) {
			// Face South, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			// Face South, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
		}
		if(direction == null || direction == Direction.WEST) {
			// Face West, Edge Bottom
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			// Face West, Edge Top
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
		}
		if(direction == null || direction == Direction.EAST) {
			// Face East, Edge Bottom
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
			// Face East, Edge Top
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
		}
		if(direction == Direction.DOWN) {
			// Face Down
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == Direction.UP) {
			// Face Up
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(pose, 0, y, 0).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			// Face North, Edge East
			buffer.vertex(pose, x, y, 0).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			buffer.vertex(pose, x, 0, 0).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			// Face South, Edge East
			buffer.vertex(pose, x, 0, z).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
			// Face South, Edge West
			buffer.vertex(pose, 0, y, z).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
			buffer.vertex(pose, 0, 0, z).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
		}
	}

	public void addBeamVertices(PoseStack poseStack, VertexConsumer buffer, Vec3 delta, float r, float g, float b, float a) {
		Vec3 normalVec = delta.normalize();
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		float xn = (float)normalVec.x;
		float yn = (float)normalVec.y;
		float zn = (float)normalVec.z;
		buffer.vertex(pose, 0, 0, 0).color(r, g, b, a).normal(normal, xn, yn, zn).endVertex();
		buffer.vertex(pose, x, y, z).color(r, g, b, a).normal(normal, xn, yn, zn).endVertex();
	}

	public static record BeamInfo(Vec3 source, Vec3 delta, int startTick) {

		public BeamInfo(Vec3 source, Vec3 delta) {
			this(source, delta, RenderTimer.INSTANCE.getTicks());
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

		private RenderTypeHelper(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
			super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
		}

		public static final RenderType MARKER_LINE_4;
		public static final RenderType MARKER_QUAD;
		public static final RenderType BEAM_LINE_3;
		public static final MultiBufferSource.BufferSource BUFFERS;

		static {
			MARKER_LINE_4 = create("packagedauto:marker_line_4",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 128, false, false,
					CompositeState.builder().
					setShaderState(RENDERTYPE_LINES_SHADER).
					setLineState(new LineStateShard(OptionalDouble.of(4))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			MARKER_QUAD = create("packagedauto:marker_quad",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 128, false, false,
					CompositeState.builder().
					setShaderState(RenderStateShard.POSITION_COLOR_SHADER).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setWriteMaskState(COLOR_WRITE).
					setDepthTestState(NO_DEPTH_TEST).
					setCullState(NO_CULL).
					createCompositeState(false));
			BEAM_LINE_3 = create("packagedauto:beam_line_3",
					DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 8192, false, false,
					CompositeState.builder().
					setShaderState(RENDERTYPE_LINES_SHADER).
					setLineState(new LineStateShard(OptionalDouble.of(3))).
					setLayeringState(VIEW_OFFSET_Z_LAYERING).
					setTransparencyState(TRANSLUCENT_TRANSPARENCY).
					setOutputState(ITEM_ENTITY_TARGET).
					setWriteMaskState(COLOR_DEPTH_WRITE).
					setCullState(NO_CULL).
					createCompositeState(false));
			BUFFERS = MultiBufferSource.immediateWithBuffers(
					Map.of(MARKER_LINE_4, new BufferBuilder(MARKER_LINE_4.bufferSize()),
							MARKER_QUAD, new BufferBuilder(MARKER_QUAD.bufferSize()),
							BEAM_LINE_3, new BufferBuilder(BEAM_LINE_3.bufferSize())),
					Tesselator.getInstance().getBuilder());
		}
	}
}
