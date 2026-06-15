package net.darkz.feather.common;

/**
 * Enum representing all supported Minecraft mod loaders.
 * Used by every FeatherPlugin module to branch configuration logic.
 */
public enum ModLoader {

    FABRIC("fabric", "net.fabricmc.loom") {
        @Override public boolean isFabricLike() { return true; }
    },
    QUILT("quilt", "org.quiltmc.loom") {
        @Override public boolean isFabricLike() { return true; }
    },
    FORGE("forge", "net.minecraftforge.gradle") {
        @Override public boolean isForge() { return true; }
    },
    NEOFORGE("neoforge", "net.neoforged.moddev") {
        @Override public boolean isNeoForge() { return true; }
    };

    /** Lower-case identifier used in gradle.properties (e.g. {@code mod_loader=fabric}). */
    public final String id;

    /** Gradle plugin id that needs to be applied for this loader. */
    public final String gradlePluginId;

    ModLoader(String id, String gradlePluginId) {
        this.id = id;
        this.gradlePluginId = gradlePluginId;
    }

    public boolean isFabricLike() { return false; }
    public boolean isForge()      { return false; }
    public boolean isNeoForge()   { return false; }

    /**
     * Parse a loader from the value stored in {@code gradle.properties}.
     *
     * @param value raw property value
     * @return matched {@link ModLoader}
     * @throws IllegalArgumentException when the value is unknown
     */
    public static ModLoader fromProperty(String value) {
        String v = value == null ? "" : value.trim().toLowerCase();
        for (ModLoader loader : values()) {
            if (loader.id.equals(v)) return loader;
        }
        throw new IllegalArgumentException(
                "[FeatherPlugin] Unknown mod_loader value: '" + value + "'. " +
                "Allowed values: fabric, quilt, forge, neoforge"
        );
    }
}
