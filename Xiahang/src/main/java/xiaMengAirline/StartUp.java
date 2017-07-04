package xiaMengAirline;

import xiaMengAirline.util.InitData;

public class StartUp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String initDatafile = "C://Users//esunnen//Desktop//厦航大赛数据20170627.xlsx";
		String fightTimeFile = "C://Users//esunnen//Desktop//飞行时间表.csv";
		
		InitData.initData(initDatafile, fightTimeFile);
		
		
		
		

	}

}
