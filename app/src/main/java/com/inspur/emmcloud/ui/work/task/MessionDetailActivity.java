package com.inspur.emmcloud.ui.work.task;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.Attachment;
import com.inspur.emmcloud.bean.GetFileUploadResult;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.SearchModel;
import com.inspur.emmcloud.bean.TaskColorTag;
import com.inspur.emmcloud.bean.TaskList;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.EditTextUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.SendFileUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TagColorUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.HorizontalProgressBarWithNumber;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;
import com.inspur.emmcloud.widget.SegmentControl;
import com.inspur.emmcloud.widget.SegmentControl.OnSegmentControlClickListener;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback.ProgressCallback;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessionDetailActivity extends BaseActivity {

	private static final int TAG_TYPE = 0;
	private static final int MANAGER = 1;
	private static final int MEMBER = 2;
	private static final int LINK = 3;
	private static final int UPLOAD_FILE = 4;
	private EditText messionNameEdit;
	private SegmentControl segmentControl;
	private int segmentIndex = 1;
	private TextView messionEndTime;
	private TextView messionStatus;
//	private ImageView typeImg;// 一个默认的类别图片
	private TextView memberText;
	private ImageDisplayUtils imageDisplayUtils;
	private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
	private List<SearchModel> addMemList = new ArrayList<SearchModel>();
	private List<SearchModel> deleteMemList = new ArrayList<SearchModel>();
	private List<SearchModel> oldMemList = new ArrayList<SearchModel>();
	private TextView managerText;
	private TaskResult task;
	private List<TaskColorTag> tagList;
	private Calendar dueDate;
	private WorkAPIService apiService;
	private LoadingDialog loadingDlg;

	private List<Attachment> attachments = new ArrayList<Attachment>();
	private ImageView[] tagImgs = new ImageView[3];
	private Handler handler;
	private AttachmentGridAdapter attachmentAdapter;

	private int attachDeletePosition = -1;
	private boolean isRefreshList = false;// 判断页面数据是否修改让taskList能够刷新
	private boolean isCanModify = true;// 已关注的任务不能进行修改

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mession_detail);
		initViews();
	}

	/**
	 * 初始化
	 */
	private void initViews() {
		apiService = new WorkAPIService(MessionDetailActivity.this);
		apiService.setAPIInterface(new WebService());
		handleMessage();
		initTask();
		initUI();
		getTasks();
		EditTextUtils.setText(messionNameEdit, task.getTitle());
		handleState();
		setSegmentControlIndex();
		handleTags();
		handleManager();
		handleDeadline();
		imageDisplayUtils = new ImageDisplayUtils(R.drawable.icon_default_photo);
	}

	/**
	 * 设置segment的Index
	 */
	private void setSegmentControlIndex() {
		segmentIndex = (2 - task.getPriority());
		segmentControl.setCurrentIndex(segmentIndex);
	}

	/**
	 * 初始化部分task数据
	 */
	private void initTask() {
		task = (TaskResult) getIntent().getExtras().getSerializable("task");
		attachments = task.getAttachments();
	}

	/**
	 * 处理截止时间
	 */
	private void handleDeadline() {
		dueDate = task.getDueDate();
		if (dueDate != null) {
			messionEndTime.setText(dueDate.get(Calendar.YEAR) + "-"
					+ (dueDate.get(Calendar.MONTH) + 1) + "-"
					+ dueDate.get(Calendar.DAY_OF_MONTH));
		} else {
			messionEndTime.setText("");
		}
	}

	/**
	 * 处理负责人显示
	 */
	private void handleManager() {
		String ownerUid = task.getOwner();
		String ownerName = ContactCacheUtils.getUserName(
				getApplicationContext(), ownerUid);
		String masterId = task.getMaster();
		String masterName = ContactCacheUtils.getUserName(
				getApplicationContext(), masterId);
		if (!StringUtils.isEmpty(masterId) && !masterId.contains("null")) {
			managerText.setText(masterName);
		} else if (!StringUtils.isEmpty(ownerUid)) {
			managerText.setText(ownerName);
		} else {
			managerText.setText(getString(R.string.mession_no_person_charge));
		}
	}

	/**
	 * 处理tags的显示
	 */
	private void handleTags() {
		tagList = task.getTags();
		if(tagList.size()>0){
			int tagSize = 0;
			if (tagList.size() > 3) {
				tagSize = 3;
			} else {
				tagSize = tagList.size();
			}
			for (int i = 0; i < tagSize; i++) {
				tagImgs[i].setVisibility(View.VISIBLE);
				TagColorUtils.setTagColorImg(tagImgs[i], tagList.get(i)
						.getColor());
			}
		}
		
//		if (tagList == null || tagList.size() == 0) {
////			typeImg.setVisibility(View.GONE);
//		} else {
//			int tagSize = 0;
//			if (tagList.size() > 3) {
//				tagSize = 3;
//			} else {
//				tagSize = tagList.size();
//			}
//			for (int i = 0; i < tagSize; i++) {
//				tagImgs[i].setVisibility(View.VISIBLE);
//				TagColorUtils.setTagColorImg(tagImgs[i], tagList.get(i)
//						.getColor());
//			}
//		}
	}

	/**
	 * 把状态转化为容易理解的文字
	 */
	private void handleState() {
		String taskState = task.getState();
		if (taskState.contains("ACTIVED") || taskState.contains("PENDING")) {
			messionStatus.setText(getString(R.string.mession_detial_doing));
		} else if (taskState.contains("REMOVED")
				|| taskState.contains("SUSPENDED")) {
			messionStatus.setText(getString(R.string.mession_detial_finish));
		}
	}

	/**
	 * 获取任务
	 */
	private void getTasks() {
		TaskList taskList = task.getSubject();
		if (NetUtils.isNetworkConnected(MessionDetailActivity.this)
				&& taskList != null) {
			loadingDlg.show();
			apiService.getTask(taskList.getId());
		}
	}

	/**
	 * 处理添加附件后返回的消息
	 */
	private void handleMessage() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONObject jsonAttachment = organizeAttachment(msg);
				addAttachMents(jsonAttachment);
			}
		};
	}

	/**
	 * 组织附件数据
	 * 
	 * @param msg
	 * @return
	 */
	protected JSONObject organizeAttachment(Message msg) {
		// 返回的数据格式
		// {"key":"CI9IPKWGIXJ.jpg","name":"IMG_20160510_095849.jpg","size":1444440,"type":"Photos"}
		JSONObject jsonAttachment = new JSONObject();
		String type = "", extName = "",category = "",name = "",key = "";
		name = JSONUtils.getString((String)msg.obj, "name", "");
		key = JSONUtils.getString((String)msg.obj, "key", "");
		extName = FileUtils.getExtensionName(name);
		type = extName;
		if (type.equals("jpg") || type.equals("png")) {
			type = "JPEG";
			category = "IMAGE";
		} else if (type.equals("doc") || type.equals("docx")) {
			type = "MS_WORD";
			category = "DOCUMENT";
		} else if (type.equals("xls") || type.equals("xlsx")) {
			type = "MS_EXCEL";
			category = "DOCUMENT";
		} else if (type.equals("ppt") || type.equals("pptx")) {
			type = "MS_PPT";
			category = "DOCUMENT";
		} else if (type.equals("txt")) {
			type = "TEXT";
			category = "DOCUMENT";
		} else {
			type = "TEXT";
			category = "DOCUMENT";
		}
		try {
			jsonAttachment.put("name", name);
			jsonAttachment.put("uri", key);
			jsonAttachment.put("category", category);
			jsonAttachment.put("type", type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonAttachment;
	}

	/**
	 * 添加附件
	 */
	protected void addAttachMents(JSONObject jsonAttachment) {
		if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
			loadingDlg.show();
			apiService.addAttachments(task.getId(), jsonAttachment.toString());
		}
	}

	/**
	 * 初始化UI界面上的一些控件
	 */
	private void initUI() {
		int tabIndex = -1;
		messionNameEdit = (EditText) findViewById(R.id.mession_name_edit);
		if (getIntent().hasExtra("tabIndex")) {
			tabIndex = (Integer) getIntent().getExtras().getSerializable(
					"tabIndex");
		}
		if (tabIndex == 2) {
			isCanModify = false;
			messionNameEdit.setFocusable(false);
			messionNameEdit.setFocusableInTouchMode(false);
		}
		segmentControl = (SegmentControl) findViewById(R.id.segment_control);
//		typeImg = (ImageView) findViewById(R.id.mession_typecolor_img);
		messionEndTime = (TextView) findViewById(R.id.mession_endtime_text);
		messionStatus = (TextView) findViewById(R.id.mession_state_text);
		tagImgs[0] = (ImageView) findViewById(R.id.mession_typecolor_img);
		tagImgs[1] = (ImageView) findViewById(R.id.mession_typecolor_img2);
		tagImgs[2] = (ImageView) findViewById(R.id.mession_typecolor_img3);
		memberText = (TextView) findViewById(R.id.mession_allmembers_text);
		managerText = (TextView) findViewById(R.id.mession_managerm_text);
		if (!isCanModify) {
			segmentControl.setIsEnable(false);
		}
		segmentControl
				.setOnSegmentControlClickListener(new OnSegmentControlClickListener() {
					@Override
					public void onSegmentControlClick(int index) {
						segmentIndex = index;
					}
				});
		loadingDlg = new LoadingDialog(this);
		initAttachments();
	}

	/**
	 * 附件展示，及监听逻辑设置
	 */
	public void initAttachments() {
		GridView attachmentGridView = (GridView) findViewById(R.id.mession_file_grid);
		attachmentAdapter = new AttachmentGridAdapter();
		attachmentGridView.setAdapter(attachmentAdapter);
		attachmentGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String ownerUid = task.getOwner();
				String userID = ((MyApplication) getApplication()).getUid();
				if (ownerUid.contains(userID)) {
					if (position == attachments.size()) {
						openFileSystem();
					} else {
						downLoadFile(view, position);
					}
				} else{
					if(attachments.size()==0 ||((attachments.size() >0 )&&(position == attachments.size()))){
						ToastUtils.show(MessionDetailActivity.this,
								getString(R.string.mession_upload_attachment));
					}
				}
			}
		});
		attachmentGridView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, final int position, long id) {
						DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == -1) {
									deleteAttachments(position);
									attachDeletePosition = position;
								} else {
									dialog.dismiss();
								}
							}
						};
						String ownerUid = task.getOwner();
						String userID = ((MyApplication) getApplication()).getUid();
						if (ownerUid.contains(userID)) {
							if(position != attachments.size()){
								EasyDialog.showDialog(MessionDetailActivity.this,
										getString(R.string.prompt),
										getString(R.string.mession_delete_atachment),
										getString(R.string.ok),
										getString(R.string.cancel), listener, true);
							}
						}else {
							if(position != attachments.size()){
								ToastUtils.show(MessionDetailActivity.this,
									getString(R.string.mession_delete_attachment));
							}
						}
						return true;
					}
				});

	}

	/**
	 * 打开文件系统
	 */
	protected void openFileSystem() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(Intent.createChooser(intent,
				getString(R.string.file_upload_tips)), UPLOAD_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtils.jasonDebug("0000000000000000000022");
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case TAG_TYPE:
				handleMessionTags(data);
				break;
			case MANAGER:
				handleManagerChange(data);
				break;
			case MEMBER:
				handleMemberChange(data);
				break;
			case LINK:
				break;
			case UPLOAD_FILE:
				ChatAPIService apiService = new ChatAPIService(MessionDetailActivity.this);
				apiService.setAPIInterface(new WebService());
				SendFileUtils.sendFileMsg(MessionDetailActivity.this, data,
						apiService, loadingDlg);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 邀请的参与人员修改
	 * 
	 * @param data
	 */
	private void handleMemberChange(Intent data) {
		if (data.getExtras().containsKey("selectMemList")) {
			selectMemList = (List<SearchModel>) data.getExtras()
					.getSerializable("selectMemList");
			addMemList = (List<SearchModel>) data.getExtras().getSerializable(
					"selectMemList");
			deleteMemList.removeAll(addMemList);
			addMemList.removeAll(oldMemList);
			inviteMatesForTask();
			deleteMemList = (List<SearchModel>) data.getExtras()
					.getSerializable("selectMemList");
			oldMemList = (List<SearchModel>) data.getExtras().getSerializable(
					"selectMemList");
		}
	}

	/**
	 * Manager变化时处理逻辑
	 * 
	 * @param data
	 */
	private void handleManagerChange(Intent data) {
		List<SearchModel> managerList = (List<SearchModel>) data.getExtras()
				.getSerializable("selectMemList");
		if (managerList == null || managerList.size() == 0) {
			return;
		}
		SearchModel searchModel = managerList.get(0);
		final String managerName = searchModel.getName();
		final String managerID = searchModel.getId();
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == -1) {
					changeMessionOwner(managerID);
					managerText.setText(managerName);
				} else {
					dialog.dismiss();
				}
			}
		};
		EasyDialog.showDialog(MessionDetailActivity.this,
				getString(R.string.prompt),
				getString(R.string.mession_change_charge_person),
				getString(R.string.ok), getString(R.string.cancel), listener,
				true);
	}

	/**
	 * tag变化的逻辑
	 * 
	 * @param data
	 */
	private void handleMessionTags(Intent data) {
		if (data == null) {
			return;
		}
		ArrayList<TaskColorTag> addTags = (ArrayList<TaskColorTag>) data
				.getSerializableExtra("tag");
		int tagNumber = 0;
		if (addTags.size() > 3) {
			tagNumber = 3;
		} else {
			tagNumber = addTags.size();
		}
		for (int i = 0; i < 3; i++) {
			tagImgs[i].setVisibility(View.GONE);
		}
		for (int i = 0; i < tagNumber; i++) {
			tagImgs[i].setVisibility(View.VISIBLE);
			TagColorUtils.setTagColorImg(tagImgs[i], addTags.get(i).getColor());
		}
		tagList.clear();
		tagList.addAll(addTags);
		task.setTags(tagList);
	}

	/**
	 * 负责人变更
	 * 
	 * @param managerID
	 */
	protected void changeMessionOwner(String managerID) {
		if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
			loadingDlg.show();
			apiService.changeMessionOwner(task.getId(), managerID);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (isRefreshList) {
			setResult(RESULT_OK);
		}
		finish();
	}

	/**
	 * 附件
	 */
	class AttachmentGridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return attachments.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Holder holder = null;
			if (null == convertView) {
				holder = new Holder();
				LayoutInflater mInflater = LayoutInflater
						.from(MessionDetailActivity.this);
				convertView = mInflater.inflate(R.layout.gridview_item, null);
				holder.attachmentImg = (ImageView) convertView
						.findViewById(R.id.btn_gv_item);
				holder.attachmentImg.setFocusable(false);
				convertView.setTag(holder);
				holder.textView = (TextView) convertView
						.findViewById(R.id.file_text);
			} else {
				holder = (Holder) convertView.getTag();
			}
			if (position < attachments.size()) {
				displayAttachments(position, holder.attachmentImg);
				holder.textView.setText(attachments.get(position).getName());
			} else if (position == attachments.size()) {
				imageDisplayUtils.displayImage(holder.attachmentImg, "drawable://"
						+ R.drawable.icon_member_add);
				holder.textView.setText(getString(R.string.add));
			}
			return convertView;
		}

	}

	/**
	 * 删除附件
	 * 
	 * @param position
	 */
	private void deleteAttachments(int position) {
		loadingDlg.show();
		apiService.deleteAttachments(task.getId(), attachments.get(position)
				.getId());
	}

	/**
	 * 附件显示逻辑
	 * 
	 * @param position
	 * @param attachmentImg
	 */
	public void displayAttachments(int position, ImageView attachmentImg) {
		String displayImg = "drawable://";
		if (attachments.get(position).getType().equals("JPEG")) {
			displayImg = displayImg + R.drawable.icon_file_photos;
		} else if (attachments.get(position).getType().equals("MS_WORD")) {
			displayImg = displayImg + R.drawable.icon_file_word;
		} else if (attachments.get(position).getType().equals("MS_EXCEL")) {
			displayImg = displayImg + R.drawable.icon_file_excel;
		} else if (attachments.get(position).getType().equals("MS_PPT")) {
			displayImg = displayImg + R.drawable.icon_file_ppt;
		} else if (attachments.get(position).getType().equals("TEXT")) {
			displayImg = displayImg + R.drawable.icon_file_unknown;
		} else {
			displayImg = displayImg + R.drawable.icon_file_unknown;
		}
		imageDisplayUtils.displayImage(attachmentImg, displayImg);
	}

	private static class Holder {
		ImageView attachmentImg;
		TextView textView;
	}

	/**
	 * 展示邀请的同伴
	 */
	private void displayInviteMates() {
		String memebers = "";
		for (int i = 0; i < selectMemList.size(); i++) {
			memebers = memebers + selectMemList.get(i).getName() + " ";
		}
		memberText.setText("");
		memberText.setText(memebers);
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back_layout:
			if (isRefreshList) {
				setResult(RESULT_OK);
			}
			finish();
			break;
		case R.id.mession_type_layout:
			addTags(intent);
			break;
		case R.id.mession_manager_layout:
			String ownerUid = task.getOwner();
			String myUid = ((MyApplication) getApplication()).getUid();
			if (!ownerUid.contains(myUid)) {
				Toast.makeText(MessionDetailActivity.this,
						getString(R.string.mession_not_change_charge_person),
						Toast.LENGTH_SHORT).show();
				break;
			}
			changeMessionManager();
			break;
		case R.id.mession_members_layout:
			inviteMembers();
			break;
		case R.id.mession_endtime_layout:
			handleDeadlineTimeDialog();
			break;
		case R.id.mession_state_layout:
			break;
		case R.id.mession_link_layout:
			break;
		case R.id.save_btn:
			saveTask();
			break;
		default:
			break;
		}
	}

	/**
	 * 变更任务负责人
	 */
	private void changeMessionManager() {
		if (isCanModify) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(),
					ContactSearchActivity.class);
			intent.putExtra("select_content", 2);
			intent.putExtra("isMulti_select", false);
			intent.putExtra("title",
					getString(R.string.mession_charge_person));
			startActivityForResult(intent, MANAGER);
		}
	}

	/**
	 * 保存任务
	 */
	private void saveTask() {
		if (isCanModify) {
			String title = messionNameEdit.getText().toString();
			int priority = 2 - segmentIndex;
			task.setTitle(title);
			task.setPriority(priority);
			// task.setAttachments(null);
			if (dueDate != null) {
				task.setDueDate(TimeUtils.localCalendar2UTCCalendar(dueDate));
			}
			String taskJson = JSON.toJSONString(task);
			Log.d("jason", "taskJson=" + taskJson);
			updateTask(taskJson);
		}
	}

	/**
	 * 添加tag
	 * 
	 * @param intent
	 */
	private void addTags(Intent intent) {
		if (isCanModify) {
			intent.setClass(MessionDetailActivity.this,
					MessionTagsManageActivity.class);
			intent.putExtra("from", "mession");
			intent.putExtra("tag", (ArrayList<TaskColorTag>) task.getTags());
			startActivityForResult(intent, TAG_TYPE);
		}

	}

	/**
	 * 邀请任务参与成员
	 */
	private void inviteMembers() {
		if (isCanModify) {
			Intent intentMem = new Intent();
			intentMem.putExtra("select_content", 2);
			intentMem.putExtra("isMulti_select", true);
			intentMem.putExtra("title",
					getString(R.string.mession_invate_members));
			if (selectMemList != null) {
				intentMem.putExtra("hasSearchResult",
						(Serializable) selectMemList);
			}
			intentMem.setClass(getApplicationContext(),
					ContactSearchActivity.class);
			startActivityForResult(intentMem, MEMBER);
		}
	}

	/**
	 * 处理截止时间Dialog
	 */
	private void handleDeadlineTimeDialog() {
		if (isCanModify) {
			Calendar cal;
			if (dueDate != null) {
				cal = dueDate;
			} else {
				cal = Calendar.getInstance();
			}
			Locale locale = getResources().getConfiguration().locale;
			Locale.setDefault(locale);
			MyDatePickerDialog dialog = new MyDatePickerDialog(
					MessionDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							messionEndTime.setText(year + "-"
									+ (monthOfYear + 1) + "-" + dayOfMonth);
							dueDate = Calendar.getInstance();
							dueDate.set(Calendar.YEAR, year);
							dueDate.set(Calendar.MONTH, monthOfYear);
							dueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							task.setDueDate(dueDate);
						}
					}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			dialog.show();
		}
	}

	/**
	 * 更新任务
	 */
	private void updateTask(String task) {
		if (NetUtils.isNetworkConnected(MessionDetailActivity.this)) {
			loadingDlg.show();
			apiService.updateTask(task);
		}
	}

	/**
	 * 邀请其他人协作，同时包含增减人员
	 */
	private void inviteMatesForTask() {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			JSONArray addMembers = new JSONArray();
			JSONArray delMembers = new JSONArray();
			for (int i = 0; i < addMemList.size(); i++) {
				addMembers.put(addMemList.get(i).getId());
			}
			for (int i = 0; i < deleteMemList.size(); i++) {
				delMembers.put(deleteMemList.get(i).getId());
			}
			if (addMembers.length() > 0) {
				if (!loadingDlg.isShowing()) {
					loadingDlg.show();
				}
				apiService.inviteMateForTask(task.getId(), addMembers);
			}
			if (delMembers.length() > 0) {
				if (!loadingDlg.isShowing()) {
					loadingDlg.show();
				}
				apiService.deleteMateForTask(task.getId(), delMembers);
			}
		}

	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnInviteMateForTaskSuccess(String subject) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			isRefreshList = true;
			task.setSubject(new TaskList(subject));
			displayInviteMates();
		}

		@Override
		public void returnInviteMateForTaskFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnUpdateTaskSuccess() {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			Intent mIntent = new Intent("com.inspur.task");
			mIntent.putExtra("refreshTask", "refreshTask");
			sendBroadcast(mIntent);
			ToastUtils.show(getApplicationContext(),
					getString(R.string.mession_saving_success));
			setResult(RESULT_OK);
		}

		@Override
		public void returnUpdateTaskFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnFileUpLoadSuccess(
				GetFileUploadResult getFileUploadResult, String fakeMessageId) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			isRefreshList = true;
			Message msg = new Message();
			msg.what = 5;
			msg.obj = getFileUploadResult.getFileMsgBody();
			handler.sendMessage(msg);
		}

		@Override
		public void returnFileUpLoadFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnAttachmentSuccess(TaskResult taskResult) {
			super.returnAttachmentSuccess(taskResult);
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			attachments = taskResult.getAttachments();
			task.setAttachments(attachments);
			initAttachments();
			ToastUtils.show(MessionDetailActivity.this,
					getString(R.string.mession_upload_attachment_success));
		}

		@Override
		public void returnAttachmentFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnAddAttachMentSuccess(Attachment attachment) {
			super.returnAddAttachMentSuccess(attachment);
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
				apiService.getSigleTask(task.getId());
			}
			isRefreshList = true;
		}

		@Override
		public void returnAddAttachMentFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {
			super.returnGetTasksSuccess(getTaskListResult);
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			ArrayList<String> memebersIds = new ArrayList<String>();
			memebersIds = handleTaskSearhMembers(getTaskListResult);
			String memebers = "";
			int memimg = memebersIds.size();
			if (memimg > 4) {
				memimg = 4;
			}
			for (int i = 0; i < memimg; i++) {
				// 去掉显示头像逻辑改为显示名字
				// String inspurID = ContactCacheUtils.getUserInspurID(
				// getApplicationContext(), memids.get(i));
				//
				// membersImg[i].setVisibility(View.VISIBLE);
				// imageDisplayUtils.displayImage(membersImg[i],
				// UriUtils.getChannelImgUri(inspurID));
				memebers = memebers
						+ ContactCacheUtils.getUserName(
								getApplicationContext(), memebersIds.get(i))
						+ " ";
			}
			memberText.setText(memebers);
		}

		@Override
		public void returnGetTasksFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnDelTaskMemSuccess() {
			super.returnDelTaskMemSuccess();
			isRefreshList = true;
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			displayInviteMates();
		}

		@Override
		public void returnDelTaskMemFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnDelAttachmentSuccess() {
			super.returnDelAttachmentSuccess();
			isRefreshList = true;
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			attachments.remove(attachDeletePosition);
			task.setAttachments(attachments);
			attachmentAdapter.notifyDataSetChanged();
		}

		@Override
		public void returnDelAttachmentFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnChangeMessionOwnerSuccess() {
			super.returnChangeMessionOwnerSuccess();
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			// 这里这样写的原因是修改完负责人，需要跳转到我关注的任务，如果从任务列表进入则可以直接finish
			// 如果从工作进入则不可以直接finish
			Intent intent = new Intent();
			intent.setClass(MessionDetailActivity.this,
					MessionListActivity.class);
			intent.putExtra("index", 2);
			startActivity(intent);
			finish();
		}

		@Override
		public void returnChangeMessionOwnerFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

		@Override
		public void returnChangeMessionTagSuccess() {
			super.returnChangeMessionTagSuccess();
			isRefreshList = true;
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
		}

		@Override
		public void returnChangeMessionTagFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionDetailActivity.this, error,errorCode);
		}

	}

	/**
	 * 下载文件
	 * 
	 * @param convertView
	 * @param position
	 */
	private void downLoadFile(View convertView, int position) {
		if (!(position < attachments.size())) {
			return;
		}
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File dir = new File(MyAppConfig.LOCAL_DOWNLOAD_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String fileName = attachments.get(position).getName();
			final String fileUri = attachments.get(position).getUri();
			final String target = MyAppConfig.LOCAL_DOWNLOAD_PATH
					+ fileName;
			final String downlaodSource = UriUtils.getPreviewUri(fileUri);
			final HorizontalProgressBarWithNumber fileProgressbar = (HorizontalProgressBarWithNumber) convertView
					.findViewById(R.id.filecard_progressbar);
			fileProgressbar.setTag(target);
			fileProgressbar.setVisibility(View.VISIBLE);
			// 当文件正在下载中 点击不响应
			if ((0 < fileProgressbar.getProgress())
					&& (fileProgressbar.getProgress() < 100)) {
				return;
			}
			
			ProgressCallback<File> progressCallback = new ProgressCallback<File>() {

				@Override
				public void onCancelled(CancelledException arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onError(Throwable arg0, boolean arg1) {
					fileProgressbar.setVisibility(View.GONE);
					ToastUtils.show(MessionDetailActivity.this,
							getString(R.string.download_fail));
				}

				@Override
				public void onFinished() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSuccess(File arg0) {
					// TODO Auto-generated method stub
					fileProgressbar.setVisibility(View.GONE);
					ToastUtils.show(MessionDetailActivity.this,
							getString(R.string.download_success));
				}

				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					if (total == 0) {
						total = 1;
					}
					int progress = (int) ((current * 100) / total);
					if (!(fileProgressbar.getVisibility() == View.VISIBLE)) {
						fileProgressbar.setVisibility(View.VISIBLE);
					}
					fileProgressbar.setProgress(progress);
					fileProgressbar.refreshDrawableState();
				}

				@Override
				public void onStarted() {
					// TODO Auto-generated method stub
					if ((fileProgressbar.getTag() != null)
							&& (fileProgressbar.getTag() == target)) {
						fileProgressbar.setVisibility(View.VISIBLE);
					} else {
						fileProgressbar.setVisibility(View.GONE);
					}
				}

				@Override
				public void onWaiting() {
					// TODO Auto-generated method stub
					
				}
			};
			
			if (FileUtils.isFileExist(fileUri)) {
				fileProgressbar.setVisibility(View.INVISIBLE);
				FileUtils.openFile(MessionDetailActivity.this, fileUri);
			}else if (FileUtils.isFileExist(target)) {
				fileProgressbar.setVisibility(View.INVISIBLE);
				FileUtils.openFile(MessionDetailActivity.this, target);
			}else {
				new DownLoaderUtils().startDownLoad(downlaodSource, target,
						progressCallback);
			}
			
		} else {
			ToastUtils.show(getApplicationContext(),
					R.string.filetransfer_sd_not_exist);
		}

	}

	/**
	 * 筛选任务获取参与人员的list
	 * @param getTaskListResult
	 * @return
	 */
	public ArrayList<String> handleTaskSearhMembers(
			GetTaskListResult getTaskListResult) {
		ArrayList<String> membersIds = new ArrayList<String>();
		for (int i = 0; i < getTaskListResult.getTaskList().size(); i++) {
			if (getTaskListResult.getTaskList().get(i).getState()
					.contains("ACTIVED")
					|| getTaskListResult.getTaskList().get(i).getState()
							.contains("REMOVED")) {
				membersIds.add(getTaskListResult.getTaskList().get(i)
						.getMaster());
				if (!StringUtils.isBlank(getTaskListResult.getTaskList()
						.get(i).getMaster())) {
					SearchModel searchModel = new SearchModel(
							ContactCacheUtils.getUserContact(
									MessionDetailActivity.this,
									getTaskListResult.getTaskList().get(i)
											.getMaster()));
					if (searchModel != null) {
						selectMemList.add(searchModel);
						deleteMemList.add(searchModel);
						oldMemList.add(searchModel);
					}
				}

			}
		}
		return membersIds;
	}
}
