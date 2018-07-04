package com.inspur.imp.plugin.audio;

import android.content.Intent;
import android.net.Uri;

import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class AudioService extends ImpPlugin {

    HashMap<String, AudioPlayer> players;	// Audio player object
    ArrayList<AudioPlayer> pausedForPhone;     // Audio players that were paused when phone call came in

    private String id;
    private String file;
    private String src;

    /**
     * 构造方法
     */
    public AudioService() {
        this.players = new HashMap<String, AudioPlayer>();
        this.pausedForPhone = new ArrayList<AudioPlayer>();
    }

    public void execute(String action,JSONObject paramsObject){
    	if (action.equals("openFile")) {
	            this.openFile(paramsObject);
	    }else if (action.equals("startPlaying")) {
            this.startPlaying(paramsObject);
        }
        else if (action.equals("pausePlaying")) {
            this.pausePlaying(paramsObject);
        }
        else if (action.equals("resetPlaying")) {
            this.resetPlaying(paramsObject);
        }
        else if (action.equals("stopPlaying")) {
            this.stopPlaying(paramsObject);
        }else{
			((ImpActivity)getActivity()).showImpDialog();
		}
    }

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		((ImpActivity)getActivity()).showImpDialog();
		return "";
	}

	public void onDestroy() {
        for (AudioPlayer audio : this.players.values()) {
            audio.destroy();
        }
        this.players.clear();
    }



    public Object onMessage(String id, Object data) {
        if (id.equals("telephone")) {
            if ("ringing".equals(data) || "offhook".equals(data)) {

                for (AudioPlayer audio : this.players.values()) {
                    if (audio.getState() == AudioPlayer.STATE.MEDIA_RUNNING.ordinal()) {
                        this.pausedForPhone.add(audio);
                        audio.pausePlaying();
                    }
                }
            }
            else if ("idle".equals(data)) {
                for (AudioPlayer audio : this.pausedForPhone) {
                    audio.startPlaying(null);
                }
                this.pausedForPhone.clear();
            }
        }
        return null;
    }

    /**
	 * 打开文件
	 * @param
	 */
	public void openFile(JSONObject jsonObject){
		try {
			if (!jsonObject.isNull("src"))
				src = FileHelper.stripFileProtocol(jsonObject.getString("src"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		File file = new File(src);

		if(file.exists()){
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//设置intent的Action属性
			intent.setAction(Intent.ACTION_VIEW);
			//获取文件file的MIME类型
			String type = getMIMEType(file);
			//设置intent的data和Type属性。
			intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
			//跳转
            getActivity().startActivityForResult(intent, ImpActivity.DO_NOTHING_RESULTCODE);
		}else{
			 this.jsCallback("fileError", Res.getString("file_error"));
		}
	}

	private String getMIMEType(File file) {

		String type="*/*";
		String fName = file.getName();
		//获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if(dotIndex < 0){
			return type;
		}
		/* 获取文件的后缀名 */
		String end=fName.substring(dotIndex,fName.length()).toLowerCase();
		if(end=="")return type;
		//在MIME和文件类型的匹配表中找到对应的MIME类型。
		for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
			if(end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	private final String[][] MIME_MapTable={
			//{后缀名，	MIME类型}
			{".3gp",	"video/3gpp"},
			{".apk",	"application/vnd.android.package-archive"},
			{".asf",	"video/x-ms-asf"},
			{".avi",	"video/x-msvideo"},
			{".bin",	"application/octet-stream"},
			{".bmp",  	"image/bmp"},
			{".c",	"text/plain"},
			{".class",	"application/octet-stream"},
			{".conf",	"text/plain"},
			{".cpp",	"text/plain"},
			{".doc",	"application/msword"},
			{".docx",	"application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
			{".xls",	"application/vnd.ms-excel"},
			{".xlsx",	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
			{".exe",	"application/octet-stream"},
			{".gif",	"image/gif"},
			{".gtar",	"application/x-gtar"},
			{".gz",	"application/x-gzip"},
			{".h",	"text/plain"},
			{".htm",	"text/html"},
			{".html",	"text/html"},
			{".jar",	"application/java-archive"},
			{".java",	"text/plain"},
			{".jpeg",	"image/jpeg"},
			{".jpg",	"image/jpeg"},
			{".js",	"application/x-javascript"},
			{".log",	"text/plain"},
			{".m3u",	"audio/x-mpegurl"},
			{".m4a",	"audio/mp4a-latm"},
			{".m4b",	"audio/mp4a-latm"},
			{".m4p",	"audio/mp4a-latm"},
			{".m4u",	"video/vnd.mpegurl"},
			{".m4v",	"video/x-m4v"},
			{".mov",	"video/quicktime"},
			{".mp2",	"audio/x-mpeg"},
			{".mp3",	"audio/x-mpeg"},
			{".mp4",	"video/mp4"},
			{".mpc",	"application/vnd.mpohun.certificate"},
			{".mpe",	"video/mpeg"},
			{".mpeg",	"video/mpeg"},
			{".mpg",	"video/mpeg"},
			{".mpg4",	"video/mp4"},
			{".mpga",	"audio/mpeg"},
			{".msg",	"application/vnd.ms-outlook"},
			{".ogg",	"audio/ogg"},
			{".pdf",	"application/pdf"},
			{".png",	"image/png"},
			{".pps",	"application/vnd.ms-powerpoint"},
			{".ppt",	"application/vnd.ms-powerpoint"},
			{".pptx",	"application/vnd.openxmlformats-officedocument.presentationml.presentation"},
			{".prop",	"text/plain"},
			{".rc",	"text/plain"},
			{".rmvb",	"audio/x-pn-realaudio"},
			{".rtf",	"application/rtf"},
			{".sh",	"text/plain"},
			{".tar",	"application/x-tar"},
			{".tgz",	"application/x-compressed"},
			{".txt",	"text/plain"},
			{".wav",	"audio/x-wav"},
			{".wma",	"audio/x-ms-wma"},
			{".wmv",	"audio/x-ms-wmv"},
			{".wps",	"application/vnd.ms-works"},
			{".xml",	"text/plain"},
			{".z",	"application/x-compress"},
			{".zip",	"application/x-zip-compressed"},
			{"",		"*/*"}
		};

    /**
     * 播放音乐
     * @param paramsObject
     * 			传递的参数
     */
    public void startPlaying(JSONObject paramsObject) {
    	try {
			if (!paramsObject.isNull("id"))
				id = paramsObject.getString("id");//传递的id
			if (!paramsObject.isNull("src"))
				file = FileHelper.stripFileProtocol(paramsObject.getString("src"));//传递的音乐路径
		} catch (JSONException e) {
			e.printStackTrace();
		}
        AudioPlayer audio = this.players.get(id);//根据id取得Media
        if (audio == null) {
            audio = new AudioPlayer(this, id, file);
            this.players.put(id, audio);
        }
        audio.startPlaying(file);
    }

    public void seekTo(String id, int milliseconds) {
    	AudioPlayer audio = this.players.get(id);
        if (audio != null) {
            audio.seekToPlaying(milliseconds);
        }
    }

    public void pausePlaying(JSONObject paramsObject) {
    	try {
			if (!paramsObject.isNull("id"))
				id = paramsObject.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        AudioPlayer audio = this.players.get(id);
        if (audio != null) {
            audio.pausePlaying();
        }else{
        	this.jsCallback("iAudio.errorCallback", Res.getString("can_not_pause"));
        }
    }

    public void resetPlaying(JSONObject paramsObject) {
    	try {
			if (!paramsObject.isNull("id"))
				id = paramsObject.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        AudioPlayer audio = this.players.get(id);
        if (audio != null) {
            audio.resetPlaying();
        }else{
        	this.jsCallback("iAudio.errorCallback", Res.getString("can_not_replay"));
        }
    }

    public void stopPlaying(JSONObject paramsObject) {
    	try {
			if (!paramsObject.isNull("id"))
				id = paramsObject.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        AudioPlayer audio = this.players.get(id);
        if (audio != null) {
            audio.stopPlaying();
        }else{
        	this.jsCallback("iAudio.errorCallback", Res.getString("can_not_stop"));
        }
    }
}
