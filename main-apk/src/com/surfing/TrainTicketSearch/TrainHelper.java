package com.surfing.TrainTicketSearch;


public class TrainHelper {
	private TrainHelper() {
	}

	public static String parseNumStr(String num) {
		if (Integer.parseInt(num) >= 1 && Integer.parseInt(num) < 10) {
			num = "0" + num;
		}
		return num;
	}
	
	public static String Train_AllType = "ȫ��";
	public static String Train_DCType_Text = "����";
	public static String Train_ZType_Text = "ֱ��";
	public static String Train_TType_Text = "�ؿ�";
	public static String Train_KType_Text = "����";
	public static String Train_PKType_Text = "�տ�";
	public static String Train_PKEType_Text = "�տ�";
	public static String Train_LKType_Text = "�ٿ�";
}
