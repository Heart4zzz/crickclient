package zov.crickclient.util.server;

import lombok.experimental.UtilityClass;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import zov.crickclient.CrickClient;
import zov.crickclient.module.list.misc.ScoreboardHealth;
import zov.crickclient.util.IMinecraft;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Server implements IMinecraft {

    private static final Pattern HEALTH_NUMBER = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)");

    public int getPing(PlayerEntity entity) {
        PlayerListEntry list = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return list != null ? list.getLatency() : 0;
    }

    public float getHealth(LivingEntity entity, boolean gapple) {
        float absorption = gapple ? entity.getAbsorptionAmount() : 0f;
        if (Float.isNaN(absorption) || absorption < 0f) absorption = 0f;

        if (entity instanceof PlayerEntity player) {
            Float scoreboardHp = readBelowNameHealth(player);
            boolean scoreboardOnly = CrickClient.getInstance().getModuleStorage().get(ScoreboardHealth.class).isEnabled();

            if (scoreboardHp != null && !Float.isNaN(scoreboardHp)) {
                return Math.max(0f, scoreboardHp) + absorption;
            }
            if (scoreboardOnly) {
                return 0f;
            }
        }

        float hp = entity.getHealth();
        if (Float.isNaN(hp) || hp < 0f) hp = 0f;
        return hp + absorption;
    }

    /** Читает HP из BELOW_NAME (Relic, FunTime и др.), если objective есть. */
    public Float readBelowNameHealth(PlayerEntity player) {
        if (player == null || player.getScoreboard() == null) return null;

        ScoreboardObjective objective = player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (objective == null) return null;

        ReadableScoreboardScore score = player.getScoreboard().getScore(player, objective);
        if (score == null) return null;

        MutableText text = ReadableScoreboardScore.getFormattedScore(
                score, objective.getNumberFormatOr(StyledNumberFormat.EMPTY));
        return parseHealthString(text.getString());
    }

    public float parseHealthString(String raw) {
        if (raw == null || raw.isBlank()) return Float.NaN;

        String normalized = raw.trim().toLowerCase(Locale.ROOT)
                .replace("hp", "")
                .replace("хп", "")
                .replace("❤", "")
                .trim();

        Matcher matcher = HEALTH_NUMBER.matcher(normalized);
        if (!matcher.find()) return Float.NaN;

        String number = matcher.group(1).replace(',', '.');
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException ignored) {
            return Float.NaN;
        }
    }

    /** Для TargetHUD / NameTags: целое или одна цифра после запятой. */
    public String formatHealthDisplay(float hp) {
        if (Float.isNaN(hp) || hp < 0f) hp = 0f;
        float rounded = Math.round(hp * 10f) / 10f;
        if (Math.abs(rounded - Math.round(rounded)) < 0.05f) {
            return String.valueOf(Math.round(rounded));
        }
        return String.format(Locale.US, "%.1f", rounded);
    }
}
