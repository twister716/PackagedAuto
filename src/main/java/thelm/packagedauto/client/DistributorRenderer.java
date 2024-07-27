package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

import org.joml.Matrix4fStack;

import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.DeltaTracker;
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
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;

// Based on Botania, Scannables, and AE2
public class DistributorRenderer {

	public static final DistributorRenderer INSTANCE = new DistributorRenderer();
	public static final Vec3 BLOCK_SIZE = new Vec3(1, 1, 1);
	public static final int BEAM_LIFETIME = 6;

	private DistributorRenderer() {}

	private List<BeamInfo> beams = new LinkedList<>();

	public void onConstruct() {
		NeoForge.EVENT_BUS.addListener(this::onRenderLevel);
	}

	public void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
		event.registerRenderBuffer(RenderTypeHelper.MARKER_LINE_4);
		event.registerRenderBuffer(RenderTypeHelper.MARKER_QUAD);
		event.registerRenderBuffer(RenderTypeHelper.BEAM_LINE_3);
	}

	public void onRenderLevel(RenderLevelStageEvent event) {
		if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			return;
		}

		Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.pushMatrix();
		matrixStack.mul(event.getModelViewMatrix());
		RenderSystem.applyModelViewMatrix();

		Player player = Minecraft.getInstance().player;
		for(InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
				renderMarker(event.getPoseStack(), stack.get(PackagedAutoDataComponents.MARKER_POS));
			}
		}

		renderBeams(event.getPoseStack(), event.getPartialTick());

		matrixStack.popMatrix();
		RenderSystem.applyModelViewMatrix();
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
		Vec3 distVec = cameraPos.subtract(blockPos.getCenter());
		if(Doubles.max(Math.abs(distVec.x), Math.abs(distVec.y), Math.abs(distVec.z)) > range) {
			return;
		}

		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
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

	public void renderBeams(PoseStack poseStack, DeltaTracker deltaTracker) {
		int currentTick = RenderTimer.INSTANCE.getTicks();
		beams.removeIf(beam->beam.shouldRemove(currentTick));

		float renderTick = currentTick+deltaTracker.getGameTimeDeltaPartialTick(true);
		Minecraft mc = Minecraft.getInstance();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
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
		Pose pose = poseStack.last();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		if(direction == null || direction == Direction.NORTH) {
			// Face North, Edge Bottom
			buffer.addVertex(pose, 0, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			// Face North, Edge Top
			buffer.addVertex(pose, x, y, 0).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
			buffer.addVertex(pose, 0, y, 0).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
		}
		if(direction == null || direction == Direction.SOUTH) {
			// Face South, Edge Bottom
			buffer.addVertex(pose, x, 0, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
			buffer.addVertex(pose, 0, 0, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
			// Face South, Edge Top
			buffer.addVertex(pose, 0, y, z).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
		}
		if(direction == null || direction == Direction.WEST) {
			// Face West, Edge Bottom
			buffer.addVertex(pose, 0, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
			buffer.addVertex(pose, 0, 0, z).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
			// Face West, Edge Top
			buffer.addVertex(pose, 0, y, z).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
			buffer.addVertex(pose, 0, y, 0).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
		}
		if(direction == null || direction == Direction.EAST) {
			// Face East, Edge Bottom
			buffer.addVertex(pose, x, 0, z).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
			buffer.addVertex(pose, x, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
			// Face East, Edge Top
			buffer.addVertex(pose, x, y, 0).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
			buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
		}
		if(direction == Direction.DOWN) {
			// Face Down
			buffer.addVertex(pose, 0, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, 0, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
			buffer.addVertex(pose, 0, 0, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
		}
		if(direction == Direction.UP) {
			// Face Up
			buffer.addVertex(pose, 0, y, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, y, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
			buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
			buffer.addVertex(pose, 0, y, z).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
		}
		if(direction == null) {
			// Face North, Edge West
			buffer.addVertex(pose, 0, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, 0, y, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
			// Face North, Edge East
			buffer.addVertex(pose, x, y, 0).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, x, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
			// Face South, Edge East
			buffer.addVertex(pose, x, 0, z).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
			// Face South, Edge West
			buffer.addVertex(pose, 0, y, z).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, 0, 0, z).setColor(r, g, b, a).setNormal(pose, 0, -1, 0);
		}
	}

	public void addBeamVertices(PoseStack poseStack, VertexConsumer buffer, Vec3 delta, float r, float g, float b, float a) {
		Vec3 normalVec = delta.normalize();
		Pose pose = poseStack.last();
		float x = (float)delta.x;
		float y = (float)delta.y;
		float z = (float)delta.z;
		float xn = (float)normalVec.x;
		float yn = (float)normalVec.y;
		float zn = (float)normalVec.z;
		buffer.addVertex(pose, 0, 0, 0).setColor(r, g, b, a).setNormal(pose, xn, yn, zn);
		buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setNormal(pose, xn, yn, zn);
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
		}
	}
}
