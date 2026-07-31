package me.neznamy.tab.shared.hook;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.event.EventBus;
import com.lunarclient.apollo.event.player.ApolloRegisterPlayerEvent;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.module.nametag.NametagModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import com.lunarclient.apollo.recipients.Recipients;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class for hooking into Lunar Client's Apollo API to override nametags
 * for players who are viewing them using Lunar Client.
 */
public class ApolloHook {

    /** Instance of the class */
    @Getter
    private static final ApolloHook instance = new ApolloHook();

    /** Flag tracking if Apollo API is installed or not */
    @Getter
    private final boolean installed = ReflectionUtils.classExists("com.lunarclient.apollo.Apollo");

    private ApolloHook() {
    }

    /**
     * Registers a callback that will be invoked with the UUID of a player once they
     * register with Apollo (start using Lunar Client). Does nothing if Apollo is not installed.
     * This should be used to (re-)send overrides, since packets sent before this event
     * fires may be silently dropped by the client.
     *
     * @param   callback
     *          Callback receiving the UUID of the newly registered player
     */
    public void onPlayerRegistered(@NotNull Consumer<UUID> callback) {
        if (!installed) return;
        try {
            EventBus.getBus().register(ApolloRegisterPlayerEvent.class, event -> callback.accept(event.getPlayer().getUniqueId()));
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to register Apollo player registration listener", t);
        }
    }

    /**
     * Returns {@code true} if given player is currently viewing the server using Lunar Client
     * with Apollo support, {@code false} if not.
     *
     * @param   uniqueId
     *          UUID of player to check
     * @return  {@code true} if player is running Lunar Client with Apollo support, {@code false} if not
     */
    public boolean isApolloPlayer(@NotNull UUID uniqueId) {
        if (!installed) return false;
        try {
            return Apollo.getPlayerManager().hasSupport(uniqueId);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Overrides nametag of {@code target} for {@code viewer} using the Apollo Nametag module.
     * Lines are expected to be already colorized using {@code &} color codes, in the order they
     * should be displayed top to bottom. Does nothing if Apollo is not installed, viewer is not
     * an Apollo player or the Nametag module is disabled.
     *
     * @param   viewer
     *          UUID of the viewing player
     * @param   target
     *          UUID of the player whose nametag should be overridden
     * @param   lines
     *          Lines to display, top to bottom, using {@code &} color codes
     */
    public void overrideNametag(@NotNull UUID viewer, @NotNull UUID target, @NotNull List<String> lines) {
        if (!installed || lines.isEmpty()) return;
        try {
            Optional<ApolloPlayer> apolloViewer = Apollo.getPlayerManager().getPlayer(viewer);
            if (!apolloViewer.isPresent()) return;
            NametagModule module = Apollo.getModuleManager().getModule(NametagModule.class);
            if (!module.isEnabled()) return;
            List<Component> components = new ArrayList<>(lines.size());
            for (String line : lines) {
                components.add(LegacyComponentSerializer.legacySection().deserialize(EnumChatFormat.color(line)));
            }
            Collections.reverse(components); // Apollo renders lines in the reversed order
            Recipients recipients = Recipients.of(Collections.singletonList(apolloViewer.get()));
            module.overrideNametag(recipients, target, Nametag.builder().lines(components).build());
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to override Apollo nametag of " + target + " for " + viewer, t);
        }
    }

    /**
     * Resets nametag override of {@code target} for {@code viewer} previously set using
     * {@link #overrideNametag(UUID, UUID, List)}. Does nothing if Apollo is not installed
     * or viewer is not an Apollo player.
     *
     * @param   viewer
     *          UUID of the viewing player
     * @param   target
     *          UUID of the player whose nametag override should be reset
     */
    public void resetNametag(@NotNull UUID viewer, @NotNull UUID target) {
        if (!installed) return;
        try {
            Optional<ApolloPlayer> apolloViewer = Apollo.getPlayerManager().getPlayer(viewer);
            if (!apolloViewer.isPresent()) return;
            Recipients recipients = Recipients.of(Collections.singletonList(apolloViewer.get()));
            Apollo.getModuleManager().getModule(NametagModule.class).resetNametag(recipients, target);
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to reset Apollo nametag of " + target + " for " + viewer, t);
        }
    }
}
