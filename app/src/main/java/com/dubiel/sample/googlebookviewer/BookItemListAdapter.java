package com.dubiel.sample.googlebookviewer;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dubiel.sample.googlebookviewer.bookdetail.BookDetailActivity;
import com.dubiel.sample.googlebookviewer.bookdetail.BookDetailActivityFragment;
import com.dubiel.sample.googlebookviewer.search.SearchManager;
import com.dubiel.sample.googlebookviewer.search.searchitem.BookListItem;

import com.dubiel.sample.googlebookviewer.search.searchitem.BookListItems;
import com.google.common.cache.Cache;
import com.squareup.picasso.Picasso;

public class BookItemListAdapter extends RecyclerView.Adapter<BookItemListAdapter.ViewHolder> {

    static final private String TAG = "BookItemListAdapter";

    private Context context;
    private Cache<Integer, BookListItems> bookListItems;
    private int smallThumbnailWidth, smallThumbnailHeight;
    private int itemCount;

    public BookItemListAdapter(Context context, Cache<Integer, BookListItems> bookListItems) {
        this.context = context;
        this.bookListItems = bookListItems;

        smallThumbnailWidth = context.getResources().getInteger(R.integer.small_thumbnail_width);
        smallThumbnailHeight = context.getResources().getInteger(R.integer.small_thumbnail_height);
    }

    @Override
    public BookItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookItemListAdapter.ViewHolder viewHolder, int i) {
        int key = (int)Math.floor(i / SearchManager.MAX_RESULTS);

        try {
            BookListItems currentBookListItems = bookListItems.getIfPresent(key);

            int bookListItemIndex = i % SearchManager.MAX_RESULTS;

            if(bookListItemIndex >= currentBookListItems.getItems().length) {
                return;
            }

            BookListItem currentBookListItem = currentBookListItems.getItems()[bookListItemIndex];

            viewHolder.selfLink = currentBookListItem.getSelfLink();
            try {
                Picasso.with(context).load(currentBookListItem.getVolumeInfo().getImageLinks().getSmallThumbnail())
                        .resize(smallThumbnailWidth, smallThumbnailHeight)
                        .into(viewHolder.smallThumbnail);
            } catch(NullPointerException e) {
                Log.i(BookItemListAdapter.TAG, "small thumbnail image of " + currentBookListItem.getSelfLink() + " not present");
            }
            viewHolder.title.setText(currentBookListItem.getVolumeInfo().getTitle());

            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra(BookDetailActivityFragment.ARG_SELF_LINK, viewHolder.selfLink);

                    context.startActivity(intent);
                }
            });
        } catch(Exception e) {
            Log.e(BookItemListAdapter.TAG, e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if(bookListItems.size() == 0) {
            return 0;
        }
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView smallThumbnail;
        private TextView title;

        public final View view;
        public String selfLink;

        public ViewHolder(View view) {
            super(view);

            this.view = view;

            title = (TextView)view.findViewById(R.id.book_item_title);
            smallThumbnail = (ImageView) view.findViewById(R.id.book_item_small_thumbnail);
        }

        public ImageView getSmallThumbnail() {
            return smallThumbnail;
        }

        public TextView getTitle() {
            return title;
        }
    }

}
