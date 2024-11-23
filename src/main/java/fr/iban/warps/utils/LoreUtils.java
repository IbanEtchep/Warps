package fr.iban.warps.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoreUtils {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\[SPLIT:(\\d+)]");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    /**
     * Traite le lore en découpant les lignes contenant un marqueur [SPLIT:X]
     * @param originalLore Le lore original
     * @return Le nouveau lore avec les lignes découpées selon la valeur dans le marqueur
     */
    public static List<Component> processLoreWithSplit(List<Component> originalLore) {
        if (originalLore == null) {
            return new ArrayList<>();
        }

        List<Component> newLore = new ArrayList<>();

        for (Component line : originalLore) {
            String legacyText = LEGACY_SERIALIZER.serialize(line);
            var matcher = SPLIT_PATTERN.matcher(legacyText);

            if (matcher.find()) {
                int maxLength = Integer.parseInt(matcher.group(1));
                String textWithoutMarker = legacyText.replaceAll(SPLIT_PATTERN.pattern(), "");

                List<String> splitLines = splitLegacyText(textWithoutMarker, maxLength);
                for (String splitLine : splitLines) {
                    newLore.add(LEGACY_SERIALIZER.deserialize(splitLine));
                }
            } else {
                newLore.add(line);
            }
        }

        return newLore;
    }

    /**
     * Découpe un texte legacy en préservant les codes couleur
     */
    private static List<String> splitLegacyText(String legacyText, int maxLength) {
        List<String> lines = new ArrayList<>();
        String lastColors;

        while (!legacyText.isEmpty()) {
            // Convertir en Component puis utiliser PlainTextSerializer pour obtenir la longueur réelle
            String plainText = PLAIN_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(legacyText));

            if (plainText.length() <= maxLength) {
                lines.add(legacyText);
                break;
            }

            int splitIndex = findSplitIndex(legacyText, plainText, maxLength);
            String currentLine = legacyText.substring(0, splitIndex).trim();
            lastColors = getLastColors(currentLine);

            lines.add(currentLine);
            legacyText = lastColors + legacyText.substring(splitIndex).trim();
        }

        return lines;
    }

    /**
     * Trouve l'index approprié pour couper le texte
     */
    private static int findSplitIndex(String legacyText, String plainText, int maxLength) {
        if (plainText.length() <= maxLength) {
            return legacyText.length();
        }

        String relevantPlainPart = plainText.substring(0, maxLength);
        int lastSpace = relevantPlainPart.lastIndexOf(' ');

        if (lastSpace == -1) {
            return getLegacyIndex(legacyText, maxLength);
        }

        return getLegacyIndex(legacyText, lastSpace);
    }

    /**
     * Convertit un index de texte plain en index de texte avec couleur
     */
    private static int getLegacyIndex(String legacyText, int plainIndex) {
        int legacyIndex = 0;
        int currentPlainIndex = 0;

        while (currentPlainIndex < plainIndex && legacyIndex < legacyText.length()) {
            if (legacyText.charAt(legacyIndex) == '&' &&
                    legacyIndex + 1 < legacyText.length() &&
                    "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(legacyText.charAt(legacyIndex + 1)) > -1) {
                legacyIndex += 2;
            } else {
                legacyIndex++;
                currentPlainIndex++;
            }
        }

        return legacyIndex;
    }

    /**
     * Récupère les derniers codes couleur d'un texte
     */
    private static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        for (int index = length - 2; index >= 0; index--) {
            if (input.charAt(index) == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(input.charAt(index + 1)) > -1) {
                result.insert(0, input.substring(index, index + 2));
            }
        }

        return result.toString();
    }
}

