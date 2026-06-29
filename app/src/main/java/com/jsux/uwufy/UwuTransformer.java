package com.jsux.uwufy;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UwuTransformer {
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("([A-Za-z]+(?:'[A-Za-z]+)?|\\S+|\\s+)");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(?:https?://\\S+|www\\.\\S+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
    private static final Pattern ACRONYM_PATTERN = Pattern.compile("^[A-Z0-9]{2,}$");

    private static final List<String> FACES = Arrays.asList(
            "(uwu)",
            "(owo)",
            "(◕‿◕)",
            "(^･ω･^)",
            "(｡•̀ᴗ-)✧",
            ":3",
            ">w<"
    );

    private static final List<String> ACTIONS = Arrays.asList(
            "*blushes*",
            "*giggles*",
            "*nuzzles*",
            "*hides face*",
            "*wiggles happily*",
            "*boops*"
    );

    private static final List<String> EXCLAMATIONS = Arrays.asList(
            "!!",
            "!!!",
            "~",
            "!!?",
            "~!!"
    );

    private static final java.util.Map<String, String> DICT = new java.util.HashMap<>();

    static {
        DICT.put("hello", "hewwo");
        DICT.put("hi", "hai");
        DICT.put("hey", "hewwo");
        DICT.put("love", "wuv");
        DICT.put("you", "yuw");
        DICT.put("your", "yuwr");
        DICT.put("friend", "fwiend");
        DICT.put("friends", "fwiends");
        DICT.put("cute", "kewte");
        DICT.put("small", "smol");
        DICT.put("very", "bery");
        DICT.put("the", "da");
        DICT.put("this", "dis");
        DICT.put("that", "dat");
        DICT.put("there", "dere");
        DICT.put("really", "weawwy");
        DICT.put("good", "gud");
        DICT.put("great", "gweat");
        DICT.put("no", "nyo");
        DICT.put("yes", "yis");
        DICT.put("thanks", "thwanks");
        DICT.put("please", "pwease");
    }

    private UwuTransformer() {
    }

    public static String uwuify(String input, UwuConfig cfg) {
        if (TextUtils.isEmpty(input) || cfg == null || !cfg.enabled) {
            return input;
        }

        if (input.length() < cfg.minLength) {
            return input;
        }

        java.util.List<String> placeholders = new java.util.ArrayList<>();
        String protectedInput = protectTokens(input, cfg, placeholders);

        Random random = new Random(seed(input, cfg));
        StringBuilder out = new StringBuilder(protectedInput.length() + 32);
        Matcher matcher = TOKEN_PATTERN.matcher(protectedInput);
        int wordIndex = 0;

        while (matcher.find()) {
            String token = matcher.group(1);
            if (token == null) {
                continue;
            }

            if (token.startsWith("__UWU_SAFE_") && token.endsWith("__")) {
                out.append(restoreToken(token, placeholders));
                continue;
            }

            if (isWord(token)) {
                out.append(transformWord(token, cfg, random, wordIndex++));
                continue;
            }

            out.append(token);
        }

        return addEmbellishments(out.toString(), random, cfg);
    }

    private static String protectTokens(String input, UwuConfig cfg, java.util.List<String> placeholders) {
        String out = input;
        if (cfg.preserveUrls) {
            out = protectWithPattern(out, URL_PATTERN, placeholders);
        }
        if (cfg.preserveEmails) {
            out = protectWithPattern(out, EMAIL_PATTERN, placeholders);
        }
        return out;
    }

    private static String protectWithPattern(String input, Pattern pattern, java.util.List<String> placeholders) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String placeholder = "__UWU_SAFE_" + placeholders.size() + "__";
            placeholders.add(matcher.group());
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(placeholder));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String restoreToken(String token, java.util.List<String> placeholders) {
        String middle = token.substring("__UWU_SAFE_".length(), token.length() - 2);
        try {
            int index = Integer.parseInt(middle);
            if (index >= 0 && index < placeholders.size()) {
                return placeholders.get(index);
            }
        } catch (Throwable ignored) {
        }
        return token;
    }

    private static String transformWord(String word, UwuConfig cfg, Random random, int wordIndex) {
        String lower = word.toLowerCase(Locale.ROOT);

        if (cfg.preserveAcronyms && ACRONYM_PATTERN.matcher(word).matches()) {
            return word;
        }

        String transformed = DICT.get(lower);
        if (transformed == null) {
            transformed = lower;
            transformed = transformed.replace("th", "d");
            transformed = transformed.replace("ove", "uv");
            transformed = transformed.replace("r", "w");
            transformed = transformed.replace("l", "w");
            transformed = transformed.replace("na", "nya");
            transformed = transformed.replace("ne", "nye");
            transformed = transformed.replace("ni", "nyi");
            transformed = transformed.replace("no", "nyo");
            transformed = transformed.replace("nu", "nyu");
            transformed = fixEnding(transformed);
        }

        if (random.nextInt(100) < cfg.stutterPct && transformed.length() > 4) {
            transformed = stutter(transformed, random);
        }

        return matchCase(word, transformed);
    }

    private static String fixEnding(String word) {
        if (word.endsWith("er") && word.length() > 3) {
            return word.substring(0, word.length() - 2) + "ew";
        }
        if (word.endsWith("ar") && word.length() > 3) {
            return word.substring(0, word.length() - 2) + "aw";
        }
        return word;
    }

    private static String stutter(String word, Random random) {
        if (word.isEmpty()) {
            return word;
        }
        char first = word.charAt(0);
        if (!Character.isLetter(first)) {
            return word;
        }
        if ("aeiou".indexOf(Character.toLowerCase(first)) >= 0) {
            return word;
        }

        int repeat = 1 + random.nextInt(2);
        StringBuilder sb = new StringBuilder(word.length() + 4);
        for (int i = 0; i < repeat; i++) {
            sb.append(first).append('-');
        }
        sb.append(word);
        return sb.toString();
    }

    private static String addEmbellishments(String input, Random random, UwuConfig cfg) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }

        StringBuilder out = new StringBuilder(input.length() + 32);
        Matcher matcher = Pattern.compile("([^.!?]*[.!?]?)(\\s*)").matcher(input);
        while (matcher.find()) {
            String segment = matcher.group(1);
            String gap = matcher.group(2);
            if (segment == null) {
                continue;
            }

            out.append(segment);
            if (!TextUtils.isEmpty(segment.trim())) {
                if (random.nextInt(100) < cfg.facePct) {
                    out.append(' ').append(pick(FACES, random));
                }
                if (random.nextInt(100) < cfg.actionPct) {
                    out.append(' ').append(pick(ACTIONS, random));
                }
                if (random.nextInt(100) < cfg.exclaimPct) {
                    out.append(' ').append(pick(EXCLAMATIONS, random));
                }
            }
            out.append(gap);
        }

        String result = out.toString().trim();
        if (result.isEmpty()) {
            result = input;
        }
        return result;
    }

    private static boolean isWord(String token) {
        return WORD_PATTERN.matcher(token).matches();
    }

    private static String pick(List<String> items, Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private static long seed(String input, UwuConfig cfg) {
        long s = input.hashCode();
        s = s * 31L + cfg.delayMs;
        s = s * 31L + cfg.stutterPct;
        s = s * 31L + cfg.facePct;
        s = s * 31L + cfg.actionPct;
        s = s * 31L + cfg.exclaimPct;
        return s;
    }

    private static String matchCase(String original, String transformed) {
        if (original.equals(original.toUpperCase(Locale.ROOT))) {
            return transformed.toUpperCase(Locale.ROOT);
        }

        if (isTitleCase(original)) {
            if (transformed.isEmpty()) {
                return transformed;
            }
            return Character.toUpperCase(transformed.charAt(0)) + transformed.substring(1);
        }

        return transformed;
    }

    private static boolean isTitleCase(String word) {
        if (word.isEmpty()) {
            return false;
        }
        if (!Character.isUpperCase(word.charAt(0))) {
            return false;
        }
        for (int i = 1; i < word.length(); i++) {
            if (Character.isUpperCase(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
