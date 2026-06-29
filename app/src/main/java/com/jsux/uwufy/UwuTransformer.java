package com.jsux.uwufy;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UwuTransformer {
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?|\d+|\s+|[^\w\s]+");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(?:https?://\S+|www\.\S+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}");
    private static final Pattern ACRONYM_PATTERN = Pattern.compile("^[A-Z0-9]{2,}$");
    private static final Pattern ALREADY_CUTE_PATTERN = Pattern.compile("(?i).*(uwu|owo|hewwo|wuv|fwiend|nya|pwease|thw|>w<|:3).*");

    private static final List<String> FACES = Arrays.asList(
            "(uwu)",
            "(owo)",
            "(◕‿◕)",
            "(^･ω･^)",
            "(｡•̀ᴗ-)✧",
            ":3",
            ">w<",
            "(づ｡◕‿‿◕｡)づ"
    );

    private static final List<String> ACTIONS = Arrays.asList(
            "*blushes*",
            "*giggles*",
            "*nuzzles*",
            "*hides face*",
            "*wiggles happily*",
            "*boops*",
            "*taps paws together*"
    );

    private static final List<String> EXCLAMATIONS = Arrays.asList(
            "!!",
            "!!!",
            "~",
            "~!!",
            "!!?",
            "owo!"
    );

    private static final Map<String, String> DICT = new HashMap<>();

    static {
        DICT.put("hello", "hewwo");
        DICT.put("hi", "hai");
        DICT.put("hey", "hewwo");
        DICT.put("love", "wuv");
        DICT.put("you", "yuw");
        DICT.put("your", "yuwr");
        DICT.put("yours", "yuwrs");
        DICT.put("friend", "fwiend");
        DICT.put("friends", "fwiends");
        DICT.put("cute", "kewte");
        DICT.put("small", "smol");
        DICT.put("very", "bery");
        DICT.put("this", "dis");
        DICT.put("that", "dat");
        DICT.put("there", "dere");
        DICT.put("really", "weawwy");
        DICT.put("good", "gud");
        DICT.put("great", "gweat");
        DICT.put("no", "nyo");
        DICT.put("yes", "yis");
        DICT.put("thanks", "thwanks");
        DICT.put("thank", "thwank");
        DICT.put("please", "pwease");
        DICT.put("stop", "stowp");
        DICT.put("for", "fow");
        DICT.put("are", "awe");
        DICT.put("the", "da");
        DICT.put("them", "dem");
        DICT.put("themself", "demsewf");
        DICT.put("my", "ma");
        DICT.put("mine", "mewn");
        DICT.put("because", "becuz");
        DICT.put("what", "wut");
        DICT.put("why", "wy");
        DICT.put("okay", "okie");
        DICT.put("okay?", "okie?");
        DICT.put("maybe", "mayb");
        DICT.put("little", "widdle");
        DICT.put("really", "weawwy");
        DICT.put("sure", "suwe");
        DICT.put("just", "juss");
        DICT.put("please", "pwease");
    }

    private UwuTransformer() {
    }

    public static String uwuify(String input, UwuConfig cfg) {
        if (TextUtils.isEmpty(input) || cfg == null || !cfg.enabled || input.length() < cfg.minLength) {
            return input;
        }

        ArrayList<String> placeholders = new ArrayList<>();
        String protectedInput = protectSegments(input, cfg, placeholders);
        Random random = new Random(seed(input, cfg));
        StringBuilder wordsFirst = new StringBuilder(protectedInput.length() + 64);

        Matcher matcher = TOKEN_PATTERN.matcher(protectedInput);
        int wordIndex = 0;
        while (matcher.find()) {
            String token = matcher.group();
            if (token == null) {
                continue;
            }

            if (isPlaceholder(token)) {
                wordsFirst.append(restorePlaceholder(token, placeholders));
                continue;
            }

            if (isWord(token)) {
                wordsFirst.append(transformWord(token, cfg, random, wordIndex++));
            } else {
                wordsFirst.append(token);
            }
        }

        String result = decorateSentences(wordsFirst.toString(), cfg, random);
        result = normalizePunctuation(result);
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
        return out;
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

    private static String transformWord(String word, UwuConfig cfg, Random random, int wordIndex) {
        String lower = word.toLowerCase(Locale.ROOT);

        if (cfg.preserveAcronyms && ACRONYM_PATTERN.matcher(word).matches()) {
            return word;
        }

        if (ALREADY_CUTE_PATTERN.matcher(word).matches()) {
            return word;
        }

        String transformed = DICT.get(lower);
        if (transformed == null) {
            transformed = applyCoreRules(lower);
        }

        if (shouldStutter(transformed, random, cfg, wordIndex)) {
            transformed = stutter(transformed, random);
        }

        transformed = maybeAddSuffix(transformed, random);
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
        return fixEndings(out);
    }

    private static String fixEndings(String word) {
        String out = word;
        if (out.endsWith("ing") && out.length() > 4) {
            out = out.substring(0, out.length() - 3) + "in'";
        }
        if (out.endsWith("er") && out.length() > 3) {
            out = out.substring(0, out.length() - 2) + "ew";
        }
        if (out.endsWith("ar") && out.length() > 3) {
            out = out.substring(0, out.length() - 2) + "aw";
        }
        if (out.endsWith("ow") && out.length() > 3) {
            out = out + "~";
        }
        return out;
    }

    private static boolean shouldStutter(String transformed, Random random, UwuConfig cfg, int wordIndex) {
        if (cfg.stutterPct <= 0) {
            return false;
        }
        if (transformed == null || transformed.length() < 4) {
            return false;
        }
        if (wordIndex == 0) {
            return random.nextInt(100) < Math.min(cfg.stutterPct / 2 + 1, cfg.stutterPct);
        }
        return random.nextInt(100) < cfg.stutterPct;
    }

    private static String stutter(String word, Random random) {
        if (TextUtils.isEmpty(word)) {
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
        StringBuilder out = new StringBuilder(word.length() + 4);
        for (int i = 0; i < repeat; i++) {
            out.append(first).append('-');
        }
        out.append(word);
        return out.toString();
    }

    private static String maybeAddSuffix(String word, Random random) {
        if (TextUtils.isEmpty(word)) {
            return word;
        }
        if (word.length() <= 3) {
            return word;
        }
        if (random.nextInt(100) < 7) {
            return word + randomSuffix(word, random);
        }
        return word;
    }

    private static String randomSuffix(String word, Random random) {
        switch (random.nextInt(6)) {
            case 0:
                return "~";
            case 1:
                return " >w<";
            case 2:
                return " owo";
            case 3:
                return " uwu";
            case 4:
                return " :3";
            default:
                return "!!";
        }
    }

    private static String decorateSentences(String input, UwuConfig cfg, Random random) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }

        Matcher matcher = Pattern.compile("([^.!?]*[.!?]?)(\s*)").matcher(input);
        StringBuilder out = new StringBuilder(input.length() + 64);
        while (matcher.find()) {
            String sentence = matcher.group(1);
            String gap = matcher.group(2);

            if (!TextUtils.isEmpty(sentence)) {
                out.append(sentence);

                if (containsSentenceEnd(sentence)) {
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
        }
        return out.toString().trim();
    }

    private static boolean containsSentenceEnd(String sentence) {
        return sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("?");
    }

    private static String normalizePunctuation(String input) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }
        return input
                .replace("?!?", "?!")
                .replace("!!?", "!!")
                .replace("..", ".")
                .replace("!!!", "!!")
                .replace("??", "?");
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
        s = s * 31L + cfg.minLength;
        s = s * 31L + cfg.stutterPct;
        s = s * 31L + cfg.facePct;
        s = s * 31L + cfg.actionPct;
        s = s * 31L + cfg.exclaimPct;
        s = s * 31L + (cfg.preserveUrls ? 1 : 0);
        s = s * 31L + (cfg.preserveEmails ? 1 : 0);
        s = s * 31L + (cfg.preservePasswords ? 1 : 0);
        s = s * 31L + (cfg.preserveAcronyms ? 1 : 0);
        return s;
    }

    private static String matchCase(String original, String transformed) {
        if (TextUtils.isEmpty(original) || TextUtils.isEmpty(transformed)) {
            return transformed;
        }
        if (original.equals(original.toUpperCase(Locale.ROOT))) {
            return transformed.toUpperCase(Locale.ROOT);
        }
        if (isTitleCase(original)) {
            return Character.toUpperCase(transformed.charAt(0)) + transformed.substring(1);
        }
        return transformed;
    }

    private static boolean isTitleCase(String word) {
        if (TextUtils.isEmpty(word)) {
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
