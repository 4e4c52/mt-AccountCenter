package com.syrinxoon.mtaccountcenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Average temperature demo chart.
 */
public class StatsChart extends AbstractChart {
	
	String[] infos = null;
	long[] dates = null;
	double[] cpu = null;
	double[] memory = null;
	int dataNum = 0;
	
	public StatsChart(String[] infos, long[] dates, double[] cpu, double[] memory, int dataNum) {
		this.infos = infos;
		this.dates = dates;
		this.cpu = cpu;
		this.memory = memory;
		this.dataNum = dataNum;
	}
	
	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	public String getName() {
		return "Statistics";
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return "Server statistics for the last 15 minutes";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context the context
	 * @return the built intent
	 */
	public GraphicalView execute(Context context) {
	  
		String[] titles = this.infos;
		
		List<Date[]> x = new ArrayList<Date[]>();
	    for (int i = 0; i < titles.length; i++) {
	      Date[] dates = new Date[this.dataNum];
	      for (int j = 0; j < this.dataNum; j++) {
	        dates[j] = new Date(this.dates[j]);
	      }
	      x.add(dates);
	    }
    
		List<double[]> values = new ArrayList<double[]>();
    
		values.add(this.cpu);
		values.add(this.memory);
    
		int[] colors = new int[] { Color.rgb(223, 128, 128), Color.rgb(134, 135, 134) };
		
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
		
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    
		int length = renderer.getSeriesRendererCount();
    
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
		}
		
	    renderer.setYAxisMin(0);
	    renderer.setYAxisMax(100);
	    renderer.setAxesColor(Color.LTGRAY);
	    renderer.setLabelsColor(Color.LTGRAY);
		renderer.setXLabels(this.dataNum);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		
		GraphicalView view = ChartFactory.getTimeChartView(context, buildDateDataset(titles, x, values),  renderer, "HH:mm");
		
		return view;
	}

}
