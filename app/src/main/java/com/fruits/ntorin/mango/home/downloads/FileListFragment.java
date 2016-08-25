package com.fruits.ntorin.mango.home.downloads;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fruits.ntorin.mango.utils.AlphanumComparator;
import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.reader.ChapterReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;


public class FileListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "dir";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mListView;
    private File[] chlist;
    private int pgno;
    private LayoutInflater mInflater;

    public FileListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FileListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileListFragment newInstance(String dir, String title) {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, dir);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("FileListFragment", getArguments().getString(ARG_PARAM1));
        File dir = new File(getArguments().getString("dir"));
        File[] chapters = dir.listFiles();
        if(chapters != null) {
            Arrays.sort(chapters, new AlphanumComparator());
            /* Arrays.sort(chapters, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    String[] lhsa = lhs.getName().split(" ");
                    String lhsunparsed = lhsa[lhsa.length - 1];
                    double lhsparsed = 0;
                    if (lhsunparsed.split(".").length <= 2) {
                        for (String s : lhsunparsed.split(".")) {
                            if (StringUtil.isNumeric(s)) {
                                continue;
                            }
                            return lhs.getName().compareTo(rhs.getName());
                        }
                        if(StringUtil.isNumeric(lhsunparsed)) {
                            lhsparsed = Double.parseDouble(lhsunparsed);
                        }else{
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    }

                    String[] rhsa = rhs.getName().split(" ");
                    String rhsunparsed = rhsa[rhsa.length - 1];
                    double rhsparsed = 0;
                    if (rhsunparsed.split(".").length <= 2) {
                        for (String s : rhsunparsed.split(".")) {
                            if (StringUtil.isNumeric(s)) {
                                continue;
                            }
                            return lhs.getName().compareTo(rhs.getName());
                        }
                        if(StringUtil.isNumeric(rhsunparsed)){
                            rhsparsed = Double.parseDouble(rhsunparsed);
                        }else{
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    }
                    int diff = 0;
                    double diffdouble = (lhsparsed - rhsparsed);
                    if (diffdouble < 0) {
                        diff = 1;
                    } else if (diffdouble > 0) {
                        diff = -1;
                    }

                    return diff;
                }

            }); */
            chlist = chapters;
            if (chapters != null) {
                for (File fi : chapters) {
                    //Log.d("FileListFragment", fi.getName());
                    File[] pages = fi.listFiles();
                    for (File f : pages) {
                        //Log.d("FileListFragment", f.getName());
                    }
                }
            }
        }else{
            ShowBadURIDialog();
        }
    }

    private void ShowBadURIDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                .setMessage("The selected folder could not be found; it has either been " +
                        "moved or deleted.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mInflater = inflater;
        View v = inflater.inflate(R.layout.fragment_file_list, container, false);
        FloatingActionButton b = (FloatingActionButton) v.findViewById(R.id.button);
        b.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfflineReadFromCheckpoint();
            }
        });
        mListView = (ListView) v.findViewById(R.id.file_list_view);
        if(chlist != null) {
            mListView.setAdapter(new FileArrayAdapter<>(getContext(), R.layout.file_item, chlist));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File dir = (File) parent.getItemAtPosition(position);
                    File[] pages = dir.listFiles();
                    for (File f : pages) {
                        //Log.d("FileListFragment", f.getName());
                    }

                    //Log.d("FileListFragment", "pa");

                    //File[] chlist = new File(getArguments().getString("dir")).listFiles();

                    Intent intent = new Intent(FileListFragment.this.getContext(), ChapterReader.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("pagelist", pages);
                    bundle.putInt("pages", pages.length);
                    bundle.putSerializable("chlist", chlist);
                    bundle.putInt("chno", position + 1);
                    if (pgno != -1) {
                        bundle.putInt("pgno", pgno);
                        pgno = -1;
                    }
                    bundle.putString("mangatitle", getArguments().getString("title"));
                    bundle.putString("title", dir.getName());
                    bundle.putBoolean("offline", true);

                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            });
        }else{
            b.setVisibility(View.GONE);
        }

        return v;
    }

    private void OfflineReadFromCheckpoint() {
        //Log.d("ReadFromCheckpoint", getContext().getFilesDir().getPath());
        File f = new File(getContext().getFilesDir().getPath() + "/" + getArguments().getString("title") + "checkpoint_OFFLINE");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            int[] checkpoint = (int[]) ois.readObject();

            int maplocation = checkpoint[0] - 1;
            pgno = checkpoint[1];
            //Log.d("ReadFromCheckpoint", "pgno: " + pgno);
            mListView.performItemClick(mListView, maplocation, mListView.getAdapter().getItemId(maplocation)); // FIXME: 7/5/2016 may not work?

            //Chapter ch = mMap.get(Integer.toString(maplocation));
            ////Log.d("ReadFromCheckpoint", ch.content);

            //new AsyncGetPages(ch, Integer.toString(checkpoint[0]), checkpoint[1]).execute();
        } catch (IOException | ClassNotFoundException e) {
            //new AsyncGetPages(mMap.get(Integer.toString(mMap.size())), "1", 1).execute();

            //Log.d("ReadFromCheckpoint", "NOT FOUND");
            int last = mListView.getAdapter().getCount() - 1;
            mListView.performItemClick(mListView, 0, mListView.getAdapter().getItemId(0));
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public class FileListAdapter implements ListAdapter{

        File[] chlist;

        public FileListAdapter(File[] chlist){
            this.chlist = chlist;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return chlist.length;
        }

        @Override
        public Object getItem(int position) {
            return chlist[position].getName();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private class FileArrayAdapter<T> extends ArrayAdapter<T> {

        File[] chlist;

        public FileArrayAdapter(Context context, int resource, T[] objects) {
            super(context, resource, objects);
            this.chlist = (File[]) objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.chlist_item, parent, false);
            TextView t = (TextView) v.findViewById(R.id.text);
            File f = (File) getItem(position);
            t.setText(f.getName());
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Muli.ttf");
            t.setTypeface(tf);
            return v;
        }

        @Override
        public T getItem(int position) {
            return super.getItem(position);
        }
    }

}
