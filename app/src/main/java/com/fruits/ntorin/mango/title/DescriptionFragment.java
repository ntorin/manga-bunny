package com.fruits.ntorin.mango.title;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fruits.ntorin.mango.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DescriptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DescriptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private int mParam1;

    private OnFragmentInteractionListener mListener;
    private Bitmap mTitleCover;
    private View mView;

    public DescriptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param section_number Parameter 1.
     * @return A new instance of fragment DescriptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DescriptionFragment newInstance(int section_number) {
        DescriptionFragment fragment = new DescriptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section_number);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_description_chapters, container, false);

        TextView descriptionText = (TextView) rootView.findViewById(R.id.description_title_description);
        TextView infoText = (TextView) rootView.findViewById(R.id.description_title_info);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
        descriptionText.setTypeface(tf);
        TextView author = (TextView) rootView.findViewById(R.id.description_title_author);
        author.setTypeface(tf);

        TextView artist = (TextView) rootView.findViewById(R.id.description_title_artist);
        artist.setTypeface(tf);

        TextView genres = (TextView) rootView.findViewById(R.id.description_title_genres);
        genres.setTypeface(tf);

        TextView status = (TextView) rootView.findViewById(R.id.description_title_status);
        status.setTypeface(tf);

        TextView rank = (TextView) rootView.findViewById(R.id.description_title_rank);
        rank.setTypeface(tf);

        mView = rootView;

        //textView.setMovementMethod(new ScrollingMovementMethod());
        //ScrollView scrollView = (ScrollView) rootView.findViewById(fragment)
        //TextView textView = (TextView) rootView.findViewById(R.id.description_title_description);
        return rootView;
    }

    public void showView(boolean b) {
        LinearLayout descriptionview = (LinearLayout) mView.findViewById(R.id.description_view);

        if (b) {
            descriptionview.setVisibility(View.VISIBLE);
        } else {
            descriptionview.setVisibility(View.INVISIBLE);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setText(String text, TextView textView){
        if(textView != null) {
            textView.setText(text); // TODO: 7/21/2016 maybe has errors
        }
    }


    public void setCover(Bitmap cover){
        if(getView() != null) {
            ImageView imageView = (ImageView) this.getView().findViewById(R.id.description_title_cover);
            imageView.setImageBitmap(cover);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
