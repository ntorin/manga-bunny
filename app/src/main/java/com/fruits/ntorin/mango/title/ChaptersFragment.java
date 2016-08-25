package com.fruits.ntorin.mango.title;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fruits.ntorin.mango.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChaptersFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TITLE = "title";
    public int maploc = -1;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ListView mListView;
    private LayoutInflater mInflater;
    private View mView;
    private int mFirstVisibleItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChaptersFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChaptersFragment newInstance(int columnCount, String title) {
        ChaptersFragment fragment = new ChaptersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, 1);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ////Log.d("mcol", "" + mColumnCount);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        ////Log.d("mcol", "" + mColumnCount);
    }

    public void setMaploc(int maploc) {
        this.maploc = maploc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapters_list, container, false);
        mInflater = inflater;
        mView = view;

        TextView t = (TextView) view.findViewById(R.id.error_text);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
        t.setTypeface(tf);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("onItemLongClick", "clicked");
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                //Log.d("onItemLongClick", "" + mListView.getChoiceMode());
                return false;
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mFirstVisibleItem = firstVisibleItem;
            }
        });
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                //Log.d("onStateChange", "clicked");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //Log.d("onCreateActionMode", "clicked");
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                //Log.d("onPrepareActionMode", "clicked");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                //Log.d("onActionItemClicked", "clicked");
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //Log.d("onDestroyActionMode", "clicked");

            }
        });
        return view;
    }

    public void showErrorView(boolean b) {
        RelativeLayout errorLayout = (RelativeLayout) mView.findViewById(R.id.error_layout);
        if (b) {
            errorLayout.setVisibility(View.VISIBLE);
        } else {
            errorLayout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setAdapter(Map<String, Chapter> map) {
        final Chapter[] chapters = new Chapter[map.size()];
        int i = 0;
        for (String s : map.keySet()) {
            chapters[i++] = map.get(String.valueOf(i));
        }
        if(chapters != null && getContext() != null) {
            mListView.setAdapter(new ChaptersListAdapter(getContext(), R.layout.site_item, chapters));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Chapter chapter = (Chapter) mListView.getAdapter().getItem(position);
                    TextView newtext = (TextView) view.findViewById(R.id.text);
                    //newtext.setTextColor(Color.rgb(66, 139, 166));
                    maploc = position;
                    mListView.setAdapter(new ChaptersListAdapter(getContext(), R.layout.site_item, chapters));
                    mListView.setSelection(mFirstVisibleItem);
                    mListener.onListFragmentInteraction(chapter);
                }
            });
        }
        //mListView.setAdapter(new MyChaptersRecyclerViewAdapter(map, mListener));
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Chapter item);
    }

    class ChaptersListAdapter extends ArrayAdapter<Chapter> {

        public ChaptersListAdapter(Context context, int resource, Chapter[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (maploc == -1) {
                //Log.d("ChaptersListAdapter", getContext().getFilesDir().getPath());
                File f = new File(getContext().getFilesDir().getPath() + "/" + getArguments().getString(ARG_TITLE) + "checkpoint");
                try {
                    FileInputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    int[] checkpoint = (int[]) ois.readObject();

                    //maploc = checkpoint[0];
                    maploc = getCount() - checkpoint[0];

                    ////Log.d("ReadFromCheckpoint", ch.content);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (OptionalDataException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            View v = convertView;
            v = mInflater.inflate(R.layout.chlist_item, parent, false);
            TextView textView = (TextView) v.findViewById(R.id.text);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            textView.setTypeface(tf);
            textView.setText(getItem(position).id);
            if (position == maploc) {
                textView.setTextColor(Color.rgb(66, 139, 166));
            }
            return v;
        }
    }
}
