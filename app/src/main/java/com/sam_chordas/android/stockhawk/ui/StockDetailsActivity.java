package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

public class StockDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 0;
    public static final String EXTRA_TEXT_SYMBOL = "symbol";
    LineSet mLineSet;
    LineChartView lineChartView;
    int minRange, maxRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra(EXTRA_TEXT_SYMBOL));

        lineChartView = (LineChartView) findViewById(R.id.linechart);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TEXT_SYMBOL,intent.getStringExtra(EXTRA_TEXT_SYMBOL));
        getLoaderManager().initLoader(LOADER_ID, bundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(EXTRA_TEXT_SYMBOL)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.d("cursor data", DatabaseUtils.dumpCursorToString(data));
        createRangeAndLineSet(data);
        fillLineChartView();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void createRangeAndLineSet(Cursor cursor) {
        mLineSet = new LineSet();
        ArrayList<Float> mArrayList = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isLast()) {
            String label = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
            float value = Float.parseFloat(label);
            mArrayList.add(value);
            mLineSet.addPoint(label, value);
            cursor.moveToNext();
        }

        if(mArrayList.size() > 1) {
            maxRange = Math.round(Collections.max(mArrayList));
            minRange = Math.round(Collections.min(mArrayList));
        }

    }

    public void fillLineChartView() {

        mLineSet.setColor(ContextCompat.getColor(this, R.color.graph_fill))
                .setFill(ContextCompat.getColor(this, R.color.graph_fill))
                .setDotsColor(ContextCompat.getColor(this, R.color.graph_dot))
                .setDashed(new float[]{5f, 5f})
                .setThickness(2);

        lineChartView.setBorderSpacing(Tools.fromDpToPx(5))
                .setAxisBorderValues(minRange - 5, maxRange + 5)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(ContextCompat.getColor(this, R.color.graph_label))
                .setXAxis(false)
                .setYAxis(false)
                .addData(mLineSet);

        if(mLineSet.size() > 1)
            lineChartView.show();
        else
            Snackbar.make(lineChartView, R.string.no_data_for_graph, Snackbar.LENGTH_LONG).show();
    }
}
