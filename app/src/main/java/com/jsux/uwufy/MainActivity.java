package com.jsux.uwufy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private SharedPreferences prefs;

    private Switch enabledSwitch;
    private EditText delayEdit;
    private EditText minLengthEdit;
    private EditText allowedPackagesEdit;
    private CheckBox preserveUrls;
    private CheckBox preserveEmails;
    private CheckBox preservePasswords;
    private CheckBox preserveAcronyms;
    private LabelSeekBar stutterBar;
    private LabelSeekBar faceBar;
    private LabelSeekBar actionBar;
    private LabelSeekBar exclaimBar;
    private EditText previewInput;
    private TextView previewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(24));
        scrollView.addView(root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f);
        title.setPadding(0, 0, 0, dp(6));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("LSPosed module for app processes. Scope it to the apps you type in, not just the keyboard, because the app is where text actually lands. Delightfully inconvenient, but that is how Android works.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        subtitle.setPadding(0, 0, 0, dp(16));
        root.addView(subtitle);

        TextView note = new TextView(this);
        note.setText("Idle delay means the text is only uwu-fied after it has stopped changing for a bit. That avoids the usual chaos of live typing.");
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        note.setPadding(0, 0, 0, dp(16));
        root.addView(note);

        enabledSwitch = new Switch(this);
        enabledSwitch.setText("Enabled");
        root.addView(enabledSwitch);

        delayEdit = createNumberField("Idle delay in ms");
        minLengthEdit = createNumberField("Minimum length");
        allowedPackagesEdit = createTextField("Allowed package list, comma or space separated");

        root.addView(wrapWithLabel("Idle delay before uwuifying", delayEdit));
        root.addView(wrapWithLabel("Minimum text length", minLengthEdit));
        root.addView(wrapWithLabel("Allowed apps", allowedPackagesEdit));

        stutterBar = createSeekBar("Stutter chance", Prefs.DEF_STUTTER_PCT);
        faceBar = createSeekBar("Face chance", Prefs.DEF_FACE_PCT);
        actionBar = createSeekBar("Action chance", Prefs.DEF_ACTION_PCT);
        exclaimBar = createSeekBar("Exclamation chance", Prefs.DEF_EXCLAIM_PCT);

        root.addView(stutterBar);
        root.addView(faceBar);
        root.addView(actionBar);
        root.addView(exclaimBar);

        preserveUrls = createCheckBox("Preserve URLs");
        preserveEmails = createCheckBox("Preserve emails");
        preservePasswords = createCheckBox("Preserve password fields");
        preserveAcronyms = createCheckBox("Preserve acronyms / ALL CAPS");

        root.addView(preserveUrls);
        root.addView(preserveEmails);
        root.addView(preservePasswords);
        root.addView(preserveAcronyms);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(14), 0, dp(14));

        Button saveButton = new Button(this);
        saveButton.setText("Save");

        Button resetButton = new Button(this);
        resetButton.setText("Reset");

        LinearLayout.LayoutParams weight = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        buttons.addView(saveButton, weight);
        buttons.addView(space(dp(10)), new LinearLayout.LayoutParams(dp(10), 1));
        buttons.addView(resetButton, weight);
        root.addView(buttons);

        TextView previewLabel = new TextView(this);
        previewLabel.setText("Preview");
        previewLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        previewLabel.setPadding(0, dp(8), 0, dp(8));
        root.addView(previewLabel);

        previewInput = createTextField("Preview input");
        previewInput.setSingleLine(false);
        previewInput.setMinLines(3);
        previewInput.setText("Hello friend, this is really good. Please do not turn my passwords into cute nonsense.");
        root.addView(previewInput);

        previewOutput = new TextView(this);
        previewOutput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        previewOutput.setPadding(dp(12), dp(12), dp(12), dp(12));
        previewOutput.setBackgroundColor(0x11000000);
        previewOutput.setGravity(Gravity.START);
        root.addView(previewOutput);

        bindDefaults();
        loadIntoUi();
        refreshPreview();

        saveButton.setOnClickListener(v -> {
            saveFromUi();
            refreshPreview();
            toast("Saved");
        });

        resetButton.setOnClickListener(v -> {
            resetDefaults();
            loadIntoUi();
            refreshPreview();
            toast("Reset");
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        delayEdit.addTextChangedListener(watcher);
        minLengthEdit.addTextChangedListener(watcher);
        allowedPackagesEdit.addTextChangedListener(watcher);
        previewInput.addTextChangedListener(watcher);

        setContentView(scrollView);
    }

    private void bindDefaults() {
        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains(Prefs.KEY_ENABLED)) {
            editor.putBoolean(Prefs.KEY_ENABLED, Prefs.DEF_ENABLED);
        }
        if (!prefs.contains(Prefs.KEY_DELAY_MS)) {
            editor.putInt(Prefs.KEY_DELAY_MS, Prefs.DEF_DELAY_MS);
        }
        if (!prefs.contains(Prefs.KEY_MIN_LENGTH)) {
            editor.putInt(Prefs.KEY_MIN_LENGTH, Prefs.DEF_MIN_LENGTH);
        }
        if (!prefs.contains(Prefs.KEY_ALLOWED_PACKAGES)) {
            editor.putString(Prefs.KEY_ALLOWED_PACKAGES, Prefs.DEF_ALLOWED_PACKAGES);
        }
        if (!prefs.contains(Prefs.KEY_STUTTER_PCT)) {
            editor.putInt(Prefs.KEY_STUTTER_PCT, Prefs.DEF_STUTTER_PCT);
        }
        if (!prefs.contains(Prefs.KEY_FACE_PCT)) {
            editor.putInt(Prefs.KEY_FACE_PCT, Prefs.DEF_FACE_PCT);
        }
        if (!prefs.contains(Prefs.KEY_ACTION_PCT)) {
            editor.putInt(Prefs.KEY_ACTION_PCT, Prefs.DEF_ACTION_PCT);
        }
        if (!prefs.contains(Prefs.KEY_EXCLAIM_PCT)) {
            editor.putInt(Prefs.KEY_EXCLAIM_PCT, Prefs.DEF_EXCLAIM_PCT);
        }
        if (!prefs.contains(Prefs.KEY_PRESERVE_URLS)) {
            editor.putBoolean(Prefs.KEY_PRESERVE_URLS, Prefs.DEF_PRESERVE_URLS);
        }
        if (!prefs.contains(Prefs.KEY_PRESERVE_EMAILS)) {
            editor.putBoolean(Prefs.KEY_PRESERVE_EMAILS, Prefs.DEF_PRESERVE_EMAILS);
        }
        if (!prefs.contains(Prefs.KEY_PRESERVE_PASSWORDS)) {
            editor.putBoolean(Prefs.KEY_PRESERVE_PASSWORDS, Prefs.DEF_PRESERVE_PASSWORDS);
        }
        if (!prefs.contains(Prefs.KEY_PRESERVE_ACRONYMS)) {
            editor.putBoolean(Prefs.KEY_PRESERVE_ACRONYMS, Prefs.DEF_PRESERVE_ACRONYMS);
        }
        editor.apply();
    }

    private void loadIntoUi() {
        UwuConfig cfg = UwuConfig.from(prefs);
        enabledSwitch.setChecked(cfg.enabled);
        delayEdit.setText(String.valueOf(cfg.delayMs));
        minLengthEdit.setText(String.valueOf(cfg.minLength));
        allowedPackagesEdit.setText(cfg.allowedPackages);
        stutterBar.setValue(cfg.stutterPct);
        faceBar.setValue(cfg.facePct);
        actionBar.setValue(cfg.actionPct);
        exclaimBar.setValue(cfg.exclaimPct);
        preserveUrls.setChecked(cfg.preserveUrls);
        preserveEmails.setChecked(cfg.preserveEmails);
        preservePasswords.setChecked(cfg.preservePasswords);
        preserveAcronyms.setChecked(cfg.preserveAcronyms);
    }

    private void saveFromUi() {
        UwuConfig cfg = readUiConfig();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Prefs.KEY_ENABLED, cfg.enabled);
        editor.putInt(Prefs.KEY_DELAY_MS, cfg.delayMs);
        editor.putInt(Prefs.KEY_MIN_LENGTH, cfg.minLength);
        editor.putString(Prefs.KEY_ALLOWED_PACKAGES, cfg.allowedPackages);
        editor.putInt(Prefs.KEY_STUTTER_PCT, cfg.stutterPct);
        editor.putInt(Prefs.KEY_FACE_PCT, cfg.facePct);
        editor.putInt(Prefs.KEY_ACTION_PCT, cfg.actionPct);
        editor.putInt(Prefs.KEY_EXCLAIM_PCT, cfg.exclaimPct);
        editor.putBoolean(Prefs.KEY_PRESERVE_URLS, cfg.preserveUrls);
        editor.putBoolean(Prefs.KEY_PRESERVE_EMAILS, cfg.preserveEmails);
        editor.putBoolean(Prefs.KEY_PRESERVE_PASSWORDS, cfg.preservePasswords);
        editor.putBoolean(Prefs.KEY_PRESERVE_ACRONYMS, cfg.preserveAcronyms);
        editor.apply();
    }

    private void resetDefaults() {
        prefs.edit().clear().apply();
        bindDefaults();
    }

    private UwuConfig readUiConfig() {
        return new UwuConfig(
                enabledSwitch.isChecked(),
                parseInt(delayEdit.getText().toString(), Prefs.DEF_DELAY_MS),
                parseInt(minLengthEdit.getText().toString(), Prefs.DEF_MIN_LENGTH),
                allowedPackagesEdit.getText().toString().trim(),
                stutterBar.getValue(),
                faceBar.getValue(),
                actionBar.getValue(),
                exclaimBar.getValue(),
                preserveUrls.isChecked(),
                preserveEmails.isChecked(),
                preservePasswords.isChecked(),
                preserveAcronyms.isChecked()
        );
    }

    private void refreshPreview() {
        UwuConfig cfg = readUiConfig();
        String input = previewInput.getText() == null ? "" : previewInput.getText().toString();
        previewOutput.setText(UwuTransformer.uwuify(input, cfg));
    }

    private EditText createTextField(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setPadding(dp(12), dp(10), dp(12), dp(10));
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return editText;
    }

    private EditText createNumberField(String hint) {
        EditText editText = createTextField(hint);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        return editText;
    }

    private CheckBox createCheckBox(String text) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        return checkBox;
    }

    private LabelSeekBar createSeekBar(String label, int defaultValue) {
        return new LabelSeekBar(label, defaultValue);
    }

    private View wrapWithLabel(String label, View field) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(8), 0, dp(8));

        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setPadding(0, 0, 0, dp(6));
        box.addView(textView);
        box.addView(field);
        return box;
    }

    private View space(int width) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        return view;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class LabelSeekBar extends LinearLayout {
        private final TextView labelView;
        private final SeekBar seekBar;
        private final String baseLabel;

        LabelSeekBar(String label, int defaultValue) {
            super(MainActivity.this);
            baseLabel = label;
            setOrientation(VERTICAL);
            setPadding(0, dp(8), 0, dp(8));

            labelView = new TextView(MainActivity.this);
            seekBar = new SeekBar(MainActivity.this);
            seekBar.setMax(100);
            seekBar.setProgress(defaultValue);

            labelView.setText(label + ": " + defaultValue + "%");
            addView(labelView);
            addView(seekBar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                    labelView.setText(baseLabel + ": " + progress + "%");
                    refreshPreview();
                }

                @Override
                public void onStartTrackingTouch(SeekBar bar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar bar) {
                }
            });
        }

        void setValue(int value) {
            seekBar.setProgress(value);
            labelView.setText(baseLabel + ": " + value + "%");
        }

        int getValue() {
            return seekBar.getProgress();
        }
    }
}
