package com.jsux.uwufy;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.os.SystemClock;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class UwuFyModule implements IXposedHookLoadPackage {
    private static final String SELF_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final Object LOCK = new Object();
    private static final WeakHashMap<TextView, PendingState> STATES = new WeakHashMap<>();
    private static final long CONFIG_CACHE_MS = 750L;

    private static volatile boolean hooksInstalled;
    private static volatile long lastConfigLoadAt;
    private static volatile UwuConfig cachedConfig = UwuConfig.defaults();
    private static XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || TextUtils.isEmpty(lpparam.packageName)) {
            return;
        }

        if (shouldSkipPackage(lpparam.packageName)) {
            return;
        }

        synchronized (LOCK) {
            if (hooksInstalled) {
                return;
            }
            hooksInstalled = true;
        }

        try {
            XposedHelpers.findAndHookMethod(
                    TextView.class,
                    "onTextChanged",
                    CharSequence.class,
                    int.class,
                    int.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (!(param.thisObject instanceof TextView)) {
                                return;
                            }

                            TextView view = (TextView) param.thisObject;
                            UwuConfig cfg = loadConfig();
                            String packageName = currentPackageName(view);
                            if (!shouldProcess(view, cfg, packageName)) {
                                return;
                            }

                            CharSequence currentText = view.getText();
                            if (TextUtils.isEmpty(currentText)) {
                                return;
                            }

                            PendingState state = getState(view);
                            String current = currentText.toString();

                            if (state.applying) {
                                state.applying = false;
                                state.lastApplied = current;
                                state.pendingSource = current;
                                return;
                            }

                            if (TextUtils.equals(current, state.lastApplied) || TextUtils.equals(current, state.pendingSource)) {
                                return;
                            }

                            state.pendingSource = current;
                            state.generation++;
                            final int generation = state.generation;
                            final String capturedPackage = packageName;
                            final WeakStateRunnable task = new WeakStateRunnable(view, capturedPackage, generation);

                            if (state.task != null) {
                                view.removeCallbacks(state.task);
                            }

                            state.task = task;
                            view.postDelayed(task, Math.max(0L, cfg.delayMs));
                        }
                    }
            );
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static String currentPackageName(TextView view) {
        try {
            Context context = view.getContext();
            if (context != null) {
                String pkg = context.getPackageName();
                if (!TextUtils.isEmpty(pkg)) {
                    return pkg;
                }
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private static PendingState getState(TextView view) {
        synchronized (STATES) {
            PendingState state = STATES.get(view);
            if (state == null) {
                state = new PendingState();
                STATES.put(view, state);
            }
            return state;
        }
    }

    private static boolean shouldSkipPackage(String packageName) {
        return SELF_PACKAGE.equals(packageName)
                || "org.lsposed.manager".equals(packageName)
                || "com.topjohnwu.magisk".equals(packageName)
                || "com.android.systemui".equals(packageName);
    }

    private static UwuConfig loadConfig() {
        long now = SystemClock.uptimeMillis();
        UwuConfig current = cachedConfig;
        if (current != null && now - lastConfigLoadAt < CONFIG_CACHE_MS) {
            return current;
        }

        synchronized (LOCK) {
            now = SystemClock.uptimeMillis();
            current = cachedConfig;
            if (current != null && now - lastConfigLoadAt < CONFIG_CACHE_MS) {
                return current;
            }

            XSharedPreferences currentPrefs = getPrefs();
            UwuConfig cfg = UwuConfig.defaults();
            if (currentPrefs != null) {
                try {
                    currentPrefs.reload();
                    cfg = UwuConfig.from(currentPrefs);
                } catch (Throwable ignored) {
                    cfg = UwuConfig.defaults();
                }
            }
            cachedConfig = cfg;
            lastConfigLoadAt = now;
            return cfg;
        }
    }

    private static XSharedPreferences getPrefs() {
        synchronized (LOCK) {
            if (prefs == null) {
                prefs = new XSharedPreferences(SELF_PACKAGE, Prefs.NAME);
            }
            return prefs;
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
            int variation = view.getInputType() & InputType.TYPE_MASK_VARIATION;
            return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                    || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                    || variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean packageAllowed(String packageName, String allowedPackages) {
        if (TextUtils.isEmpty(allowedPackages)) {
            return true;
        }

        String[] entries = allowedPackages.split("[,\n\r\t ]+");
        for (String raw : entries) {
            String item = raw == null ? "" : raw.trim();
            if (item.isEmpty()) {
                continue;
            }
            if ("*".equals(item) || packageName.equals(item)) {
                return true;
            }
            if (item.endsWith("*")) {
                String prefix = item.substring(0, item.length() - 1);
                if (packageName.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void applyText(TextView view, String transformed) {
        if (view == null || TextUtils.isEmpty(transformed)) {
            return;
        }

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

    private static final class PendingState {
        String pendingSource = "";
        String lastApplied = "";
        int generation;
        boolean applying;
        WeakStateRunnable task;
    }

    private static final class WeakStateRunnable implements Runnable {
        private final WeakReference<TextView> viewRef;
        private final String packageName;
        private final int generation;

        WeakStateRunnable(TextView view, String packageName, int generation) {
            this.viewRef = new WeakReference<>(view);
            this.packageName = packageName;
            this.generation = generation;
        }

        @Override
        public void run() {
            TextView view = viewRef.get();
            if (view == null) {
                return;
            }

            UwuConfig cfg = loadConfig();
            if (!shouldProcess(view, cfg, packageName)) {
                return;
            }

            PendingState state = getState(view);
            if (state.generation != generation || state.applying) {
                return;
            }

            CharSequence now = view.getText();
            if (now == null) {
                return;
            }

            String source = now.toString();
            if (!TextUtils.equals(source, state.pendingSource)) {
                return;
            }

            String transformed = UwuTransformer.uwuify(source, cfg);
            if (TextUtils.isEmpty(transformed) || TextUtils.equals(transformed, source)) {
                state.lastApplied = source;
                return;
            }

            state.applying = true;
            state.lastApplied = transformed;
            applyText(view, transformed);
        }
    }
}
