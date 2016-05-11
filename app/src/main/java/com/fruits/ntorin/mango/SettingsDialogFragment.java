package com.fruits.ntorin.mango;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class SettingsDialogFragment extends DialogFragment {

    private int sortByID;
    private int pickSourceID;

    public interface SettingsDialogListener {
        public void onItemClick(SettingsDialogFragment dialog);
    }

    SettingsDialogListener mListener;

    public RadioButton getSortBy() {
        return sortBy;
    }

    public RadioButton getPickSource() {
        return pickSource;
    }

    RadioButton sortBy = null;
    RadioButton pickSource = null;
    String[] genreList;

    public CheckBox[] getGenres() {
        return genres;
    }

    CheckBox[] genres;

    public SettingsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //setStyle(R.style.AppTheme, R.style.AppTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.fragment_settings_dialog, null);

        final RadioGroup sortByGroup = (RadioGroup) v.findViewById(R.id.sortByGroup);
        sortByGroup.check(R.id.sortByDefault);
        sortBy = (RadioButton) v.findViewById(sortByGroup.getCheckedRadioButtonId());
        final RadioGroup pickSourceGroup = (RadioGroup) v.findViewById(R.id.pickSourceGroup);
        pickSourceGroup.check(R.id.pickSourceDefault);
        pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());
        final int[] genreListID = {getResources().getIdentifier(pickSource.getText().toString(), "array", getActivity().getBaseContext().getPackageName())};
        Log.d("SettingsDialogFragment", pickSource.getText().toString() + " id " + genreListID[0]);
        genreList = getResources().getStringArray(genreListID[0]);
        Log.d("genreList", "" + genreList[0] + " " + genreList[1]);

                //Log.d("SettingsDialogFragment", "" + getView());
        builder.setView(v);
        RadioButton r1 = (RadioButton) v.findViewById(R.id.radioTest);
        sortByGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                         sortBy = (RadioButton) v.findViewById(sortByGroup.getCheckedRadioButtonId());
                        mListener.onItemClick(SettingsDialogFragment.this);
                    }
                }
        );
        pickSourceGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());
                        genreListID[0] = getResources().getIdentifier(pickSource.getText().toString(), null, null);
                        genreList = getResources().getStringArray(genreListID[0]);
                        mListener.onItemClick(SettingsDialogFragment.this);
                    }
                }
        );

        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.genreList);
        initiateGenreList(linearLayout);

        /*String[] sortBy = {"a", "b", "c"};
        String[] filterGenre = {"x", "y", "z"};
        builder.setTitle(R.string.settings_dialog_message);
        builder.setSingleChoiceItems(sortBy, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onItemClick(SettingsDialogFragment.this);
                    }
                });
        builder.setMultiChoiceItems(filterGenre, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        mListener.onItemClick(SettingsDialogFragment.this);
                    }
                });*/
        return builder.create();
    }

    private void initiateGenreList(LinearLayout linearLayout) {

        linearLayout.removeAllViews();
        genres = new CheckBox[genreList.length];

        for(int i = 0; i < genreList.length; i++){
            CheckBox checkBox =
            new CheckBox(getActivity().getBaseContext());
            genres[i] = checkBox;
            checkBox.setText(genreList[i]);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(SettingsDialogFragment.this);
                }
            });

            linearLayout.addView(checkBox);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            mListener = (SettingsDialogListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsDialogListener");
        }
    }
}
