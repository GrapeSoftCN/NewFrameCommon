package util;

import java.util.Collections;
import java.util.Comparator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.star.uno.RuntimeException;

import common.java.database.dbFilter;

public class Sort {

	/**
	 * TODO(这里用一句话描述这个方法的作用)
	 * 
	 * @param js
	 * @param feild
	 *            排序字段
	 * @param sort
	 *            1顺序 -1逆序
	 */
	@SuppressWarnings("unchecked")
	public static void sort_double(JSONArray js,  final String feild, final int sort) {
		Collections.sort(js, new Comparator<JSONObject>() {
			public int compare(JSONObject o1, JSONObject o2) {
				double ob1 = (Double) o1.get(feild);
				double ob2 = (Double) o2.get(feild);
				if (sort == 1) {
					if (ob1 > ob2) {
						return -1;
					} else if (ob1 < ob2) {
						return 1;
					} else {
						return 0;
					}
				}
				if (sort == -1) {
					if (ob1 > ob2) {
						return 1;
					} else if (ob1 < ob2) {
						return -1;
					} else {
						return 0;
					}
				}
				throw new RuntimeException("sort非法");
			}
		});
	}
	
	
	public  static double distanceByLongNLat(double long1, double lat1, double long2, double lat2) {
		double a, b, R;
		R = 6378137;// 地球半径
		lat1 = lat1 * Math.PI / 180.0;
		lat2 = lat2 * Math.PI / 180.0;
		a = lat1 - lat2;
		b = (long1 - long2) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
		return d;
	}
	public static double[] getAround(double lat, double lon, int raidus) {

		Double latitude = lat;
		Double longitude = lon;

		Double degree = (24901 * 1609) / 360.0;
		double raidusMile = raidus;

		Double dpmLat = 1 / degree;
		Double radiusLat = dpmLat * raidusMile;
		Double minLat = latitude - radiusLat;
		Double maxLat = latitude + radiusLat;

		Double mpdLng = degree * Math.cos(latitude * (Math.PI / 180));
		Double dpmLng = 1 / mpdLng;
		Double radiusLng = dpmLng * raidusMile;
		Double minLng = longitude - radiusLng;
		Double maxLng = longitude + radiusLng;
		return new double[] { minLat, minLng, maxLat, maxLng };
	}
}
