package me.neznamy.tab.api.belowname;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for manipulating the BelowName objective's displayed value.
 * <p>
 * Instance can be obtained using {@link TabAPI#getBelowNameManager()}.
 * This requires the BelowName feature to be enabled in config, otherwise the method will
 * return {@code null}.
 */
@SuppressWarnings("unused") // API class
public interface BelowNameManager {

    /**
     * Overrides the "fancy value" shown to viewers for the specified player, on both the
     * classic scoreboard BelowName objective (1.20.3+ clients) and the extra nametag line
     * shown to viewers using Lunar Client (via the Apollo API). Supports placeholders as well
     * as any supported RGB formats. Use {@code null} to reset back to the value configured
     * in "fancy-value" / "apollo-fancy-value" (or their defaults if not configured).
     * <p>
     * This has no effect on pre-1.20.3 clients viewing the player, which only support the
     * numeric "value".
     *
     * @param   player
     *          player to change the fancy value of
     * @param   fancyValue
     *          new fancy value to display, or {@code null} to reset
     * @see     #getFancyValue(TabPlayer)
     */
    void setFancyValue(@NonNull TabPlayer player, @Nullable String fancyValue);

    /**
     * Returns fancy value assigned using {@link #setFancyValue(TabPlayer, String)}.
     * If no custom fancy value is set using the API, returns {@code null}.
     *
     * @param   player
     *          Player to get custom fancy value of
     * @return  Custom fancy value assigned using the API, or {@code null} if not set
     * @see     #setFancyValue(TabPlayer, String)
     */
    @Nullable
    String getFancyValue(@NonNull TabPlayer player);

    /**
     * Overrides the BelowName objective's title shown to {@code player} on their own scoreboard
     * (Minecraft appends this after every entry's fancy value/number on their screen). Supports
     * placeholders as well as any supported RGB formats. Use {@code null} to reset back to the
     * value configured in "title" (or its default if not configured).
     *
     * @param   player
     *          player to change the objective title of
     * @param   title
     *          new title to display, or {@code null} to reset
     * @see     #getTitle(TabPlayer)
     */
    void setTitle(@NonNull TabPlayer player, @Nullable String title);

    /**
     * Returns title assigned using {@link #setTitle(TabPlayer, String)}.
     * If no custom title is set using the API, returns {@code null}.
     *
     * @param   player
     *          Player to get custom title of
     * @return  Custom title assigned using the API, or {@code null} if not set
     * @see     #setTitle(TabPlayer, String)
     */
    @Nullable
    String getTitle(@NonNull TabPlayer player);
}
