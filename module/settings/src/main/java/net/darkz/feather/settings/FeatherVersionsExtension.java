package net.darkz.feather.settings;

import org.gradle.api.initialization.Settings;

import javax.inject.Inject;
import java.util.*;

/**
 * DSL extension registered in {@code settings.gradle} by
 * {@link FeatherPluginSettings}.
 */
public class FeatherVersionsExtension {

    private final List<Entry> entries = new ArrayList<>();

    @Inject
    public FeatherVersionsExtension(Settings settings) {}

    /**
     * Declare a Minecraft version together with the loaders you target.
     *
     * @param mcVersion minecraft version string (e.g. {@code "1.21.1"})
     * @param loaders   one or more loader ids: fabric, quilt, forge, neoforge
     */
    public void version(String mcVersion, String... loaders) {
        if (loaders.length == 0) {
            throw new IllegalArgumentException(
                    "[FeatherPlugin] version('" + mcVersion + "') requires at least one loader.");
        }
        entries.add(new Entry(mcVersion, List.of(loaders)));
    }

    /** Returns all declared entries (immutable view). */
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int totalLoaderCount() {
        return entries.stream().mapToInt(e -> e.loaders().size()).sum();
    }

    // ──────────────────────────────────────────────────────────────

    public record Entry(String mcVersion, List<String> loaders) {}
}
