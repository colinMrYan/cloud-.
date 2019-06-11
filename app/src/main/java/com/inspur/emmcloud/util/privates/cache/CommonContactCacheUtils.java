package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.SearchModel;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class CommonContactCacheUtils {
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_NOTHIING = 4;

    /**
     * 保存通讯录联系人或群组的搜索次数
     *
     * @param context
     * @param searchModel
     */
    public static void saveCommonContact(final Context context, final SearchModel searchModel) {
        if (!searchModel.getType().equals(SearchModel.TYPE_USER)) {
            return;
        }
        if (!searchModel.getId().equals(MyApplication.getInstance().getUid())) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        SearchModel originSearchModel = DbCacheUtils.getDb(context).findById(SearchModel.class, searchModel.getId());
                        if (originSearchModel == null) {
                            searchModel.setHeat(1);
                        } else {
                            int heat = originSearchModel.getHeat();
                            heat = heat + 1;
                            searchModel.setHeat(heat);
                        }
                        DbCacheUtils.getDb(context).saveOrUpdate(searchModel);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();
        }

    }

    /**
     * 获取常用联系人列表
     *
     * @param context
     * @param num
     */
    public static List<SearchModel> getCommonContactList(Context context, int num, int selectContent, List<Contact> excludeContactList) {
        List<SearchModel> commonContactList = null;
        try {

            switch (selectContent) {
                case SEARCH_ALL:
                case SEARCH_NOTHIING:
                    commonContactList = DbCacheUtils.getDb(context).selector(SearchModel.class).where("type", "=", "USER").orderBy("heat", true).limit(num).findAll();
                    break;
//                case SEARCH_CHANNELGROUP:
//                    commonContactList = DbCacheUtils.getDb(context).selector
//                        (SearchModel.class).where("type", "=", "GROUP").orderBy("heat", true).limit(num).findAll();
//                    break;
                case SEARCH_CONTACT:
                    String noInSql = "()";
                    noInSql = getNoInSql(noInSql, excludeContactList);
                    commonContactList = DbCacheUtils.getDb(context).selector
                            (SearchModel.class).where("type", "=", "USER")
                            .and(WhereBuilder.b().expr("id not in" + noInSql))
                            .orderBy("heat", true).limit(num).findAll();
                default:
                    break;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (commonContactList == null) {
            commonContactList = new ArrayList<SearchModel>();
        }
        return commonContactList;
    }


    /**
     * 删除单个常用联系人(只删除组)
     * lbc 2018/09/13
     *
     * @param context
     * @param searchModel 删除联系人groupID
     **/
    public static void delectCommonContact(Context context, SearchModel searchModel) {
        try {
            DbCacheUtils.getDb(context).delete(searchModel);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取sql中的id数组
     *
     * @param noInSql
     * @param addSearchContactList
     * @return
     */
    private static String getNoInSql(String noInSql, List<Contact> addSearchContactList) {
        if (addSearchContactList != null && addSearchContactList.size() > 0) {
            noInSql = noInSql.substring(0, noInSql.length() - 1);
            if (noInSql.length() > 1) {
                noInSql = noInSql + ",";
            }
            for (int i = 0; i < addSearchContactList.size(); i++) {
                noInSql = noInSql + addSearchContactList.get(i).getId() + ",";
            }
            if (noInSql.endsWith(",")) {
                noInSql = noInSql.substring(0, noInSql.length() - 1);
            }
            noInSql = noInSql + ")";
        }
        return noInSql;
    }

}
