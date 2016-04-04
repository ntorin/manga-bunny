package com.fruits.ntorin.mango.title;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fruits.ntorin.mango.R;
import com.fruits.ntorin.mango.title.ChaptersFragment.OnListFragmentInteractionListener;
import com.fruits.ntorin.mango.dummy.DummyContent.DummyItem;

import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyChaptersRecyclerViewAdapter extends RecyclerView.Adapter<MyChaptersRecyclerViewAdapter.ViewHolder> {

    private final Map<String, Chapter> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyChaptersRecyclerViewAdapter(Map<String, Chapter> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chapters, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(String.valueOf(position + 1));
        holder.mIdView.setText(mValues.get(String.valueOf(position + 1)).id);
        //holder.mContentView.setText(mValues.get(String.valueOf(position + 1)).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Chapter mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }



        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
