package com.fruits.ntorin.mango.home;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.sourcefns.Sources;

public class SettingsDialogFragment extends DialogFragment {

    SettingsDialogListener mListener;
    RadioButton sortBy = null;
    RadioButton pickSource = null;
    RadioButton completion = null;
    String[] genreList;
    String[] genreselected;
    CheckBox[] genres;
    private int sortByID;
    private int pickSourceID;
    public SettingsDialogFragment() {
        // Required empty public constructor
    }

    public RadioButton getCompletion() {
        return completion;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.fragment_settings_dialog, null);

        final RadioGroup sortByGroup = (RadioGroup) v.findViewById(R.id.sortByGroup);
        sortByGroup.check(R.id.sortByDefault);
        sortBy = (RadioButton) v.findViewById(sortByGroup.getCheckedRadioButtonId());

        final RadioGroup pickSourceGroup = (RadioGroup) v.findViewById(R.id.pickSourceGroup);
        final RadioGroup pickSourceGroup2 = (RadioGroup) v.findViewById(R.id.pickSourceGroup2);
        switch (Sources.getSelectedSource()){
            case Sources.MANGAHERE:
                pickSourceGroup.check(R.id.sources_mangahere);
                break;
            case Sources.MANGAFOX:
                pickSourceGroup2.check(R.id.sources_mangafox);
                break;
            case Sources.MANGAREADER:
                pickSourceGroup.check(R.id.sources_mangareader);
                break;
            case Sources.MANGAPANDA:
                pickSourceGroup2.check(R.id.sources_mangapanda);
                break;
            case Sources.GOODMANGA:
                pickSourceGroup.check(R.id.sources_goodmanga);
                break;
            case Sources.MANGAINN:
                pickSourceGroup2.check(R.id.sources_mangainn);
        }
        //pickSourceGroup.check(R.id.pickSourceDefault);
        pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());
        if(pickSource == null){
            pickSource = (RadioButton) v.findViewById(pickSourceGroup2.getCheckedRadioButtonId());
        }

        final int[] genreListID = {getResources().getIdentifier(pickSource.getText().toString(), "array",
                getActivity().getBaseContext().getPackageName())};
        //Log.d("SettingsDialogFragment", pickSource.getText().toString() + " id " + genreListID[0]);
        genreList = getResources().getStringArray(genreListID[0]);
        String genresel = getArguments().getString("genreselected");
        if (genresel != null) {
            genreselected = genresel.split(" ");
        }

        final RadioGroup completionGroup = (RadioGroup) v.findViewById(R.id.completion_group);
        completionGroup.check(R.id.completion_default);
        completion = (RadioButton) v.findViewById(completionGroup.getCheckedRadioButtonId());

        completionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                completion = (RadioButton) v.findViewById(completionGroup.getCheckedRadioButtonId());
            }
        });

        ////Log.d("genreList", "" + genreList[0] + " " + genreList[1]);

        builder.setView(v);
        sortByGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        sortBy = (RadioButton) v.findViewById(sortByGroup.getCheckedRadioButtonId());
                        //mListener.onItemClick(SettingsDialogFragment.this);
                    }
                }
        );

        final boolean[] isChecking = {true};
        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.genreList);
        pickSourceGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId != -1 && isChecking[0]) {
                            isChecking[0] = false;
                            pickSourceGroup2.clearCheck();
                            //mCheckedId = checkedId;
                        }
                        isChecking[0] = true;

                        pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());

                        if (pickSource != null) {
                            String sourcename = pickSource.getText().toString();
                            genreListID[0] = getResources().getIdentifier(sourcename, "array",
                                    getActivity().getBaseContext().getPackageName());
                            //Log.d("pickSourceGroup", "text: " + pickSource.getText() + " identifier: " + genreListID[0]);
                            RadioGroup sortByGroup = (RadioGroup) v.findViewById(R.id.sortByGroup);
                            if (sourcename.equalsIgnoreCase("MangaReader")) {
                                Sources.setSelectedSource(Sources.MANGAREADER);
                                sortByGroup.setVisibility(View.INVISIBLE);
                            } else if (sourcename.equalsIgnoreCase("MangaHere")) {
                                Sources.setSelectedSource(Sources.MANGAHERE);
                                sortByGroup.setVisibility(View.VISIBLE);
                            } else if (sourcename.equalsIgnoreCase("GoodManga")) {
                                Sources.setSelectedSource(Sources.GOODMANGA);
                                sortByGroup.setVisibility(View.VISIBLE);
                            }
                            genreList = getResources().getStringArray(genreListID[0]);

                            //mListener.onItemClick(SettingsDialogFragment.this);
                            initiateGenreList(linearLayout);
                        }
                    }
                }
        );
        pickSourceGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1 && isChecking[0]) {
                    isChecking[0] = false;
                    pickSourceGroup.clearCheck();
                    //mCheckedId = checkedId;
                }
                isChecking[0] = true;

                pickSource = (RadioButton) v.findViewById(pickSourceGroup2.getCheckedRadioButtonId());

                if (pickSource != null) {
                    String sourcename = pickSource.getText().toString();
                    genreListID[0] = getResources().getIdentifier(sourcename, "array",
                            getActivity().getBaseContext().getPackageName());
                    //Log.d("pickSourceGroup", "text: " + pickSource.getText() + " identifier: " + genreListID[0]);
                    RadioGroup sortByGroup = (RadioGroup) v.findViewById(R.id.sortByGroup);
                    if (sourcename.equalsIgnoreCase("MangaFox")) {
                        Sources.setSelectedSource(Sources.MANGAFOX);
                        sortByGroup.setVisibility(View.VISIBLE);
                    } else if (sourcename.equalsIgnoreCase("MangaPanda")) {
                        Sources.setSelectedSource(Sources.MANGAPANDA);
                        sortByGroup.setVisibility(View.INVISIBLE);
                    } else if (sourcename.equalsIgnoreCase("MangaInn")) {
                        Sources.setSelectedSource(Sources.MANGAINN);
                        sortByGroup.setVisibility(View.VISIBLE);
                    }
                    genreList = getResources().getStringArray(genreListID[0]);

                    //mListener.onItemClick(SettingsDialogFragment.this);
                    initiateGenreList(linearLayout);
                }else{
                    //Log.d("nullpick", "picksource is null");
                }

            }
        });

        initiateGenreList(linearLayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickSource = (RadioButton) v.findViewById(pickSourceGroup.getCheckedRadioButtonId());
                if(pickSource == null){
                    pickSource = (RadioButton) v.findViewById(pickSourceGroup2.getCheckedRadioButtonId());
                }
                genreListID[0] = getResources().getIdentifier(pickSource.getText().toString(), "array",
                        getActivity().getBaseContext().getPackageName());
                //Log.d("pickSourceGroup", "text: " + pickSource.getText() + " identifier: " + genreListID[0]);
                genreList = getResources().getStringArray(genreListID[0]);
                mListener.onItemClick(SettingsDialogFragment.this);
                initiateGenreList(linearLayout);
            }
        });

        return builder.create();
    }

    private void initiateGenreList(LinearLayout linearLayout) {
        //Log.d("initiateGenreList", "starting");
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
            if (genreselected != null) {
                for (String s : genreselected) {
                    if (checkBox.getText().toString().equals(s)) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
            }
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mListener.onItemClick(SettingsDialogFragment.this);
                }
            });

            switch (i % 2) {
                case 0:
                    left.addView(checkBox);
                    break;
                case 1:
                    right.addView(checkBox);
                    break;
                //case 2:
                //    right.addView(checkBox);
            }
        }
        //Log.d("initiateGenreList", "completed");
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
        void onItemClick(SettingsDialogFragment dialog);
    }
}
