package com.dubiel.sample.googlesbookviewer;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dubiel.sample.googlesbookviewer.search.searchitem.BookListItem;

import com.squareup.picasso.Picasso;

public class BookItemListAdapter extends RecyclerView.Adapter<BookItemListAdapter.ViewHolder> {
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
    public void onBindViewHolder(BookItemListAdapter.ViewHolder viewHolder, int i) {
        Picasso.with(context).load(bookListItems[i].getVolumeInfo().getImageLinks().getSmallThumbnail())
                .resize(R.integer.small_thumbnail_width, R.integer.small_thumbnail_height).into(viewHolder.smallThumbnail);
        viewHolder.title.setText(bookListItems[i].getVolumeInfo().getTitle());
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
        public ViewHolder(View view) {
            super(view);
            title = (TextView)view.findViewById(R.id.book_item_title);
            smallThumbnail = (ImageView) view.findViewById(R.id.book_item_small_thumbnail);
        }
    }

}
