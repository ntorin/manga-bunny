package com.fruits.ntorin.mango.home;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fruits.ntorin.mango.R;

public class SettingsDialogFragment extends DialogFragment {

    SettingsDialogListener mListener;
    RadioButton sortBy = null;
    RadioButton pickSource = null;
    String[] genreList;
    CheckBox[] genres;
    private int sortByID;
    private int pickSourceID;
    public SettingsDialogFragment() {
        // Required empty public constructor
    }

    public RadioButton getSortBy() {
        return sortBy;
    }

    public RadioButton getPickSource() {
        return pickSource;
    }

    public CheckBox[] getGenres() {
        return genres;
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

        final int[] genreListID = {getResources().getIdentifier(pickSource.getText().toString(), "array",
                getActivity().getBaseContext().getPackageName())};
        Log.d("SettingsDialogFragment", pickSource.getText().toString() + " id " + genreListID[0]);
        genreList = getResources().getStringArray(genreListID[0]);
        Log.d("genreList", "" + genreList[0] + " " + genreList[1]);

        //Log.d("SettingsDialogFragment", "" + getView());
        builder.setView(v);
        //RadioButton r1 = (RadioButton) v.findViewById(R.id.radioTest);
        sortByGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        sortBy = (RadioButton) v.findViewById(sortByGroup.getCheckedRadioButtonId());
                        mListener.onItemClick(SettingsDialogFragment.this);
                    }
                }
        );

        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.genreList);
        pickSourceGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());
                        genreListID[0] = getResources().getIdentifier(pickSource.getText().toString(), "array",
                                getActivity().getBaseContext().getPackageName());
                        Log.d("pickSourceGroup", "text: " + pickSource.getText() + " identifier: " + genreListID[0]);
                        genreList = getResources().getStringArray(genreListID[0]);
                        mListener.onItemClick(SettingsDialogFragment.this);
                        initiateGenreList(linearLayout);
                    }
                }
        );

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
        LinearLayout left = (LinearLayout) linearLayout.findViewById(R.id.genreListLeft);
        LinearLayout center = (LinearLayout) linearLayout.findViewById(R.id.genreListCenter);
        LinearLayout right = (LinearLayout) linearLayout.findViewById(R.id.genreListRight);
        left.removeAllViews();
        //left.remove
        center.removeAllViews();
        right.removeAllViews();

        genres = new CheckBox[genreList.length];

        for (int i = 0; i < genreList.length; i++) {
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

            switch(i % 3){
                case 0:
                    left.addView(checkBox);
                    break;
                case 1:
                    center.addView(checkBox);
                    break;
                case 2:
                    right.addView(checkBox);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (SettingsDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsDialogListener");
        }
    }

    public interface SettingsDialogListener {
        public void onItemClick(SettingsDialogFragment dialog);
    }
}
