/**
 * 
 * FindSearchNews.java
 * classes : com.inspur.emmcloud.bean.FindSearchNews
 * V 1.0.0
 * Create at 2016年10月18日 下午6:07:58
 */
package com.inspur.emmcloud.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import com.inspur.emmcloud.util.TimeUtils;

/**
 * com.inspur.emmcloud.bean.FindSearchNews create at 2016年10月18日 下午6:07:58
 */
public class FindSearchNews {
	private String id = ""; // solr索引id
	private String oid = ""; // 数据原id
	private String title = "";// 新闻标题
	private String authorNews = ""; // 新闻作者
	private String important = ""; // 新闻重要性，f=false t=true
									// 代表新闻是否重要.目前界面上针对重要新闻，让其标题变成黑色；不重要新闻标题为蓝色.
	private String poster = ""; // 新闻封面的图片名
	private String postTime = ""; // 新闻发布时间
	private String postTime_ = ""; // 用于sort排序的时间
	private String publisher = ""; // 属于新闻的发布版面 “集团公告” “集团新闻” “单位公告”
	private String publisherFacet = ""; // 属于新闻的发布版面的分面参数
	private String categoryNcid = ""; // 对新闻的分类。目前总共分为四种：1 单位新闻；2 集团公告；3 集团新闻；4
										// 单位公告
	private String url = ""; // 新闻的网址
	private String dataType = ""; // 分类
	private String textNews = "";// 新闻正文
	private String version_ = "";

	public FindSearchNews(JSONObject obj) {
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("oid")) {
				oid = obj.getString("oid");
			}
			if (obj.has("title")) {
				JSONArray titleArray = obj.getJSONArray("title");
				title =titleArray.getString(0);
			}
			if (obj.has("author_news")) {
				authorNews = obj.getString("author_news");
			}
			if (obj.has("important")) {
				important = obj.getString("important");
			}
			if (obj.has("poster")) {
				poster = obj.getString("poster");
			}
			if (obj.has("posttime")) {
				postTime = obj.getString("posttime");
			}
			if (obj.has("posttime_")) {
				postTime_ = obj.getString("posttime_");
			}
			if (obj.has("publisher_facet")) {
				publisherFacet = obj.getString("publisher_facet");
			}
			if (obj.has("publisher")) {
				publisher = obj.getString("publisher");
			}
			if (obj.has("category_ncid")) {
				categoryNcid = obj.getString("category_ncid");
			}
			if (obj.has("url")) {
				url = obj.getString("url");
			}
			if (obj.has("datatype")) {
				dataType = obj.getString("datatype");
			}
			if (obj.has("text_news")) {
				textNews = obj.getString("text_news");
			}
			if (obj.has("version_")) {
				version_ = obj.getString("version_");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public String getId() {
		return id;
	};

	public String getOid() {
		return oid;
	};

	public String getTitle() {
		return title;
	};

	public String getAuthorNews() {
		return authorNews;
	};

	public String getImportant() {
		return important;
	};

	public String getPoster() {
		return poster;
	};

	public String getPostTime() {
		String[] array = postTime.split(" ");
		if (array.length>1) {
			postTime = array[0];
		}
		postTime = postTime.replaceAll("/", "-");
		return postTime;
	};

	public String getPostTime_() {
		return TimeUtils.getFindSearchNewsDisplayTime(postTime_);
	};

	public String getPublisher() {
		return publisher;
	};

	public String getPublisherFacet() {
		return publisherFacet;
	};

	public String getCategoryNcid() {
		return categoryNcid;
	};

	public String getUrl() {
		return url;
	};

	public String getDataType() {
		return dataType;
	};

	public String getTextNews() {
		return textNews;
	};

	public String getVersion() {
		return version_;
	};
}
