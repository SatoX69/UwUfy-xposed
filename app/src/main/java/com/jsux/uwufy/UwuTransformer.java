package com.jsux.uwufy;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UwuTransformer {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\p{L}+(?:['’\\-]\\p{L}+)?|\\d+|\\s+|[^\\p{L}\\d\\s]+");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(?:https?://\\S+|www\\.\\S+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
    private static final Pattern CODE_SPAN_PATTERN = Pattern.compile("`[^`]+`");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("\\{[^}]+\\}|\\[[^\\]]+\\]|<[^>]+>");
    private static final Pattern ACRONYM_PATTERN = Pattern.compile("^[A-Z0-9]{2,}$");
    private static final Pattern ALREADY_CUTE_PATTERN = Pattern.compile("(?i).*(uwu|owo|hewwo|fwiend|pwease|wuv|nya|>w<|:3|rawr|nuzzle|mew|nyaa).*");

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

    private static final Map<String, String> DICT = new LinkedHashMap<>();

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
        DICT.put("very", "vewy");
        DICT.put("this", "dis");
        DICT.put("that", "dat");
        DICT.put("there", "dere");
        DICT.put("their", "dair");
        DICT.put("really", "weawwy");
        DICT.put("good", "gud");
        DICT.put("great", "gweat");
        DICT.put("nice", "nyice");
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
        DICT.put("my", "ma");
        DICT.put("mine", "mewn");
        DICT.put("because", "becuz");
        DICT.put("what", "wut");
        DICT.put("why", "wy");
        DICT.put("okay", "okie");
        DICT.put("maybe", "mayb");
        DICT.put("little", "widdle");
        DICT.put("sure", "suwe");
        DICT.put("just", "juss");
        DICT.put("sorry", "sowwy");
        DICT.put("think", "tink");
        DICT.put("going", "goin");
        DICT.put("right", "wight");
        DICT.put("more", "mowe");
        DICT.put("really", "weawwy");
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
                continue;
            }
            if (isWord(token)) {
                out.append(transformWord(token, cfg, random, wordIndex++));
                continue;
            }
            out.append(transformPunctuation(token, cfg, random));
        }

        String result = out.toString();
        result = maybeDecorateText(result, cfg, random);
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
        out = protectWithPattern(out, CODE_SPAN_PATTERN, placeholders);
        return protectWithPattern(out, INLINE_CODE_PATTERN, placeholders);
    }

    private static String protectWithPattern(String input, Pattern pattern, List<String> placeholders) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String placeholder = makePlaceholder(placeholders.size());
            placeholders.add(matcher.group());
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(placeholder));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String makePlaceholder(int index) {
        return "UWUSAFE" + encodeIndex(index) + "UWU";
    }

    private static String encodeIndex(int index) {
        if (index < 0) {
            return "a";
        }
        StringBuilder out = new StringBuilder();
        int value = index;
        do {
            out.append((char) ('a' + (value % 26)));
            value /= 26;
        } while (value > 0);
        return out.reverse().toString();
    }

    private static int decodeIndex(String value) {
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        int out = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if (c < 'a' || c > 'z') {
                return -1;
            }
            out = out * 26 + (c - 'a');
        }
        return out;
    }

    private static boolean isPlaceholder(String token) {
        return !TextUtils.isEmpty(token) && token.startsWith("UWUSAFE") && token.endsWith("UWU");
    }

    private static String restorePlaceholder(String token, List<String> placeholders) {
        if (!isPlaceholder(token)) {
            return token;
        }
        String middle = token.substring("UWUSAFE".length(), token.length() - 3);
        int index = decodeIndex(middle);
        if (index >= 0 && index < placeholders.size()) {
            return placeholders.get(index);
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

        String lower = word.toLowerCase(Locale.ROOT);
        if (ALREADY_CUTE_PATTERN.matcher(lower).matches()) {
            return word;
        }

        String transformed = DICT.containsKey(lower) ? DICT.get(lower) : applyCoreRules(lower);
        transformed = maybeStutter(transformed, random, cfg, wordIndex);
        transformed = maybeRepeatVowels(transformed, random);
        transformed = maybeAddWordDecoration(transformed, random, cfg, wordIndex);
        return matchCase(word, transformed);
    }

    private static String transformPunctuation(String token, UwuConfig cfg, Random random) {
        if (TextUtils.isEmpty(token)) {
            return token;
        }

        if (isSentenceEnd(token) && cfg.exclaimPct > 0 && random.nextInt(100) < cfg.exclaimPct) {
            return token + pick(EXCLAMATIONS, random);
        }
        return token;
    }

    private static boolean isSentenceEnd(String token) {
        return ".".equals(token) || "!".equals(token) || "?".equals(token) || "…".equals(token);
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
        out = softenCommonEndings(out);
        return out;
    }

    private static String softenCommonEndings(String word) {
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
        if (wordIndex % 3 != 0) {
            return word;
        }
        if (random.nextInt(100) >= cfg.stutterPct) {
            return word;
        }

        char first = word.charAt(0);
        if (!Character.isLetter(first)) {
            return word;
        }
        if (word.length() > 1 && Character.toLowerCase(first) == Character.toLowerCase(word.charAt(1))) {
            return word;
        }

        return random.nextBoolean() ? first + "-" + word : first + "... " + word;
    }

    private static String maybeRepeatVowels(String word, Random random) {
        if (TextUtils.isEmpty(word) || word.length() < 5 || random.nextInt(6) != 0) {
            return word;
        }
        String out = word.replace("oo", "uwu").replace("ee", "eee");
        out = out.replace("ai", "ay").replace("ou", "ow");
        return out;
    }

    private static String maybeAddWordDecoration(String word, Random random, UwuConfig cfg, int wordIndex) {
        String out = word;
        if (cfg.facePct > 0 && wordIndex % 2 == 0 && random.nextInt(100) < cfg.facePct) {
            out = appendDecoration(out, pick(FACES, random));
        }
        if (cfg.actionPct > 0 && wordIndex % 4 == 0 && random.nextInt(100) < cfg.actionPct) {
            out = appendDecoration(out, pick(ACTIONS, random));
        }
        return out;
    }

    private static String maybeDecorateText(String text, UwuConfig cfg, Random random) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        String out = text;
        boolean addFace = cfg.facePct > 0 && random.nextInt(100) < cfg.facePct;
        boolean addAction = cfg.actionPct > 0 && random.nextInt(100) < cfg.actionPct;
        boolean addExclaim = cfg.exclaimPct > 0 && random.nextInt(100) < cfg.exclaimPct;

        if (addFace) {
            out = appendDecoration(out, pick(FACES, random));
        }
        if (addAction) {
            out = appendDecoration(out, pick(ACTIONS, random));
        }
        if (addExclaim) {
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
        seed = 31L * seed + (cfg.preserveUrls ? 1 : 0);
        seed = 31L * seed + (cfg.preserveEmails ? 1 : 0);
        seed = 31L * seed + (cfg.preservePasswords ? 1 : 0);
        seed = 31L * seed + (cfg.preserveAcronyms ? 1 : 0);
        return seed;
    }

    private static String pick(List<String> items, Random random) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.get(random.nextInt(items.size()));
    }
}
