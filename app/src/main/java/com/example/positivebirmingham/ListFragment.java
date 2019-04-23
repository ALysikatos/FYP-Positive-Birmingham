package com.example.positivebirmingham;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.example.positivebirmingham.MapsActivity.tabLayout;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private RecyclerView recyclerView;
    private static Bundle mBundleRecyclerViewState;
    private Parcelable recyclerViewState;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListFragment() {
    }

    @SuppressWarnings("unused")
    public static ListFragment newInstance(int columnCount) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        //setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save list state
       // mListState = mLayoutManager.onSaveInstanceState();
     //   outstate.putParcelable(KEY_RECYCLER_STATE, mListState);

        // save RecyclerView state
      //  mBundleRecyclerViewState = new Bundle();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        if (recyclerView.getLayoutManager().onSaveInstanceState() != null ){
        Log.i("Wotzit", "hey");}
        outState.putParcelable(KEY_RECYCLER_STATE, recyclerViewState);
        super.onSaveInstanceState(outState);
      //  outState.putParcelable("KeyForLayoutManagerState", LinearLayoutManagerInstance.onSaveInstanceState());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        //recyclerView = view.findViewById(R.id.recycler_view);

        if (savedInstanceState != null) {
            //recyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            //recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            recyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            if (recyclerView != null) {
            }
            recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(Architecture.ITEMS, mListener));
          //  return recyclerView;

            //   Parcelable state = savedInstanceState.getParcelable("KeyForLayoutManagerState");
            //  linearLayoutManagerInstance.onRestoreInstanceState(state);
        } else {


            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                //  if (recyclerView == null) {
                recyclerView = (RecyclerView) view;

                DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
                recyclerView.addItemDecoration(itemDecorator);

                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                // recyclerView.setAdapter(new MyItemRecyclerViewAdapter(Architecture.ITEMS, mListener));
                Architecture architecture = Architecture.getInstance(getContext());
                //architecture.setUpArchitectureItems();
                List<Architecture.ArchitectureItem> blah = new ArrayList<Architecture.ArchitectureItem>();
                List<Architecture.ArchitectureItem> bleh = new ArrayList<Architecture.ArchitectureItem>();
                List<Architecture.ArchitectureItem> bluh = new ArrayList<Architecture.ArchitectureItem>();
                blah = Architecture.getITEMS();
                bleh = architecture.getITEMS();
                bluh = Architecture.ITEMS;

                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(Architecture.ITEMS, mListener));
                Log.i("hug", String.valueOf(blah.size()));
                Log.i("hugo", String.valueOf(bleh.size()));
                Log.i("hugos", String.valueOf(bluh.size()));
            }
        }
        return view;
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
        Log.i("mytag", "my log");
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
        void onListFragmentInteraction(Architecture.ArchitectureItem item);
    }
}
