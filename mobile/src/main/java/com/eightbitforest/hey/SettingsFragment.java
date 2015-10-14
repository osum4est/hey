package com.eightbitforest.hey;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        ArrayList<String> lights = getArguments().getStringArrayList("lights");
        ArrayList<String> ids = getArguments().getStringArrayList("ids");
        refresh(lights, ids);
    }

    public void refresh(ArrayList<String> lights, ArrayList<String> ids) {
        final PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if (lights.size() < 1) {
            Preference errorPreference = new Preference(screen.getContext());
            errorPreference.setTitle(getString(R.string.error_no_lights_title));
            errorPreference.setSummary(getString(R.string.error_no_lights_summary));
            screen.addPreference(errorPreference);
        } else {

            // GENERAL
            PreferenceCategory categoryGeneral = new PreferenceCategory(screen.getContext());
            categoryGeneral.setTitle(getString(R.string.category_general));
            screen.addPreference(categoryGeneral);

            SwitchPreference switchSound = new SwitchPreference(screen.getContext());
            switchSound.setTitle(getString(R.string.sound_title));
            switchSound.setSummaryOff(getString(R.string.sound_summary_off));
            switchSound.setSummaryOn(getString(R.string.sound_summary_on));
            switchSound.setDefaultValue(true);
            switchSound.setKey("sound");
            categoryGeneral.addPreference(switchSound);


            // LIGHTS
            PreferenceCategory categoryLights = new PreferenceCategory(screen.getContext());
            categoryLights.setTitle(getString(R.string.category_lights));
            screen.addPreference(categoryLights);
            for (int i = 0; i < lights.size(); i++) {
                SwitchPreference switchPreference = new SwitchPreference(screen.getContext());
                switchPreference.setTitle(lights.get(i));
                switchPreference.setSummaryOff(getText(R.string.lights_summary_off));
                switchPreference.setSummaryOn(getText(R.string.lights_summary_on));
                switchPreference.setDefaultValue(true);
                String id = ids.get(i);
                if (id != null) {
                    switchPreference.setKey(id);
                }
                categoryLights.addPreference(switchPreference);
            }


            // TESTING

            PreferenceCategory categoryTest = new PreferenceCategory(screen.getContext());
            categoryTest.setTitle(getString(R.string.category_testing));
            screen.addPreference(categoryTest);

            Preference preferenceTestOff = new Preference(screen.getContext());
            preferenceTestOff.setTitle(getString(R.string.test_off_title));
            preferenceTestOff.setSummary(getString(R.string.test_off_summary));
            preferenceTestOff.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    screen.getContext().startService(new Intent(screen.getContext(), LightsOffService.class));
                    return true;
                }
            });
            categoryTest.addPreference(preferenceTestOff);

            Preference preferenceTestOn = new Preference(screen.getContext());
            preferenceTestOn.setTitle(getString(R.string.test_on_title));
            preferenceTestOn.setSummary(getString(R.string.test_on_summary));
            preferenceTestOn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("on", true);
                    Intent i = new Intent(screen.getContext(), LightsOffService.class);
                    i.putExtras(bundle);
                    screen.getContext().startService(i);
                    return true;
                }
            });
            categoryTest.addPreference(preferenceTestOn);
        }
    }
}
