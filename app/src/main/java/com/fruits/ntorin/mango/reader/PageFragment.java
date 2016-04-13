package com.fruits.ntorin.mango.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fruits.ntorin.mango.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static com.fruits.ntorin.mango.BitmapFunctions.getBitmapFromURL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "http://mangafox.me/manga/virgin_love/v01/c002/1.html";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private Bitmap bitmap;


    public PageFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param href Page image URL
     * @return A new instance of fragment PageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PageFragment newInstance(String href) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putString("href", href);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        String href = getArguments().getString("href");
        new AsyncFetchPage().execute(href);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("pagefragment", "PageFragment destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pagefragment", "PageFragment paused");
    }


    @Override
    public void onStart() {
        super.onStart();
        if(bitmap == null){
            //bitmap = mBitmap;
        }
        Log.d("pagefragment", "PageFragment started");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(bitmap == null){
            //bitmap = mBitmap;
        }
        Log.d("pagefragment", "PageFragment view destroyed");
    }

    private class AsyncFetchPage extends AsyncTask<String, Void, Void>{

        String href;
        Bitmap bitmap;
        PageFragment pageFragment;

        public AsyncFetchPage(){
            super();
            //this.href = href;
            //this.pageFragment = pageFragment;
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d("pagefragment", "doInBackground");
            Document document = null;
            try {
                Log.d("checkurl", params[0]);
                document = Jsoup.connect(params[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Element li = document.getElementById("image");
            String bmpURL = li.attr("src");
            mBitmap = getBitmapFromURL(bmpURL);
            //setImage(bitmap);
            Log.d("pagefragment", "doInBackground done");
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("pagefragment", "onPostExecute");
            setImage(mBitmap);
            Log.d("onCreateView resetimg", "setting image" + mBitmap);
            bitmap = mBitmap;
            Log.d("pagefragment", "onPostExecute finished");
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        mImageView = (ImageView) view.findViewById(R.id.reader_img);
        //if(bitmap != null){
            Log.d("onCreateView resetimg", "setting image" + mBitmap);
            setImage(mBitmap);
        //}else{
            //new AsyncFetchPage().execute(ARG_PARAM1);
        //}
        Log.d("pagefragment", "view created");
        return view;
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

    public void setImage(Bitmap bitmap){
        mImageView.setImageBitmap(bitmap);
        mImageView.postInvalidate();
        bitmap = mBitmap;
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
