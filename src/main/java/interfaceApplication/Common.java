package interfaceApplication;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef.UserMode;
import common.java.database.dbFilter;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.offices.excelHelper;
import common.java.security.codec;
import common.java.serviceHelper.fastDBService;
import common.java.session.session;
import common.java.string.StringHelper;
import common.java.time.timeHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.star.uno.RuntimeException;

/**
 * 管理
 * 
 *
 */
public class Common {
	private GrapeTreeDBModel common;
	private GrapeDBDescriptionModel gDbSpecField;
	private GrapePermissionsModel grapePermissionsModel;
	private session se;
	private JSONObject userInfo = null;
	private String currentWeb = null;
	private String pkString;
	private fastDBService fdbs;

	/**
	 * 
	 */
	public Common() {
		common = new GrapeTreeDBModel();
		fdbs = new fastDBService("Common");
		se = new session();
		gDbSpecField = new GrapeDBDescriptionModel();
		grapePermissionsModel = new GrapePermissionsModel();
		grapePermissionsModel.importDescription(appsProxy.tableConfig("Common"));
		gDbSpecField.importDescription(appsProxy.tableConfig("Common"));
		common.descriptionModel(gDbSpecField);
		common.permissionsModel(grapePermissionsModel);
		pkString = common.getPk();
		userInfo = se.getDatas();
		common.enableCheck();
		if (userInfo != null && userInfo.size() != 0) {
			currentWeb = userInfo.getString("currentWeb"); // 当前用户所属网站id
		}
	}
	
	
//	  1.排序   y
//	  2.校验（手机，邮箱，密码，微信账号）
//	  3.更新加一操作  y
//	  4.时间字段操作 
//	  5.实名举报=》实名模式的区分（mode字段）  ==> 涉及用考虑是否需要放入common中处理
//	  6.工作流模式中的互动：进度展示OperaReport() ==》工作流模式在通用模式下处理起来很麻烦，考虑重新独立出来 
//	  7.统计，根据时间统计 y
//	  8.导出  y
//	  9.文件路径相关的字段尚未处理  =》李琼重构 
//	  10.考虑是否兼容文章
	 
	
	
	
	/**
	 * 多种参数查询构造 1.无参数查询 2.根据主键查询 3.根据指定字段查询 4.批量查询
	 */
	public String select() {
		return fdbs.select();
	}

	/**
	 * 传入多个主键查询信息
	 * 
	 * @param 
	 * @return
	 */
	public String select(String ids) {
		JSONArray jsonArray = new JSONArray();
		if (!StringHelper.InvaildString(ids)) {
			String[] array = ids.split(",");
			common.or();
			for (String id : array) {
				common.eq(pkString, id);
			}
			jsonArray = common.select();
		}
		return JSONArray.toJSONString(jsonArray);
	}

	/**
	 * 单个查询
	 * @param 
	 * @return
	 */
	public String find(String ids, String fileds) {
		if (StringHelper.InvaildString(ids)) {
			return rMsg.netMSG(1, "无参数信息");
		}
		String[] filedArray = null;
		if (!StringHelper.InvaildString(fileds)) {
			filedArray = fileds.split(",");
		}
		return JSONArray.toJSONString((fdbs.finds(ids, filedArray)));
	}

	/**
	 * 添加信息
	 * 
	 * @param adsInfo
	 * @return
	 */
	public String add(String Info) {
		String message = rMsg.netMSG(99, "添加失败");
		if (StringHelper.InvaildString(Info)) {
			return message = rMsg.netMSG(1, "无参数信息");
		}
		JSONObject object = JSONObject.toJSON(codec.DecodeFastJSON(Info));
		if (object == null || object.size() == 0) {
			return message = rMsg.netMSG(1, "参数解析异常");
		}
		// 先调用putJson将参数组装进入对象中，进行新增
		// putJson();
		// 需要处理某一字段
		int code = common.data(object).insertEx() instanceof Object ? 0 : 99;
		message = code == 0 ? rMsg.netMSG(code, "添加成功") : message;
		return message;
	}

	public String addCommon(String Info) {
		String message = rMsg.netMSG(99, "添加失败");
		if (StringHelper.InvaildString(Info)) {
			return message = rMsg.netMSG(1, "无参数信息");
		}
		JSONObject object = JSONObject.toJSON(codec.DecodeFastJSON(Info));
		if (object == null || object.size() == 0) {
			return message = rMsg.netMSG(1, "参数解析异常");
		}
		int code = common.data(object).insertEx() instanceof Object ? 0 : 99;
		message = code == 0 ? rMsg.netMSG(code, "添加成功") : message;
		return message;
	}
	/**
	 * 修改信息
	 * @param mid
	 *            id
	 * @param msgInfo
	 *            待修改信息，json-string
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String UpdateAD(String id, String Info) {
		JSONObject object = JSONObject.toJSON(codec.DecodeFastJSON(Info));
		String result = rMsg.netMSG(100, "修改失败");
		Boolean flag = false;
		if ((!StringHelper.InvaildString(id)) && (!StringHelper.InvaildString(Info))) {
			if (object != null && object.size() != 0) {
				flag = common.eq(pkString, id).dataEx(object).updateEx();
			}
		}
		return result = flag ? rMsg.netMSG(0, "修改成功") : result;
	}

	/**
	 * 删除
	 * 
	 * @param adid
	 * @return
	 */
	public String DeleteAD(String adid) {
		return DeleteBatchAD(adid);
	}

	/**
	 * 批量删除
	 * 
	 * @param adid
	 * @return
	 */
	public String DeleteBatchAD(String adid) {
		String[] value = null;
		Long code = 0L;
		String message = rMsg.netMSG(99, "删除失败");
		if (!StringHelper.InvaildString(adid)) {
			value = adid.split(",");
			if (value != null && value.length > 0) {
				common.or();
				for (String id : value) {
					common.eq(pkString, id);
				}
				code = common.deleteAllEx();
				message = (code == Long.valueOf(String.valueOf(value.length))) ? rMsg.netMSG(0, "删除成功") : message;
			}
		}
		return message;
	}

	// 分页，若为文章轮播图类型，需填充文章内容
	/** --------------前台分页显示 ---------- **/
	public String PageAD(String wbid, int idx, int pageSize) {
		return PageByAD(wbid, idx, pageSize, null);
	}

	public String PageByAD(String wbid, int idx, int pageSize, String adsInfo) {
		JSONArray CondArray = null;
		JSONArray array = null;
		long total = 0;
		if (!StringHelper.InvaildString(wbid)) {
			if (!StringHelper.InvaildString(adsInfo)) {
				CondArray = buildCond(adsInfo);
				CondArray = (CondArray == null || CondArray.size() <= 0) ? JSONArray.toJSONArray(adsInfo) : CondArray;
				if (CondArray != null && CondArray.size() >= 0) {
					common.where(CondArray);
				}
			}
		}
		array = common.dirty().page(idx, pageSize);
		total = common.count();
		return rMsg.netPAGE(idx, pageSize, total, array);
	}

	/** --------------后台分页显示 ---------- **/
	public String PageADBack(int idx, int pageSize) {
		return PageByAD(currentWeb, idx, pageSize, null);
	}

	public String PageByADBack(int idx, int pageSize, String adsInfo) {
		return PageByAD(currentWeb, idx, pageSize, adsInfo);
	}

	/**
	 * 1.针对涉及文件字段处理，如路径等 2.针对初始赋值或者需要填充字段入表进行特定的业务操作
	 * 3.支持参数类型=》String，Json，JsonArray，map，List拼接
	 */
	public JSONObject putJson(String strJSonArr) {
		// 解析字段是否关于文件处理(字段为文件路径)
		// 解析字段是否关于图片处理(字段为文件路径)
		if (StringHelper.InvaildString(strJSonArr)) {
			return null;
		}
		
		JSONObject json = JSONObject.toJSON(strJSonArr) instanceof JSONObject ? JSONObject.toJSON(strJSonArr)
				: new JSONObject();
		if (null != json) {
			JSONObject newJson = new JSONObject();
			if(json.containsKey("createTime")){
				
			}
			if(json.containsKey("wbid")){
				
			}
			if(json.containsKey("uid")){
				
			}
			//传入的字段和值需要放入
			return json;
		}
		return null;
	}

	/**
	 * 支持hide
	 */
	public String hide(String ids) {
		ArrayList<String> arrayList = new ArrayList<String>();
		if (!StringHelper.InvaildString(ids)) {// TODO 1
			String[] eids_arr = ids.split(",");
			for (String eid : eids_arr) {
				boolean updateEx = common.eq(pkString, eid).eq("deleteable", 0).hide();
				if (!updateEx) {
					arrayList.add(eid);
				}
			}
		} else {
			return rMsg.netMSG(99, "非法参数");
		}
		if (arrayList.size() == 0) {
			return rMsg.netMSG(0, "更新成功");
		} else {
			return rMsg.netMSG(1, "更新失败的有以下   " + arrayList);
		}
	}

	/**
	 * 支持show
	 */
	public String show(String ids) {
		ArrayList<String> arrayList = new ArrayList<String>();
		if (!StringHelper.InvaildString(ids)) {// TODO 1
			String[] eids_arr = ids.split(",");
			for (String id : eids_arr) {
				boolean updateEx = common.eq(pkString, id).eq("deleteable", 1).show();
				if (!updateEx) {
					arrayList.add(id);
				}
			}
		} else {
			return rMsg.netMSG(99, "非法参数");
		}
		if (arrayList.size() == 0) {
			return rMsg.netMSG(0, "更新成功");
		} else {
			return rMsg.netMSG(1, "更新失败的有以下   " + arrayList);
		}
	}

	/**
	 * 地址搜索距离排序
	 * 
	 * 
	 */
	public String getArea_sort(double longitude, double latitude, int raidus, String cond, String sortfeild, int sort) {
		if (raidus > 10000) {
			return rMsg.netMSG(98, "半径不能超过10公里");
		}
		if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90 || raidus < 0) {
			return rMsg.netMSG(99, "参数不对");
		}
		JSONArray select = getArea_JSONArray(longitude, latitude, raidus, cond);
		sort_double(select, sortfeild, sort);
		return rMsg.netMSG(0, select);
	}

	public JSONArray getArea_JSONArray(double longitude, double latitude, int raidus, String cond) {
		double[] around = getAround(longitude, latitude, raidus);
		double minLat = around[0];
		double minLng = around[1];
		double maxLat = around[2];
		double maxLng = around[3];
		dbFilter dbFilter = new dbFilter();
		dbFilter.gte("longitude", minLng).lte("longitude", maxLng).gte("latitude", minLat).lte("latitude", maxLat);
		JSONArray build = dbFilter.build();
		common.and().where(build);
		JSONArray jsonArray = JSONArray.toJSONArray(cond);
		if (jsonArray.size() > 0) {
			common.and().where(jsonArray);
		}
		JSONArray select = common.eq("deleteable", 0).select();
		for (Object object : select) {
			JSONObject obj = (JSONObject) object;
			double longitude1 = (Double) obj.get("longitude");
			double latitude1 = (Double) obj.get("latitude");
			double dx = distanceByLongNLat(longitude, latitude, longitude1, latitude1);
			obj.puts("dx", dx);
			long evaluate = (long) obj.getLong("evaluate");
			long orderCnt = (long) obj.getLong("orderCnt");
			double hot = evaluate * 0.3 + orderCnt * 0.7;
			obj.puts("hot", hot);
			double score = (Double) obj.get("score");
			obj.puts("score", score);
		}
		return select;
	}

	/**
	 * 事由使用次数+1 需要传入Json形式
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String addUseTime(String events) {
		int code = 99;
		String result = rMsg.netMSG(100, "操作失败");
		if (StringHelper.InvaildString(events)) {
			return rMsg.netMSG(3, "该拒绝/完成事由不存在");
		}
		JSONObject json = JSONObject.toJSON(events);
		if (null != json) {
			for (Object obj : json.entrySet()) {
				common.eq(obj.toString(), json.get(obj.toString()));
			}
		}
		JSONObject object = common.dirty().find();
		if (object != null && object.size() > 0) {
			object.put("count", Integer.parseInt(object.getString("count")) + 1);
			code = common.data(object).updateEx() ? 0 : 99;
		}
		result = code == 0 ? rMsg.netMSG(0, "新增次数成功") : result;
		return result;
	}

	/**
	 * 统计某状态下的数据
	 * 
	 * @param filed
	 *            state
	 * @return String 统计后数据
	 */
	public String Count(String fileds, int state) {
		long count = 0;
		JSONObject json = JSONObject.toJSON(fileds);
		if (null != json) {
			for (Object obj : json.entrySet()) {
				common.eq(obj.toString(), json.get(obj.toString()));
			}
		}
		count += common.eq("state", state).count();
		return rMsg.netMSG(0, String.valueOf(count));
	}
	
	/**
	 * 某段时间内统计某状态下的数据
	 * 
	 * @param filed
	 *            state
	 * @return String 统计后数据
	 */
	public long getCountByTimediff(long timediff,int state) {
		long count = 0;
		long currentTime = timeHelper.nowMillis();
		long startTime = currentTime - timediff;
		count = common.gt("time", startTime).lt("time", currentTime).eq("state", state).count();
		return count;
	}

	/**
	 * 导出excel
	 * 
	 * @param info
	 *            查询条件，showField 展示字段集合逗号分割
	 */
	public Object Export(String info, String showField) {
		String reportInfo = searchExportInfo(info, showField);
		if (!StringHelper.InvaildString(reportInfo)) {
			try {
				return excelHelper.out(reportInfo);
			} catch (Exception e) {
				nlogger.logout(e);
			}
		}
		return rMsg.netMSG(false, "导出异常");
	}

	private String searchExportInfo(String info, String showField) {
		JSONArray condArray = null;
		JSONArray array = null;
		if (!StringHelper.InvaildString(info)) {
			condArray = JSONArray.toJSONArray(info);
			if (condArray != null && condArray.size() != 0) {
				common.where(condArray);
			} else {
				return null;
			}
		}
		if (!StringHelper.InvaildString(showField)) {
			array = common.field(showField).select();
		}
		return (array != null && array.size() != 0) ? array.toJSONString() : null;
	}

	
	
	/**
	 * 整合参数，将JSONObject类型的参数封装成JSONArray类型
	 * @param object
	 * @return
	 */
	public JSONArray buildCond(String Info) {
		JSONObject object = JSONObject.toJSON(Info);
		String key;
		Object value;
		JSONArray condArray = null;
		dbFilter filter = new dbFilter();
		if (object != null && object.size() > 0) {
			for (Object object2 : object.keySet()) {
				key = object2.toString();
				value = object.get(key);
				filter.eq(key, value);
			}
			condArray = filter.build();
		} else {
			condArray = JSONArray.toJSONArray(Info);
		}
		return condArray;
	}
	
	//准备提取出工具类
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
