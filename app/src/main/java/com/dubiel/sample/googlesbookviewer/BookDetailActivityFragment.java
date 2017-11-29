package com.dubiel.sample.googlesbookviewer;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItems;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;


public class BookDetailActivityFragment extends Fragment {

    public static final String ARG_SELF_LINK = "self_link";

    private String selfLink;

    public BookDetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SELF_LINK)) {

            selfLink = getArguments().getString(ARG_SELF_LINK);

//            Activity activity = this.getActivity();
//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                appBarLayout.setTitle(mItem.content);
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

//        try {
//            return Ion.with(getApplicationContext())
//                    .load(getUrl())
//                    .as(new TypeToken<BookListItems>() {
//                    }).get();
//        } catch(InterruptedException | ExecutionException e) {
//            System.out.println(e.getMessage());
//        }

        return rootView;
    }
}
