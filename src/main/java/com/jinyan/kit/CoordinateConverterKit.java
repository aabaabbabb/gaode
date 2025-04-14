package com.jinyan.kit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CoordinateConverterKit {
	// 定义常量，百度坐标系和高德坐标系的地球半径
	private static final double x_pi = Math.PI * 3000.0 / 180.0;
	private static final double z = Math.sqrt(3) - 1;
	private static final double x_e = Math.sqrt(2 / (1 + z) * (1 - z));
	private static final double x_e2 = x_e * x_e;
	private static final double e1 = (1 - x_e2) / (1 - x_e);

	public static double[] bd09ToGcj02(double bd_lon, double bd_lat) {
		double x = bd_lon - 0.0065;
		double y = bd_lat - 0.006;

		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);

		double gg_lng = z * Math.cos(theta);
		double gg_lat = z * Math.sin(theta);

		double dx = gg_lng - bd_lon;
		double dy = gg_lat - bd_lat;

		double[] ret = new double[2];
		ret[0] = bd_lon + dx;
		ret[1] = bd_lat + dy;

		return ret;
	}

	public static List<String> getCoordinate(double bd_lon, double bd_lat) {
		List<String> list = new ArrayList<String>();
		double[] gcj02 = CoordinateConverterKit.bd09ToGcj02(bd_lon, bd_lat);
		DecimalFormat df = new DecimalFormat("0.000000");
		list.add(df.format(gcj02[0]));
		list.add(df.format(gcj02[1]));
		return list;
	}

	public static void main(String[] args) {
		double bd_lon = 104.14773; // 百度经度
		double bd_lat = 30.79137; // 百度纬度

		double[] gcj02 = bd09ToGcj02(bd_lon, bd_lat);

		DecimalFormat df = new DecimalFormat("0.000000");
		System.out.println("高德经度: " + df.format(gcj02[0]));
		System.out.println("高德纬度: " + df.format(gcj02[1]));
	}
}
