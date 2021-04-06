// Copyright (C) 2009 by NHN Corporation. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.VerticalAlignment;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class ChartResponder implements Responder {
	private boolean isEmpty(Object object) {
		return object == null || "".equals(object);
	}

	public Response makeResponse(FitNesseContext context, Request request)
			throws Exception {
		JFreeChart chart = null;
		SimpleResponse response = new SimpleResponse();

		if (!isEmpty(request.getInput("data"))) {
			chart = getCategoryChart("Test Chart",
					getDatasetFromData((String) request.getInput("data")));
		} else if (!isEmpty(request.getInput("file"))) {
			String[] values = new String[] { "a", "b", "c", "d" };
			int i = 0;
			StringTokenizer stok = new StringTokenizer((String) request
					.getInput("file"), "{}", false);
			while (stok.hasMoreTokens()) {
				values[i] = stok.nextToken();
				i++;
			}

			chart = getCategoryChart(values[0], getDatasetFromData(values));
		} else if (!isEmpty(request.getInput("url"))) {

		}

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat();
		calendar.setTimeInMillis(0);

		response.setExpiresHeader(format.format(calendar.getTime()));
		response.setMaxAge(0);

		byte[] image = getChartImage(chart, 400, 250);

		if (image == null || image.length == 0) {
			final String invalid = "invalid data";
			response.setContentType("text/plain");
			response.setContent(invalid);
		} else {
			response.setContentType("image/png");
			response.setContent(image);
		}

		return response;
	}

	private CategoryDataset getDatasetFromData(String data) {
		final String series = "Series";
		String[] datas = data.split(",");
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (int i = 0; i < datas.length; i++) {
			dataset.addValue(Double.valueOf(datas[i]), series, String
					.valueOf(i + 1));
		}

		return dataset;
	}

	private CategoryDataset getDatasetFromData(String[] data) {
		final String[] series = { "wrong", "right", "exception"};
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (int i = 1; i < data.length; i++) {
			String[] datas = data[i].split(",");

			for (int j = 0; j < datas.length; j++) {
				dataset.addValue(Double.valueOf(datas[j]), series[i - 1],
						String.valueOf(j + 1));
			}
		}

		return dataset;
	}

	private JFreeChart getCategoryChart(String title, CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createAreaChart(title,
				"Test Execution", "Test Counts", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(Color.white);
		
		final TextTitle subtitle = new TextTitle(
				"NTAF Test execution result AreaChart.");
		subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		subtitle.setVerticalAlignment(VerticalAlignment.BOTTOM);
		chart.addSubtitle(subtitle);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setForegroundAlpha(0.4f);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		return chart;
	}

	private byte[] getChartImage(JFreeChart chart, int width, int height)
			throws IOException {
		if (chart == null) {
			return null;
		}

		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		return encoder.encode(getBufferedImage(chart, width, height));
	}

	private BufferedImage getBufferedImage(JFreeChart chart, int width,
			int height) {
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_BGR);
		Graphics2D graphics2d = bufferedImage.createGraphics();
		chart.draw(graphics2d, new Rectangle2D.Double(0, 0, width, height));
		graphics2d.dispose();
		return bufferedImage;
	}
}