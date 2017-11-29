package com.dubiel.sample.googlesbookviewer;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItem;

import com.squareup.picasso.Picasso;

public class BookItemListAdapter extends RecyclerView.Adapter<BookItemListAdapter.ViewHolder> {
    private static int SMALL_THUMBNAIL_WIDTH = 128;
    private static int SMALL_THUMBNAIL_HEIGHT = 192;

    private Context context;
    private BookListItem[] bookListItems;

    public BookItemListAdapter(Context context) {
        this.context = context;
        this.bookListItems = new BookListItem[0];
    }

    public BookItemListAdapter(Context context, BookListItem[] bookListItems) {
        this.context = context;
        this.bookListItems = bookListItems;
    }

    @Override
    public BookItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookItemListAdapter.ViewHolder viewHolder, int i) {
        viewHolder.selfLink = bookListItems[i].getSelfLink();
        Picasso.with(context).load(bookListItems[i].getVolumeInfo().getImageLinks().getSmallThumbnail())
                .resize(SMALL_THUMBNAIL_WIDTH, SMALL_THUMBNAIL_HEIGHT)
                .into(viewHolder.smallThumbnail);
        viewHolder.title.setText(bookListItems[i].getVolumeInfo().getTitle());

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra(BookDetailActivityFragment.ARG_SELF_LINK, viewHolder.selfLink);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookListItems.length;
    }

    public void setBookListItems(BookListItem[] bookListItems) {
        this.bookListItems = bookListItems;
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
    }

}
