package thelm.packagedauto.api;

import java.util.Arrays;

public enum PatternType {
	PACKAGE,
	RECIPE,
	DIRECT;

	public static PatternType fromName(String name) {
		return Arrays.stream(values()).filter(t->t.name().equalsIgnoreCase(name)).findAny().orElse(null);
	}
}
