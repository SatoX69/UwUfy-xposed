package com.jsux.uwufy;

import android.text.TextUtils;
import android.widget.EditText;
import android.text.method.PasswordTransformationMethod;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class UwuFyModule implements IXposedHookLoadPackage {
    private static final String SELF_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final Set<String> DEFAULT_SKIP_PACKAGES = new HashSet<>();
    private static final Object LOCK = new Object();
    private static final WeakHashMap<TextView, PendingState> STATES = new WeakHashMap<>();
    private static boolean hooksInstalled;
    private static XSharedPreferences prefs;

    static {
        DEFAULT_SKIP_PACKAGES.add(SELF_PACKAGE);
        DEFAULT_SKIP_PACKAGES.add("org.lsposed.manager");
        DEFAULT_SKIP_PACKAGES.add("com.topjohnwu.magisk");
        DEFAULT_SKIP_PACKAGES.add("com.android.systemui");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || TextUtils.isEmpty(lpparam.packageName)) {
            return;
        }

        if (DEFAULT_SKIP_PACKAGES.contains(lpparam.packageName)) {
            return;
        }

        synchronized (LOCK) {
            if (hooksInstalled) {
                return;
            }
            hooksInstalled = true;
        }

        try {
            XposedHelpers.findAndHookMethod("android.widget.TextView", null, "onTextChanged", CharSequence.class, int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object target = param.thisObject;
                    if (!(target instanceof TextView)) {
                        return;
                    }

                    TextView view = (TextView) target;
                    UwuConfig config = loadConfig();
                    if (!shouldProcess(view, config, lpparam.packageName)) {
                        return;
                    }

                    PendingState state;
                    synchronized (STATES) {
                        state = STATES.get(view);
                        if (state == null) {
                            state = new PendingState();
                            STATES.put(view, state);
                        }
                    }

                    if (state.applying) {
                        state.applying = false;
                        return;
                    }

                    if (param.args == null || param.args.length == 0 || !(param.args[0] instanceof CharSequence)) {
                        return;
                    }

                    String current = param.args[0].toString();
                    if (TextUtils.isEmpty(current)) {
                        return;
                    }

                    state.latestText = current;
                    state.generation++;
                    final int generation = state.generation;

                    if (state.task != null) {
                        view.removeCallbacks(state.task);
                    }

                    state.task = new Runnable() {
                        @Override
                        public void run() {
                            UwuConfig freshConfig = loadConfig();
                            if (!shouldProcess(view, freshConfig, lpparam.packageName)) {
                                return;
                            }

                            PendingState currentState;
                            synchronized (STATES) {
                                currentState = STATES.get(view);
                            }

                            if (currentState == null || currentState.generation != generation || currentState.applying) {
                                return;
                            }

                            CharSequence now = view.getText();
                            if (now == null) {
                                return;
                            }

                            String source = now.toString();
                            if (!TextUtils.equals(source, currentState.latestText)) {
                                return;
                            }

                            String transformed = UwuTransformer.uwuify(source, freshConfig);
                            if (TextUtils.isEmpty(transformed) || TextUtils.equals(transformed, source)) {
                                return;
                            }

                            currentState.applying = true;
                            applyText(view, transformed);
                        }
                    };

                    long delay = Math.max(200L, config.delayMs);
                    view.postDelayed(state.task, delay);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static void applyText(TextView view, String transformed) {
        try {
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                editText.setText(transformed);
                int end = transformed.length();
                if (end >= 0) {
                    editText.setSelection(end);
                }
            } else {
                view.setText(transformed);
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean shouldProcess(TextView view, UwuConfig config, String packageName) {
        if (view == null || config == null || !config.enabled) {
            return false;
        }

        if (!(view instanceof EditText)) {
            return false;
        }

        if (view.getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }

        if (config.preservePasswords && isPasswordInput(view)) {
            return false;
        }

        if (!packageAllowed(packageName, config.allowedPackages)) {
            return false;
        }

        CharSequence text = view.getText();
        return text != null && text.length() >= config.minLength;
    }

    private static boolean isPasswordInput(TextView view) {
        try {
            int type = view.getInputType();
            int variation = type & android.text.InputType.TYPE_MASK_VARIATION;
            if (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                return true;
            }
            if (variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                return true;
            }
            if (variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
                return true;
            }
            if (variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean packageAllowed(String packageName, String allowedPackages) {
        if (TextUtils.isEmpty(allowedPackages)) {
            return true;
        }

        String[] entries = allowedPackages.split(",");
        for (String raw : entries) {
            String item = raw.trim();
            if (!item.isEmpty() && packageName.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private static UwuConfig loadConfig() {
        XSharedPreferences pref = getPrefs();
        if (pref != null) {
            try {
                pref.reload();
                return UwuConfig.from(pref);
            } catch (Throwable ignored) {
            }
        }
        return UwuConfig.defaults();
    }

    private static XSharedPreferences getPrefs() {
        synchronized (LOCK) {
            if (prefs == null) {
                prefs = new XSharedPreferences(SELF_PACKAGE, Prefs.NAME);
            }
            return prefs.getFile().canRead() ? prefs : null;
        }
    }

    private static final class PendingState {
        String latestText;
        int generation;
        boolean applying;
        Runnable task;
    }
}
