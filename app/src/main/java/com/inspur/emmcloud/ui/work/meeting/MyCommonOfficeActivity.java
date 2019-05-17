package com.inspur.emmcloud.ui.work.meeting;

import com.inspur.emmcloud.BaseActivity;

/**
 * 我的办公地点
 */
public class MyCommonOfficeActivity extends BaseActivity {
//
//{
//    private static final int CREATE_COMMON_OFFICE = 0;
//    private LoadingDialog loadingDialog;
//    private WorkAPIService apiService;
//    private List<Office> commonOfficeList = new ArrayList<Office>();
//    private List<Office> originCommonOfficeList = new ArrayList<Office>();
//    private MyCommonOfficeAdapter myCommonOfficeAdapter;
//    private boolean isOrdering = false;
//    private List<String> selectOfficeIdList = new ArrayList<String>();
//    private String userId = "";
//    private boolean isOfficeChange = false;
//    private DragSortListView dragSortListView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_common_office);
//        initViews();
//        getOffice();
//    }
//
//    /**
//     * 初始化views
//     */
//    private void initViews() {
//        apiService = new WorkAPIService(MyCommonOfficeActivity.this);
//        apiService.setAPIInterface(new WebService());
//        loadingDialog = new LoadingDialog(MyCommonOfficeActivity.this);
//        dragSortListView = (DragSortListView) findViewById(R.id.meeting_my_common_office_listview);
//        DragSortController dragSortController = buildController(dragSortListView);
//        dragSortListView.setFloatViewManager(dragSortController);
//        dragSortListView.setOnTouchListener(dragSortController);
//        dragSortListView.setDragEnabled(true);
//        dragSortListView
//                .setOnItemClickListener(new OnItemSelectClickListener());
//        dragSortListView
//                .setOnItemLongClickListener(new OnMyOfficeLongClickListener());
//        dragSortListView.setDropListener(new DropListener() {
//            @Override
//            public void drop(int from, int to) {
//                if (from != to) {
//                    Office item = commonOfficeList.get(from);
//                    commonOfficeList.remove(item);
//                    commonOfficeList.add(to, item);
//                    myCommonOfficeAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//        userId = ((MyApplication) getApplicationContext()).getUid();
//        String selectCommonOfficeIds = PreferencesUtils.getString(
//                MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent() + userId
//                        + "selectCommonOfficeIds");
//        selectOfficeIdList = JSONUtils.JSONArray2List(selectCommonOfficeIds, new ArrayList<String>());
//    }
//
//    /**
//     * 获取办公地点
//     */
//    private void getOffice() {
//        if (NetUtils.isNetworkConnected(MyCommonOfficeActivity.this)) {
//            loadingDialog.show();
//            apiService.getOfficeList();
//        }
//    }
//
//    /**
//     * onClick方法
//     *
//     * @param view
//     */
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.ibt_back:
//                backActivity();
//                break;
//            case R.id.meeting_common_office_create_img:
//                Intent intent = new Intent();
//                intent.setClass(MyCommonOfficeActivity.this,
//                        CreateCommonOfficeSpaceActivity.class);
//                startActivityForResult(intent, CREATE_COMMON_OFFICE);
//                break;
//            case R.id.meeting_common_office_order_img:
//                handleOrder();
//                break;
//            case R.id.meeting_common_office_confirm_layout:
//            case R.id.meeting_common_office_confirm_text:
//                releaseOrder();
//                break;
//            default:
//                break;
//        }
//
//    }
//
//    /**
//     * 返回上层Activity
//     */
//    private void backActivity() {
//        if (isOfficeChange) {
//            setResult(RESULT_OK);
//        }
//        finish();
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            backActivity();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == CREATE_COMMON_OFFICE) {
//                getOffice();
//            }
//        }
//    }
//
//    /**
//     * 释放排序
//     */
//    private void releaseOrder() {
//        isOfficeChange = true;
//        (findViewById(R.id.meeting_common_office_create_layout)).setVisibility(View.VISIBLE);
//        (findViewById(R.id.meeting_common_office_confirm_layout)).setVisibility(View.GONE);
//        originCommonOfficeList = commonOfficeList;
//        saveAllOfficeIds(false);
//        isOrdering = false;
//        myCommonOfficeAdapter.notifyDataSetChanged();
//    }
//
//    /**
//     * 打开排序
//     */
//    private void handleOrder() {
//        (findViewById(R.id.meeting_common_office_create_layout)).setVisibility(View.GONE);
//        (findViewById(R.id.meeting_common_office_confirm_layout)).setVisibility(View.VISIBLE);
//        isOrdering = true;
//        if (myCommonOfficeAdapter != null) {
//            myCommonOfficeAdapter.notifyDataSetChanged();
//        }
//
//    }
//
//    /**
//     * 按顺序保存所有的办公地点
//     *
//     * @param isFirst 判断是不是第一次保存数据（当第一次如果没有勾选任何常用办公地点，怎默认全勾选）
//     */
//    private void saveAllOfficeIds(boolean isFirst) {
//        List<String> allCommonOfficeIdList = new ArrayList<String>();
//        List<String> allCommonBuildingIdList = new ArrayList<String>();
//        List<String> newSelectOfficeList = new ArrayList<String>();
//        for (int i = 0; i < originCommonOfficeList.size(); i++) {
//            Office office = originCommonOfficeList.get(i);
//            if (selectOfficeIdList.contains(office.getId())) {
//                newSelectOfficeList.add(office.getId());
//            }
//            allCommonOfficeIdList.add(office.getId());
//            allCommonBuildingIdList.add(office.getId());
//        }
//        selectOfficeIdList = newSelectOfficeList;
//        //当第一次如果没有勾选任何常用办公地点，怎默认全勾选
//        if (isFirst && (selectOfficeIdList.size() == 0)) {
//            selectOfficeIdList = allCommonOfficeIdList;
//        }
//        LogUtils.debug("jason", "selectOfficeIdList.size=" + selectOfficeIdList.size());
//        PreferencesUtils.putString(MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent()
//                        + userId + "allCommonOfficeIds",
//                JSONUtils.toJSONString(allCommonOfficeIdList));
//        PreferencesUtils.putString(MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent()
//                        + userId + "allCommonBuildingIds",
//                JSONUtils.toJSONString(allCommonBuildingIdList));
//        PreferencesUtils.putString(MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent()
//                        + userId + "selectCommonOfficeIds",
//                JSONUtils.toJSONString(selectOfficeIdList));
//    }
//
//    @Override
//    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
//    }
//
//    @Override
//    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
//    }
//
//    /**
//     * 显示把手
//     *
//     * @param imageView
//     */
//    public void renderHandleImg(ImageView imageView) {
//        if (isOrdering) {
//            imageView.setVisibility(View.VISIBLE);
//        } else {
//            imageView.setVisibility(View.GONE);
//        }
//    }
//
//    public void setOfficeList(ArrayList<Office> officeListNet) {
//        String localOfficeIds = PreferencesUtils.getString(
//                MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent() + userId
//                        + "allCommonOfficeIds");
//        LogUtils.debug("jason", "localOfficeIds=" + localOfficeIds);
//        if (StringUtils.isBlank(localOfficeIds)) {
//            originCommonOfficeList = officeListNet;
//        } else {
//            List<String> allOfficeIdList = JSONUtils.JSONArray2List(localOfficeIds, new ArrayList<String>());
//            List<Office> allOfficeList = new ArrayList<Office>();
//            for (int i = 0; i < allOfficeIdList.size(); i++) {
//                String officeId = allOfficeIdList.get(i);
//                LogUtils.debug("jason", "officeId=" + officeId);
//                Iterator<Office> iter = officeListNet.iterator();
//                while (iter.hasNext()) {
//                    Office office = iter.next();
//                    if (office.getId().equals(officeId)) {
//                        allOfficeList.add(office);
//                        iter.remove();
//                    }
//                }
//            }
//            allOfficeList.addAll(allOfficeList.size(), officeListNet);
//            originCommonOfficeList = allOfficeList;
//        }
//        commonOfficeList = originCommonOfficeList;
//        saveAllOfficeIds(true);
//    }
//
//    /**
//     * 处理点击事件，点击后如果id已选择则移除id， 没有选择则添加id，同时记录结果
//     *
//     * @param position
//     */
//    public void handleClickEvent(int position) {
//        String officeId = commonOfficeList.get(position).getId();
//        if (!selectOfficeIdList.contains(officeId)) {
//            selectOfficeIdList.add(officeId);
//            List<String> newSelectOfficeIdList = new ArrayList<String>();
//            for (int i = 0; i < originCommonOfficeList.size(); i++) {
//                String id = originCommonOfficeList.get(i).getId();
//                if (selectOfficeIdList.contains(id)) {
//                    newSelectOfficeIdList.add(id);
//                }
//            }
//            selectOfficeIdList = newSelectOfficeIdList;
//        } else {
//            selectOfficeIdList.remove(officeId);
//        }
//        isOfficeChange = true;
//        myCommonOfficeAdapter.notifyDataSetChanged();
//        PreferencesUtils.putString(MyCommonOfficeActivity.this, MyApplication.getInstance().getTanent()
//                        + userId + "selectCommonOfficeIds",
//                JSONUtils.toJSONString(selectOfficeIdList));
//    }
//
//    /**
//     * 显示对号
//     *
//     * @param imageView
//     * @param position
//     */
//    public void renderSelectImg(ImageView imageView, int position) {
//        for (int i = 0; i < selectOfficeIdList.size(); i++) {
//            LogUtils.debug("jason", "selectOfficeIdList" + i + "=" + selectOfficeIdList.get(i));
//        }
//        LogUtils.debug("jason", "id=" + commonOfficeList.get(position)
//                .getId());
//        if (isOrdering) {
//
//            imageView.setVisibility(View.GONE);
//        } else if (selectOfficeIdList.contains(commonOfficeList.get(position)
//                .getId())) {
//            imageView.setVisibility(View.VISIBLE);
//        } else {
//            imageView.setVisibility(View.GONE);
//        }
//    }
//
//    /**
//     * 创建DragSortController，并设置其属性
//     *
//     * @param dslv
//     * @return
//     */
//    public DragSortController buildController(DragSortListView dslv) {
//        DragSortController controller = new DragSortController(dslv);
//        controller.setDragHandleId(R.id.my_common_office_handle);
//        controller.setSortEnabled(true);
//        controller.setDragInitMode(0);
//        return controller;
//    }
//
//    /**
//     * 我的常用办公地点
//     */
//    class MyCommonOfficeAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return commonOfficeList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            convertView = LayoutInflater.from(MyCommonOfficeActivity.this)
//                    .inflate(R.layout.my_common_office_item, null);
//            ((TextView) convertView.findViewById(R.id.my_common_office_floor))
//                    .setText(commonOfficeList.get(position).getOfficeBuilding().getName());
//            ((TextView) convertView
//                    .findViewById(R.id.my_common_office_building))
//                    .setText(commonOfficeList.get(position).getName());
//            ImageView handleImg = (ImageView) convertView
//                    .findViewById(R.id.my_common_office_handle);
//            ImageView selectImage = (ImageView) convertView
//                    .findViewById(R.id.my_common_office_selected_img);
//            renderHandleImg(handleImg);
//            renderSelectImg(selectImage, position);
//            return convertView;
//        }
//
//    }
//
//    /**
//     * ListView点击事件
//     */
//    class OnItemSelectClickListener implements OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position,
//                                long id) {
//            if (!isOrdering) {
//                handleClickEvent(position);
//            }
//
//        }
//    }
//
//    /**
//     * 会议返回结果
//     */
//    class WebService extends APIInterfaceInstance {
////        @Override
////        public void returnOfficeListResultSuccess(GetOfficeResult getOfficeResult) {
////
////            if (loadingDialog != null && loadingDialog.isShowing()) {
////                loadingDialog.dismiss();
////            }
////            setOfficeList(getOfficeResult.getOfficeList());
////            myCommonOfficeAdapter = new MyCommonOfficeAdapter();
////            dragSortListView.setAdapter(myCommonOfficeAdapter);
////            myCommonOfficeAdapter.notifyDataSetChanged();
////        }
////
////        @Override
////        public void returnOfficeListResultFail(String error, int errorCode) {
////            if (loadingDialog != null && loadingDialog.isShowing()) {
////                loadingDialog.dismiss();
////            }
////            WebServiceMiddleUtils.hand(MyCommonOfficeActivity.this, error, errorCode);
////        }
//
//        @Override
//        public void returnDeleteOfficeSuccess(int position) {
//            super.returnDeleteOfficeSuccess(position);
//            if (loadingDialog != null && loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            originCommonOfficeList.remove(position);
//            saveAllOfficeIds(false);
//            isOfficeChange = true;
//            myCommonOfficeAdapter.notifyDataSetChanged();
//        }
//
//        @Override
//        public void returnDeleteOfficeFail(String error, int errorCode) {
//            if (loadingDialog != null && loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            WebServiceMiddleUtils.hand(MyCommonOfficeActivity.this, error, errorCode);
//        }
//    }
//
//    /**
//     * 长按事件处理
//     */
//    class OnMyOfficeLongClickListener implements OnItemLongClickListener {
//
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                       final int position, long id) {
//            new MyQMUIDialog.MessageDialogBuilder(MyCommonOfficeActivity.this)
//                    .setMessage(R.string.meeting_office_delete_position)
//                    .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
//                        @Override
//                        public void onClick(QMUIDialog dialog, int index) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
//                        @Override
//                        public void onClick(QMUIDialog dialog, int index) {
//                            dialog.dismiss();
//                            deleteOffice(position);
//                            myCommonOfficeAdapter.notifyDataSetChanged();
//                        }
//                    })
//                    .show();
//            return true;
//        }
//
//        /**
//         * 删除常用办公地点
//         *
//         * @param position
//         */
//        protected void deleteOffice(int position) {
//            if (NetUtils.isNetworkConnected(MyCommonOfficeActivity.this)) {
//                loadingDialog.show();
//                apiService.deleteOffice(originCommonOfficeList.get(position)
//                        .getId(), position);
//            }
//        }
//
//    }

}
