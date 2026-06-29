package com.jsux.uwufy;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UwuTransformer {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?|\\d+|\\s+|[^\\w\\s]+");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(?:https?://\\S+|www\\.\\S+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
    private static final Pattern CODE_SPAN_PATTERN = Pattern.compile("`[^`]+`");
    private static final Pattern ACRONYM_PATTERN = Pattern.compile("^[A-Z0-9]{2,}$");
    private static final Pattern ALREADY_CUTE_PATTERN = Pattern.compile("(?i).*(uwu|owo|hewwo|wuv|fwiend|nya|pwease|thw|>w<|:3|rawr|nuzzle).*");

    private static final List<String> FACES = Arrays.asList(
            "(uwu)",
            "(owo)",
            "(◕‿◕)",
            "(^･ω･^)",
            "(｡•̀ᴗ-)✧",
            ":3",
            ">w<",
            "(づ｡◕‿‿◕｡)づ",
            "(≧◡≦)",
            "(｡♥‿♥｡)"
    );

    private static final List<String> ACTIONS = Arrays.asList(
            "*blushes*",
            "*giggles*",
            "*nuzzles*",
            "*hides face*",
            "*wiggles happily*",
            "*boops*",
            "*taps paws together*",
            "*sways side to side*",
            "*flops down*"
    );

    private static final List<String> EXCLAMATIONS = Arrays.asList(
            "!!",
            "!!!",
            "~",
            "~!!",
            "!!?",
            "owo!",
            "nya!"
    );

    private static final Map<String, String> DICT = new HashMap<>();

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("hello", "hewwo");
        map.put("hi", "hai");
        map.put("hey", "hewwo");
        map.put("love", "wuv");
        map.put("you", "yuw");
        map.put("your", "yuwr");
        map.put("yours", "yuwrs");
        map.put("friend", "fwiend");
        map.put("friends", "fwiends");
        map.put("cute", "kewte");
        map.put("small", "smol");
        map.put("very", "vewy");
        map.put("this", "dis");
        map.put("that", "dat");
        map.put("there", "dere");
        map.put("their", "dair");
        map.put("really", "weawwy");
        map.put("good", "gud");
        map.put("great", "gweat");
        map.put("nice", "nyice");
        map.put("no", "nyo");
        map.put("yes", "yis");
        map.put("thanks", "thwanks");
        map.put("thank", "thwank");
        map.put("please", "pwease");
        map.put("stop", "stowp");
        map.put("for", "fow");
        map.put("are", "awe");
        map.put("the", "da");
        map.put("them", "dem");
        map.put("my", "ma");
        map.put("mine", "mewn");
        map.put("because", "becuz");
        map.put("what", "wut");
        map.put("why", "wy");
        map.put("okay", "okie");
        map.put("maybe", "mayb");
        map.put("little", "widdle");
        map.put("sure", "suwe");
        map.put("just", "juss");
        map.put("sorry", "sowwy");
        map.put("think", "tink");
        map.put("going", "goin");
        map.put("right", "wight");
        map.put("more", "mowe");
        map.put("really", "weawwy");
        DICT.putAll(map);
    }

    private UwuTransformer() {
    }

    public static String uwuify(String input, UwuConfig cfg) {
        if (TextUtils.isEmpty(input) || cfg == null || !cfg.enabled || input.length() < cfg.minLength) {
            return input;
        }

        Random random = new Random(seed(input, cfg));
        ArrayList<String> placeholders = new ArrayList<>();
        String protectedInput = protectSegments(input, cfg, placeholders);

        StringBuilder out = new StringBuilder(protectedInput.length() + 64);
        Matcher matcher = TOKEN_PATTERN.matcher(protectedInput);
        int wordIndex = 0;
        while (matcher.find()) {
            String token = matcher.group();
            if (token == null) {
                continue;
            }
            if (isPlaceholder(token)) {
                out.append(restorePlaceholder(token, placeholders));
            } else if (isWord(token)) {
                out.append(transformWord(token, cfg, random, wordIndex++));
            } else {
                out.append(token);
            }
        }

        String result = out.toString();
        result = maybeDecorateSentences(result, cfg, random);
        result = normalizePunctuation(result);
        result = fixSpacing(result);
        return result;
    }

    private static String protectSegments(String input, UwuConfig cfg, List<String> placeholders) {
        String out = input;
        if (cfg.preserveUrls) {
            out = protectWithPattern(out, URL_PATTERN, placeholders);
        }
        if (cfg.preserveEmails) {
            out = protectWithPattern(out, EMAIL_PATTERN, placeholders);
        }
        return protectWithPattern(out, CODE_SPAN_PATTERN, placeholders);
    }

    private static String protectWithPattern(String input, Pattern pattern, List<String> placeholders) {
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

    private static boolean isPlaceholder(String token) {
        return token.startsWith("__UWU_SAFE_") && token.endsWith("__");
    }

    private static String restorePlaceholder(String token, List<String> placeholders) {
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

    private static boolean isWord(String token) {
        return !TextUtils.isEmpty(token) && Character.isLetter(token.charAt(0));
    }

    private static String transformWord(String word, UwuConfig cfg, Random random, int wordIndex) {
        if (TextUtils.isEmpty(word)) {
            return word;
        }

        if (cfg.preserveAcronyms && ACRONYM_PATTERN.matcher(word).matches()) {
            return word;
        }

        if (ALREADY_CUTE_PATTERN.matcher(word).matches()) {
            return word;
        }

        String lower = word.toLowerCase(Locale.ROOT);
        String transformed = DICT.get(lower);
        if (transformed == null) {
            transformed = applyCoreRules(lower);
        }

        transformed = maybeStutter(transformed, random, cfg, wordIndex);
        transformed = maybeRepeatVowel(transformed, random);
        transformed = maybeAddSuffix(transformed, random, cfg);
        return matchCase(word, transformed);
    }

    private static String applyCoreRules(String lower) {
        String out = lower;
        out = out.replace("th", "d");
        out = out.replace("ove", "uv");
        out = out.replace("ar", "aw");
        out = out.replace("er", "ew");
        out = out.replace("or", "ow");
        out = out.replace("l", "w");
        out = out.replace("r", "w");
        out = out.replace("na", "nya");
        out = out.replace("ne", "nye");
        out = out.replace("ni", "nyi");
        out = out.replace("no", "nyo");
        out = out.replace("nu", "nyu");
        out = out.replace("tion", "shun");
        out = out.replace("ing", "in");
        return fixEndings(out);
    }

    private static String fixEndings(String word) {
        String out = word;
        if (out.endsWith("ing") && out.length() > 4) {
            out = out.substring(0, out.length() - 3) + "in";
        }
        if (out.endsWith("er") && out.length() > 3) {
            out = out.substring(0, out.length() - 2) + "ew";
        }
        if (out.endsWith("re") && out.length() > 3) {
            out = out.substring(0, out.length() - 2) + "we";
        }
        if (out.endsWith("ly") && out.length() > 3) {
            out = out.substring(0, out.length() - 2) + "wy";
        }
        return out;
    }

    private static String maybeStutter(String word, Random random, UwuConfig cfg, int wordIndex) {
        if (TextUtils.isEmpty(word) || word.length() < 4 || cfg.stutterPct <= 0) {
            return word;
        }
        if (random.nextInt(100) >= cfg.stutterPct) {
            return word;
        }
        if (wordIndex % 2 != 0) {
            return word;
        }

        char first = word.charAt(0);
        if (!Character.isLetter(first)) {
            return word;
        }
        if (word.length() > 1 && Character.toLowerCase(first) == Character.toLowerCase(word.charAt(1))) {
            return word;
        }

        if (random.nextBoolean()) {
            return first + "-" + word;
        }
        return first + "... " + word;
    }

    private static String maybeRepeatVowel(String word, Random random) {
        if (TextUtils.isEmpty(word) || word.length() < 5 || random.nextInt(5) != 0) {
            return word;
        }
        return word.replace("oo", "uwu").replace("ee", "eee");
    }

    private static String maybeAddSuffix(String word, Random random, UwuConfig cfg) {
        String out = word;
        if (cfg.facePct > 0 && random.nextInt(100) < cfg.facePct) {
            out = appendDecoration(out, pick(FACES, random));
        }
        if (cfg.actionPct > 0 && random.nextInt(100) < cfg.actionPct) {
            out = appendDecoration(out, pick(ACTIONS, random));
        }
        if (cfg.exclaimPct > 0 && random.nextInt(100) < cfg.exclaimPct) {
            out = appendDecoration(out, pick(EXCLAMATIONS, random));
        }
        return out;
    }

    private static String maybeDecorateSentences(String text, UwuConfig cfg, Random random) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        String out = text;
        if (cfg.facePct > 0 && random.nextInt(100) < cfg.facePct) {
            out = appendDecoration(out, pick(FACES, random));
        }
        if (cfg.actionPct > 0 && random.nextInt(100) < cfg.actionPct) {
            out = appendDecoration(out, pick(ACTIONS, random));
        }
        if (cfg.exclaimPct > 0 && random.nextInt(100) < cfg.exclaimPct) {
            out = appendDecoration(out, pick(EXCLAMATIONS, random));
        }
        return out;
    }

    private static String appendDecoration(String text, String decoration) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(decoration)) {
            return text;
        }
        String trimmed = text.trim();
        if (trimmed.endsWith(decoration)) {
            return text;
        }
        if (Character.isWhitespace(text.charAt(text.length() - 1))) {
            return text + decoration;
        }
        return text + " " + decoration;
    }

    private static String normalizePunctuation(String input) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }
        String out = input.replaceAll("(?<=\\p{L})'(?=\\p{L})", "'");
        out = out.replaceAll("\\s+([,.;:!?])", "$1");
        out = out.replaceAll("([!?.,]){3,}", "$1$1");
        out = out.replaceAll("([!?])\\1{1,}", "$1$1");
        return out;
    }

    private static String fixSpacing(String input) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }
        String out = input.replaceAll("\\s{2,}", " ");
        out = out.replaceAll("\\s+([)\\]\\}]+)", "$1");
        return out.trim();
    }

    private static String matchCase(String original, String transformed) {
        if (TextUtils.isEmpty(original) || TextUtils.isEmpty(transformed)) {
            return transformed;
        }

        if (original.equals(original.toUpperCase(Locale.ROOT))) {
            return transformed.toUpperCase(Locale.ROOT);
        }

        if (Character.isUpperCase(original.charAt(0))) {
            return Character.toUpperCase(transformed.charAt(0)) + transformed.substring(1);
        }

        return transformed;
    }

    private static long seed(String input, UwuConfig cfg) {
        long seed = 0x6d2b79f5L;
        seed = 31L * seed + input.hashCode();
        seed = 31L * seed + cfg.delayMs;
        seed = 31L * seed + cfg.minLength;
        seed = 31L * seed + cfg.stutterPct;
        seed = 31L * seed + cfg.facePct;
        seed = 31L * seed + cfg.actionPct;
        seed = 31L * seed + cfg.exclaimPct;
        seed = 31L * seed + cfg.allowedPackages.hashCode();
        return seed;
    }

    private static String pick(List<String> items, Random random) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.get(random.nextInt(items.size()));
    }
}
