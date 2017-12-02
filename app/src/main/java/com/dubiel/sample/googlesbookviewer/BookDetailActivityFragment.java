package com.dubiel.sample.googlesbookviewer;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dubiel.sample.googlesbookviewer.search.searchitem.BookDetailItem;
import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItems;
import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutionException;


public class BookDetailActivityFragment extends Fragment {

    public static final String ARG_SELF_LINK = "self_link";
    static final private String TAG = "BookDetailFragment";

    private String selfLink;
    private int smallImageWidth, smallImageHeight;

    public BookDetailActivityFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SELF_LINK)) {

            smallImageWidth = getContext().getResources().getInteger(R.integer.small_image_width);
            smallImageHeight = getContext().getResources().getInteger(R.integer.small_image_height);

            selfLink = getArguments().getString(ARG_SELF_LINK);
//            System.out.println("selfLink: " + selfLink);

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

        final ImageView small = (ImageView) rootView.findViewById(R.id.book_detail_item_small);
        final TextView title = (TextView) rootView.findViewById(R.id.book_detail_item_title);
        final TextView authors = (TextView) rootView.findViewById(R.id.book_detail_item_author);
        final WebView description = (WebView) rootView.findViewById(R.id.book_detail_item_description);
        final TextView infoLink = (TextView) rootView.findViewById(R.id.book_detail_item_info_link);

        Ion.with(getContext())
                .load(selfLink)
                .as(new TypeToken<BookDetailItem>() {
                }).setCallback(new FutureCallback<BookDetailItem>() {
            @Override
            public void onCompleted(Exception e, BookDetailItem bookDetailItem) {

                try {
                    Picasso.with(getContext()).load(bookDetailItem.getVolumeInfo().getImageLinks().getSmall())
                            .resize(smallImageWidth, smallImageHeight)
                            .into(small);
                    title.setText(bookDetailItem.getVolumeInfo().getTitle());
                    authors.setText(Joiner.on("\n").join(bookDetailItem.getVolumeInfo().getAuthors()));
                    description.loadDataWithBaseURL(null, bookDetailItem.getVolumeInfo().getDescription(), "text/html", "utf-8", null);
                    infoLink.setText(bookDetailItem.getVolumeInfo().getInfoLink());
                } catch (NullPointerException npe) {
                    Log.i(BookDetailActivityFragment.TAG, "npe " + selfLink + ", " + npe.getMessage());
                }

//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                    description.setText(Html.fromHtml(bookDetailItem.getVolumeInfo().getDescription(), Html.FROM_HTML_MODE_LEGACY));
//                } else {
//                    description.setText(Html.fromHtml(bookDetailItem.getVolumeInfo().getDescription()));
//                }

//                System.out.println("bookDetailItem.title: " + bookDetailItem.getVolumeInfo().getTitle());
//                System.out.println("bookDetailItem.authors: " + bookDetailItem.getVolumeInfo().getAuthors()[0]);
//                System.out.println("bookDetailItem.small: " + bookDetailItem.getVolumeInfo().getImageLinks().getSmall());
            }
        });

        return rootView;
    }
}
