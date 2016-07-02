package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

public class StockHawkWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor cursor = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(cursor!=null) {
                    cursor.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                if(cursor==null)
                    return 0;
                else
                    return cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position))
                    return null;

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                views.setTextViewText(R.id.stock_symbol, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
                views.setTextViewText(R.id.bid_price, cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
                views.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));

                if(cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1)
                    views.setInt(R.id.change, getString(R.string.set_background_resource), R.drawable.percent_change_pill_green);
                else
                    views.setInt(R.id.change, getString(R.string.set_background_resource), R.drawable.percent_change_pill_red);

                Intent intent = new Intent();
                intent.putExtra(StockDetailsActivity.EXTRA_TEXT_SYMBOL, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
                views.setOnClickFillInIntent(R.id.stock_view, intent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(cursor.moveToPosition(position))
                    return cursor.getLong(cursor.getColumnIndex(QuoteColumns._ID));
                else
                    return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
