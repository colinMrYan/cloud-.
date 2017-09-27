package com.inspur.emmcloud.ui.work.meeting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

public class LocationActivity extends BaseActivity{

	String building,roomName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meeting_location);

		roomName = getIntent().getStringExtra("room");
		
		if(!TextUtils.isEmpty(roomName)){
			((TextView)(findViewById(R.id.loc_content_text))).setText(roomName.split(" ")[1]);
			((TextView)(findViewById(R.id.loc_buildingname_text))).setText(roomName.split(" ")[0]);
		}
		
		
	}
	
	public void onClick(View v){
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.meeting_location_name_layout:
			showDialog();
			break;
		case R.id.location_layout:
			intent.setClass(LocationActivity.this, BuildingActivity.class);
			intent.putExtra("building", roomName.split(" ")[0]);
			startActivity(intent);
			break;
		

		default:
			break;
		}
	}
	
	private void showDialog() {
		View view = getLayoutInflater().inflate(
				R.layout.meeting_choose_dialog, null);
		final Dialog dialog = new Dialog(this,
				R.style.transparentFrameWindowStyle);
		dialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = LayoutParams.MATCH_PARENT;
		wl.height = LayoutParams.WRAP_CONTENT;

		Button chooseBtn = (Button) view.findViewById(R.id.location_choose_btn);
		Button detailBtn = (Button) view.findViewById(R.id.location_detail_btn);
		Button cancelBtn = (Button) view.findViewById(R.id.location_detail_btn);
		chooseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				String room = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getName();
//				String roomid = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getMeettingid();
//
//				String roomFlour = meettingAreas.get(groupPosition).getName();
//				Intent intent = new Intent();
//				intent.putExtra("room", roomFlour + " " + room);
//				intent.putExtra("roomid", roomid);
//				setResult(RESULT_OK, intent);
//				finish();
				dialog.dismiss();
			}
		});

		detailBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Toast.makeText(MeettingRoomListActivity.this, "暂未支持",
				// Toast.LENGTH_SHORT).show();
//				Intent intent = new Intent();
//				String bid = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getMeettingid();
//
//				String roomname = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getName();
//				String equips[] = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getEquipment();
//				int meettingMember = meettingAreas.get(groupPosition)
//						.getMeettingRooms().get(childPosition).getGalleryful();
//				String shortname = meettingAreas.get(groupPosition).getName();
//				intent.setClass(MeettingRoomListActivity.this,
//						MeettingsOfRoomActivity.class);
//				String equip = "";
//				for (int i = 0; i < equips.length; i++) {
//					equip = equip + equips[i];
//					if (i < equips.length - 1) {
//						equip = equip + ":";
//					}
//				}
//				intent.putExtra("roomname", roomname);
//				intent.putExtra("equips", equip);
//				intent.putExtra("meettingMember", meettingMember);
//				intent.putExtra("shortname", shortname);
//				intent.putExtra("bid", bid);
//				startActivity(intent);
//				// if(NetUtils.isNetworkConnected(MeettingRoomListActivity.this)){
//				// apiService.getRoomMeetttingList(bid);
//				// }

				dialog.dismiss();
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
}
