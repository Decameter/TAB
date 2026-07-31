package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.belowname.BelowName;
import me.neznamy.tab.shared.hook.ApolloHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class computing and sending nametag overrides for viewers using Lunar Client via the Apollo API.
 * This runs independently of vanilla scoreboard teams and only affects players who are viewing
 * the server using Lunar Client, without changing anything for viewers who are not.
 */
public class ApolloNameTagSender {

    /** Instance of the class */
    @Getter
    private static final ApolloNameTagSender instance = new ApolloNameTagSender();

    private ApolloNameTagSender() {
        ApolloHook.getInstance().onPlayerRegistered(this::onApolloPlayerRegistered);
    }

    /**
     * (Re-)sends nametag override of {@code target} to {@code viewer} if viewer is running
     * Lunar Client with Apollo support. Does nothing otherwise.
     *
     * @param   viewer
     *          Player viewing the nametag
     * @param   target
     *          Player whose nametag is being viewed
     */
    public void update(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        if (!ApolloHook.getInstance().isInstalled()) return;
        if (!ApolloHook.getInstance().isApolloPlayer(viewer.getUniqueId())) return;
        ApolloHook.getInstance().overrideNametag(viewer.getUniqueId(), target.getUniqueId(), computeLines(viewer, target));
    }

    /**
     * Resets nametag override of {@code target} for {@code viewer} if viewer is running
     * Lunar Client with Apollo support. Does nothing otherwise.
     *
     * @param   viewer
     *          Player who should no longer see an override
     * @param   target
     *          Player whose nametag override should be reset
     */
    public void reset(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        if (!ApolloHook.getInstance().isInstalled()) return;
        if (!ApolloHook.getInstance().isApolloPlayer(viewer.getUniqueId())) return;
        ApolloHook.getInstance().resetNametag(viewer.getUniqueId(), target.getUniqueId());
    }

    /**
     * Re-sends nametag override of {@code target} to everyone who currently has target's
     * vanilla scoreboard team registered. Used when target's override configuration changes
     * via the API.
     *
     * @param   target
     *          Player whose override should be re-sent to all current viewers
     */
    public void refreshForTarget(@NonNull TabPlayer target) {
        if (!ApolloHook.getInstance().isInstalled()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.teamData.hasTeamRegistered(target)) update(viewer, target);
        }
    }

    /**
     * Called once a player registers with Apollo. Since packets sent before this point may be
     * silently dropped by the client, all overrides this player should currently be seeing
     * are re-sent once they are ready to receive them.
     *
     * @param   viewerId
     *          UUID of the player who just registered with Apollo
     */
    private void onApolloPlayerRegistered(@NotNull java.util.UUID viewerId) {
        TabPlayer viewer = TAB.getInstance().getPlayer(viewerId);
        if (viewer == null) return;
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.teamData.hasTeamRegistered(target)) update(viewer, target);
        }
    }

    /**
     * Computes nametag lines to display to {@code viewer} for {@code target}, in order.
     * If target's API override or group's "apollo" configuration is set, those lines are used.
     * Otherwise, lines are composed to mirror the vanilla nametag format (tagprefix + name + tagsuffix),
     * with an additional line for belowname-objective's fancy value if that feature is enabled.
     *
     * @param   viewer
     *          Player who will see the nametag
     * @param   target
     *          Player who owns the nametag
     * @return  Lines to display, top to bottom
     */
    @NotNull
    private List<String> computeLines(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        List<Property> lines = target.teamData.apiApolloNametagOverride;
        if (lines == null) lines = target.teamData.apolloNametagOverride;
        if (lines != null && !lines.isEmpty()) {
            List<String> resolved = new ArrayList<>(lines.size());
            for (Property line : lines) {
                resolved.add(line.getFormat(viewer));
            }
            return resolved;
        }
        return defaultLines(viewer, target);
    }

    /**
     * Composes default nametag lines when no custom "apollo" override is configured, mirroring
     * the vanilla nametag format and adding belowname-objective's fancy value as a second line
     * if that feature is currently enabled for the target.
     *
     * @param   viewer
     *          Player who will see the nametag
     * @param   target
     *          Player who owns the nametag
     * @return  Default lines to display, top to bottom
     */
    @NotNull
    private List<String> defaultLines(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        List<String> lines = new ArrayList<>(2);
        lines.add(target.teamData.prefix.getFormat(viewer) + target.getNickname() + target.teamData.suffix.getFormat(viewer));
        BelowName belowName = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
        if (belowName != null && !target.belowNameData.disabled.get() && target.belowNameData.apolloFancyValue != null) {
            String value = target.belowNameData.apolloFancyValue.getFormat(viewer);
            if (!value.isEmpty()) lines.add(value);
        }
        return lines;
    }
}
