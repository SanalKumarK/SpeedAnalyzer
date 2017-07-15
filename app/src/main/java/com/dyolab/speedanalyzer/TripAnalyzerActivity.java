package com.dyolab.speedanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.dyolab.speedanalyzer.model.TripAnalyzerReport;
import com.dyolab.speedanalyzer.model.TripDO;
import com.dyolab.speedanalyzer.model.TripSpeedDetailsDO;
import com.dyolab.speedanalyzer.service.SpeedAnalyzerModelProvider;
import com.dyolab.speedanalyzer.util.SpeedAnalyzerUtil;
import com.dyolab.speedanalyzer.util.TripAnalyzer;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class TripAnalyzerActivity extends AppCompatActivity {

    public SpeedAnalyzerModelProvider modelProvider;

    private GraphView mGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        modelProvider = new SpeedAnalyzerModelProvider(getBaseContext());

        setContentView(R.layout.activity_trip_analyzer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reStyleGraph();
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        TripDO trip = (TripDO) intent.getSerializableExtra(TripActivity.TRIP);
        displayAnalyzerActivity(trip);
    }

    private void reStyleGraph() {
        mGraphView = (GraphView) findViewById(R.id.graph);
        mGraphView.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.graph_x_label));
        mGraphView.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.graph_y_label));

        // custom label formatter
        mGraphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return "";
                } else {
                    // show y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });
    }

    private void displayAnalyzerActivity(TripDO trip) {
        TripAnalyzer analyzer = new TripAnalyzer();

        ArrayList<TripSpeedDetailsDO> speedLogs = (ArrayList) modelProvider.getTripSpeedDetailsInfo(trip.getTripCode());

        if(speedLogs != null ) {
            TripAnalyzerReport report = analyzer.getTripAnalyzeReport(speedLogs);

            //Apply Data for the graph
            renderGraph(speedLogs);

            TextView maxSpeedTxt = (TextView) findViewById(R.id.maxSpeed);
            maxSpeedTxt.setText(SpeedAnalyzerUtil.speedFormat.format(report.getMaxSpeed()) +
                    " " + SpeedAnalyzerUtil.unit);

            TextView minSpeedTxt = (TextView) findViewById(R.id.minSpeed);
            minSpeedTxt.setText(SpeedAnalyzerUtil.speedFormat.format(report.getMinSpeed())+
                    " " + SpeedAnalyzerUtil.unit);

            TextView avgSpeedTxt = (TextView) findViewById(R.id.avgSpeed);
            avgSpeedTxt.setText(SpeedAnalyzerUtil.speedFormat.format(report.getAvgSpeed())+
                    " " + SpeedAnalyzerUtil.unit);

            TextView idleTime = (TextView) findViewById(R.id.idleTime);
            idleTime.setText(SpeedAnalyzerUtil.speedFormat.format(report.getIdleTime()) +
                    " mins");
        }
    }

    private void renderGraph(ArrayList<TripSpeedDetailsDO> speedLogs) {
        //Parse SpeedLogs for populating the graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        int i = 0;
        for (TripSpeedDetailsDO speedDO: speedLogs) {
            series.appendData(new DataPoint(i++, speedDO.getSpeed()), true, i);
        }

        mGraphView.addSeries(series);
    }
}