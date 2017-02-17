package com.inspur.emmcloud.util;

public class ProtocolUtils {

	private static String schemebefore = "(ecm://";
	private static String schemeafter = ")";
	public static String getMentionProtoUtils(String uid){
//		return "(ecm://"+"user"+")";
		return "(ecm-contact://"+uid+")";
	}
	
	//未用上的方法
//	public static int getProtocolLength(){
//		return schemebefore.length()+schemeafter.length()+36;
//	}
}
