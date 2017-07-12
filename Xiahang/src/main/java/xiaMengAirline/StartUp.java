package xiaMengAirline;

import xiaMengAirline.util.InitData;

public class StartUp {

	public static void main(String[] args) {
		
		String initDatafile = "XiahangData20170705_1.xlsx";
		String fightTimeFile = "C://Users//esunnen//Desktop//飞行时间表.csv";
		
		InitData.initData(initDatafile, fightTimeFile);
		
		
		
		
		
		

	}

}
