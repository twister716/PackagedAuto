package thelm.packagedauto.api;

import java.util.Locale;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum PatternType implements StringRepresentable {
	PACKAGE,
	RECIPE,
	DIRECT;

	public static final Codec<PatternType> CODEC = StringRepresentable.fromValues(PatternType::values);
    public static final IntFunction<PatternType> BY_ID = ByIdMap.continuous(PatternType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, PatternType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PatternType::ordinal);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.US);
	}
}
