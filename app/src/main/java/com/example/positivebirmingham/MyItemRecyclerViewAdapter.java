package com.example.positivebirmingham;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.positivebirmingham.ListFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * Recycle viewholder holds view
 * adapter binds data to the view
 * {@link RecyclerView.Adapter} that can display a and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<Architecture.ArchitectureItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    public int counter = 0;

    public MyItemRecyclerViewAdapter(List<Architecture.ArchitectureItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    //viewholder == ui elements
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    //binds data to ui elements
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        counter++;

        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(counter));
        holder.mTitleView.setText(mValues.get(position).architectureTitle);
        holder.mDistanceView.setText(String.valueOf(mValues.get(position).architectureDistance));

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

    //size of list

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mTitleView;
        public Architecture.ArchitectureItem mItem;
        public final TextView mDistanceView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDistanceView = (TextView) view.findViewById(R.id.distance);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
