package xiaMengAirline.evaluator.aviation2017;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Created by peicheng on 17/6/29.
 */
public class ResultEvaluator implements Cloneable{

    //定义输入数据结构
    private Map<String, Flight> flightMap = new HashMap<>();
    private Map<String, List<Flight>> airLineMap = new HashMap<>();
    private Map<String, List<AirplaneLimitation>> airplaneLimitationMap = new HashMap<>();
    private Map<String, List<AirportClose>> airportCloseMap = new HashMap<>();
    private List<Scene> sceneList = new ArrayList<>();
    private Map<String, TravelTime> travelTimeMap = new HashMap<>();
    private Map<String, Long> flightIntervalTimeMap = new HashMap<>();
    private Set<String> domesticAirportSet = new HashSet<>();               //国内机场集合，用于控制调机
    private Map<String, String> airplaneStartAirportMap = new HashMap<>();  //飞机起始机场映射表，用于限定飞机起始机场，判断飞机是否空飞
    private Map<String, Integer> endAirportMap = new HashMap<>();           //结束机场统计，用于实现基地平衡

    //定义结果数据结构
    private Map<String, List<ResultFlight>> resultAirLineMap;
    private Map<String, ResultFlight> resultFlightMap;

    /**
     * 目标函数值 = 参数1*调机航班数 + 参数2*取消航班数 + 参数3*机型发生变化的航班数 +参数4*联程拉直航班对的个数 +参数5*航班总延误时间（小时） +参数6*航班总提前时间（小时）
     * + 参数7 * 非可行标识（0-1变量） + 参数8 * 违背约束数量 。
     */
    private int emptyFlightNum = 0;              //调机航班数
    private double cancelFlightNum = 0;          //取消航班数量
    private double flightTypeChangeNum = 0;      //机型发生变化的航班数量
    private double connectFlightStraightenNum = 0;  //联程拉直航班对的个数
    private double totalFlightDelayHours = 0.0;  //航班总延误时间（小时）
    private double totalFlightAheadHours = 0.0;  //航班总提前时间（小时）
    private boolean isFeasible = true;           //可行标识
    private int constraintViolationNum = 0;      //违背约束的次数


    public ResultEvaluator(InputStream inputStream){
        //读取输入数据
        readInputData(inputStream);
        //设置联程航班标识
        Iterator<String> iterator = airLineMap.keySet().iterator();
        while(iterator.hasNext()){
            String airplaneId = iterator.next();
            List<Flight> flightList = airLineMap.get(airplaneId);
            Collections.sort(flightList);
            airplaneStartAirportMap.put(airplaneId, flightList.get(0).getStartAirport());
            for(int index = 1; index < flightList.size(); ++ index){
                Flight preFlight = flightList.get(index - 1);
                Flight flight = flightList.get(index);
                if(preFlight.getFlightNo().equals(flight.getFlightNo())){
                    preFlight.setConnected(flight.getFlightId());
                    flight.setConnected(preFlight.getFlightId());
                }
                flightIntervalTimeMap.put(preFlight.getFlightId() + "#" + flight.getFlightId(),
                        flight.getStartDateTime().getTime() - preFlight.getEndDateTime().getTime());
            }
            String endAirport = flightList.get(flightList.size() - 1).getEndAirport();
            if(endAirportMap.containsKey(endAirport)){
                endAirportMap.put(endAirport, endAirportMap.get(endAirport) + 1);
            }
            else{
                endAirportMap.put(endAirport, 1);
            }
        }
    }

    /**
     * 读取输入数据集，将数据进行整理，方便约束检测，结果评分
     * @param inputStream
     */
    private void readInputData(InputStream inputStream){
        try {
            XSSFWorkbook workBook = new XSSFWorkbook(inputStream);

            //读取航班信息
            XSSFSheet flightSheet = workBook.getSheet("航班");
            Iterator<Row> rowIterator = flightSheet.iterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                if(row.getRowNum() == 0)
                    continue;
                Flight flight = new Flight(row);
                flightMap.put(flight.getFlightId(), flight);
                if(airLineMap.containsKey(flight.getAirplaneId())){
                    airLineMap.get(flight.getAirplaneId()).add(flight);
                }
                else {
                    List<Flight> flightList = new ArrayList<>();
                    flightList.add(flight);
                    airLineMap.put(flight.getAirplaneId(), flightList);
                }
                if(flight.isDomestic()){
                    domesticAirportSet.add(flight.getStartAirport());
                    domesticAirportSet.add(flight.getEndAirport());
                }
            }

            //读取航线-飞机限制信息
            XSSFSheet airplaneLimitSheet = workBook.getSheet("航线-飞机限制");
            rowIterator = airplaneLimitSheet.iterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                if(row.getRowNum() == 0)
                    continue;
                AirplaneLimitation airplaneLimitation = new AirplaneLimitation(row);
                String key = airplaneLimitation.getStartAirport() + "#" + airplaneLimitation.getEndAirport();
                if(airplaneLimitationMap.containsKey(key))
                    airplaneLimitationMap.get(key).add(airplaneLimitation);
                else {
                    List<AirplaneLimitation> airplaneLimitationList = new ArrayList<>();
                    airplaneLimitationList.add(airplaneLimitation);
                    airplaneLimitationMap.put(key, airplaneLimitationList);
                }
            }

            //读取机场关闭限制信息
            XSSFSheet airportCloseSheet = workBook.getSheet("机场关闭限制");
            rowIterator = airportCloseSheet.iterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                if(row.getRowNum() == 0)
                    continue;
                AirportClose airportClose = new AirportClose(row);
                String key = airportClose.getAirport();
                if(airportCloseMap.containsKey(key))
                    airportCloseMap.get(key).add(airportClose);
                else {
                    List<AirportClose> airportCloseList = new ArrayList<>();
                    airportCloseList.add(airportClose);
                    airportCloseMap.put(key, airportCloseList);
                }
            }

            //读取故障信息
            XSSFSheet typhoonSceneSheet = workBook.getSheet("台风场景");
            rowIterator = typhoonSceneSheet.iterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                if(row.getRowNum() == 0)
                    continue;
                Scene scene = new Scene(row);
                sceneList.add(scene);
            }

            //读取飞行时间信息
            XSSFSheet travelTimeSheet = workBook.getSheet("飞行时间");
            rowIterator = travelTimeSheet.iterator();
            while(rowIterator.hasNext()){
                Row row = rowIterator.next();
                if(row.getRowNum() == 0)
                    continue;
                TravelTime travelTime = new TravelTime(row);
                String key = travelTime.getAirplaneType() + "#"
                        + travelTime.getStartAirport() + "#"
                        + travelTime.getEndAirport();
                travelTimeMap.put(key, travelTime);
            }

            //关闭excel
            workBook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取选手上传的结果文件
     * @param inputStream
     */
    private void readResultData(InputStream inputStream){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream) {
            });
            String line;
            while((line = reader.readLine()) != null){
                ResultFlight resultFlight = new ResultFlight(line);
                String airplaneId = resultFlight.getAirplaneId();
                if(resultAirLineMap.containsKey(airplaneId)){
                    resultAirLineMap.get(airplaneId).add(resultFlight);
                }
                else {
                    List<ResultFlight> resultFlightList = new ArrayList<>();
                    resultFlightList.add(resultFlight);
                    resultAirLineMap.put(airplaneId, resultFlightList);
                }
                //不能存在重复航班
                if(resultFlightMap.containsKey(resultFlight.getFlightId())){
                    constraintViolationNum += 1;
                    System.out.println("不能存在重复航班: " + resultFlight.getFlightId());
                    isFeasible = false;
                }
                resultFlightMap.put(resultFlight.getFlightId(), resultFlight);
            }
            reader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 重置结果数据，用于新的评分
     */
    private void resetStatisticsData(){
        resultAirLineMap = new HashMap<>();
        resultFlightMap = new HashMap<>();
        emptyFlightNum = 0;
        cancelFlightNum = 0.0;
        flightTypeChangeNum = 0.0;
        connectFlightStraightenNum = 0;
        totalFlightDelayHours = 0.0;
        totalFlightAheadHours = 0.0;
        isFeasible = true;
        constraintViolationNum = 0;
    }

    /**
     * 当所有数据统计完后，按照指定的公式计算公式
     * @return
     */
    private double calculateScore(){
    	System.out.println("isFeasible:" + isFeasible);
    	System.out.println("empty:" + emptyFlightNum);
    	System.out.println("cancel:" + cancelFlightNum);
    	System.out.println("change:" + flightTypeChangeNum);
    	System.out.println("connect:" + connectFlightStraightenNum);
    	System.out.println("delay:" + totalFlightDelayHours);
    	System.out.println("ahead:" + totalFlightAheadHours);
    	System.out.println("constraintViolationNum:" + constraintViolationNum);
    	
    	
       return emptyFlightNum * Configuration.adjustFlightParam +
               cancelFlightNum * Configuration.cancelFlightParam +
               flightTypeChangeNum * Configuration.flightTypeChangeParam +
               connectFlightStraightenNum * Configuration.connectFlightStraightenParam +
               totalFlightDelayHours * Configuration.delayFlightParam +
               totalFlightAheadHours * Configuration.aheadFlightParam +
               (isFeasible ? 0 : 1) * Configuration.infeasibilityPenaltyParam +
               constraintViolationNum * Configuration.constraintViolationPenaltyParam;
    }

    /**
     * 全局的判断结果的合法性，结果数据中是否包含全部的飞机ID, 航班ID
     * @return
     */
    private void globalJudgeLegalityOfResult(){
        Set<String> originAirplaneIdSet = airLineMap.keySet();
        if(!resultAirLineMap.keySet().containsAll(originAirplaneIdSet)) {
            //少一个飞机，算违背一次约束
            Iterator<String> airplaneIdIter = resultAirLineMap.keySet().iterator();
            while(airplaneIdIter.hasNext()){
                String airplaneId = airplaneIdIter.next();
                if(!originAirplaneIdSet.contains(airplaneId)){
                	System.out.println("少一个飞机，算违背一次约束" + airplaneId);
                    constraintViolationNum += 1;
                }
            }
            System.out.println("少一个飞机，算违背一次约束1");
            isFeasible = false;
        }
        Set<String> originFlightIdSet = flightMap.keySet();
        if(!resultFlightMap.keySet().containsAll(originFlightIdSet)) {
            //少一个航班，算违背一次约束
            Iterator<String> flightIdIter = resultFlightMap.keySet().iterator();
            while(flightIdIter.hasNext()){
                String flightId = flightIdIter.next();
                ResultFlight resultFlight = resultFlightMap.get(flightId);
                if(resultFlight.isEmptyFly())  //过滤空飞的航班
                    continue;
                if(!originFlightIdSet.contains(flightId)){
                	System.out.println("少一个飞机，算违背一次约束" + flightId);
                    constraintViolationNum += 1;
                }
            }
            System.out.println("少一个飞机，算违背一次约束2");
            isFeasible = false;
        }
    }

    /**
     * 针对已经删除了取消航班，并且按照时间排好序的航线进行合法性检测
     * @param airLine 调整后的航线
     * @return
     */
    private void judgeLegalityOfAirLine(List<ResultFlight> airLine){
        for(int index = 0; index < airLine.size(); ++ index){
            ResultFlight newFlight = airLine.get(index);
            String flightId = newFlight.getFlightId();
            String startAirport = newFlight.getStartAirport();
            String endAirport = newFlight.getEndAirport();
            String airplaneId = newFlight.getAirplaneId();
            String airplaneType = airLineMap.get(newFlight.getAirplaneId()).get(0).getAirplaneType();
            Flight originFlight = flightMap.get(newFlight.getFlightId());
            //判断第一个航班的起飞机场必须与飞机的初始起飞机场一致
            if(index == 0){
                if(!airplaneStartAirportMap.get(airplaneId).equals(startAirport)){
                    constraintViolationNum += 1;
                    System.out.println("判断第一个航班的起飞机场必须与飞机的初始起飞机场一致 filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
            }

            //判断飞行时间、起始降落机场是否与原始航班一致
            if(newFlight.isStraighten()){   //判断联程拉直航班
                //首先判断联程航班拉直的方式是否有效
                if(!originFlight.isConnected()) {
                    constraintViolationNum += 1;
                    System.out.println("首先判断联程航班拉直的方式是否有效 filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                    continue;//联程航班拉直后，发现该航班是非联程航班，直接下一轮判断
                }
                String nextFlightId = originFlight.getConnectedFlightId();
                Flight nextFlight = flightMap.get(nextFlightId);
                //联程航班拉直后，第一个航班的降落机场等于联程下一个航班的降落机场
                if(!startAirport.equals(originFlight.getStartAirport())
                        || !endAirport.equals(nextFlight.getEndAirport())
                        || endAirport.equals(originFlight.getEndAirport())) {
                    constraintViolationNum += 1;
                    System.out.println("联程航班拉直后，第一个航班的降落机场等于联程下一个航班的降落机场  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
                //判断调整后的飞行时间
                long travelTime = originFlight.getEndDateTime().getTime() - originFlight.getStartDateTime().getTime()
                        + nextFlight.getEndDateTime().getTime() - nextFlight.getStartDateTime().getTime();
                String travelTimeKey = airplaneType + "#" + startAirport + "#" + endAirport;
                if(travelTimeMap.containsKey(travelTimeKey)) {
                   travelTime = travelTimeMap.get(travelTimeKey).getTravelTime();
                }
                if(newFlight.getEndDateTime().getTime() - newFlight.getStartDateTime().getTime()
                        !=  travelTime) {
                    constraintViolationNum += 1;
                    System.out.println("判断调整后的飞行时间  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
                if(!originFlight.isDomestic() || !nextFlight.isDomestic()
                        || !resultFlightMap.get(nextFlightId).isCancel()){  //联程拉直航班必须为国内航班，且后一个航班必须取消
                    constraintViolationNum += 1;
                    System.out.println("联程拉直航班必须为国内航班，且后一个航班必须取消  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
                //当且仅当中间机场受影响时可拉直航班
                //（只判断起飞限制和降落限制，因为停机限制其实就是起飞限制和降落限制的交集，所以不用判断停机限制）
                boolean affectFlag = false;
                for(int i = 0; i < sceneList.size(); ++i){
                    Scene scene = sceneList.get(i);
                    if(scene.isEndInScene(originFlight.getFlightId(),
                            originFlight.getAirplaneId(),
                            originFlight.getEndAirport(),
                            originFlight.getEndDateTime())){
                        affectFlag = true;
                        break;
                    }
                    if(scene.isStartInScene(nextFlight.getFlightId(),
                            nextFlight.getAirplaneId(),
                            nextFlight.getStartAirport(),
                            nextFlight.getStartDateTime())){
                        affectFlag = true;
                        break;
                    }
                }
                if(!affectFlag){
                    constraintViolationNum += 1;
                    System.out.println("当且仅当中间机场受影响时可拉直航班  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
            }
            else if(!newFlight.isEmptyFly()){  //判断普通航班
               if(newFlight.getEndDateTime().getTime() - newFlight.getStartDateTime().getTime()
                       != originFlight.getEndDateTime().getTime() - originFlight.getStartDateTime().getTime()) {
                   constraintViolationNum += 1;
                   System.out.println("判断普通航班 飞行时间  filghtID: " + newFlight.getFlightId());
                   isFeasible = false;
               }
               if(!startAirport.equals(originFlight.getStartAirport())
                       || !endAirport.equals(originFlight.getEndAirport())) {
                   constraintViolationNum += 1;
                   System.out.println("判断普通航班  启停机场  filghtID: " + newFlight.getFlightId());
                   isFeasible = false;
               }
            }
            else {  //判断调机（空飞）航班
                String travelTimeKey = airplaneType + "#" + startAirport + "#" + endAirport;
                if(!travelTimeMap.containsKey(travelTimeKey)) {
                    constraintViolationNum += 1;
                    System.out.println("判断调机（空飞）航班 飞行时间存在  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
                else{
                    if(newFlight.getEndDateTime().getTime() - newFlight.getStartDateTime().getTime()
                            != travelTimeMap.get(travelTimeKey).getTravelTime()) {
                        constraintViolationNum += 1;
                        System.out.println("判断调机（空飞）航班 飞行时间长度 filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                }
                //只允许国内机场才能调机
                if(!(domesticAirportSet.contains(startAirport) && domesticAirportSet.contains(endAirport))){
                    constraintViolationNum += 1;
                    System.out.println("只允许国内机场才能调机  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
            }

            //判断飞机限制
            String airportPair = startAirport + "#" + endAirport;
            if(airplaneLimitationMap.containsKey(airportPair)){
                List<AirplaneLimitation> airplaneLimitationList = airplaneLimitationMap.get(airportPair);
                for(int i = 0; i < airplaneLimitationList.size(); ++ i){
                    if(airplaneLimitationList.get(i).getAirplaneId().equals(airplaneId)) {
                        constraintViolationNum += 1;
                        System.out.println("判断飞机限制  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                }
            }
            //判断机场关闭限制
            if(airportCloseMap.containsKey(startAirport)){
                List<AirportClose> airportCloseList = airportCloseMap.get(startAirport);
                for(int i = 0; i < airportCloseList.size(); ++ i){
                    if(airportCloseList.get(i).isClosed(newFlight.getStartDateTime().getTime())) {
                        constraintViolationNum += 1;
                        System.out.println("判断机场关闭限制  起飞 filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                }
            }
            if(airportCloseMap.containsKey(endAirport)){
                List<AirportClose> airportCloseList = airportCloseMap.get(endAirport);
                for(int i = 0; i < airportCloseList.size(); ++ i){
                    if(airportCloseList.get(i).isClosed(newFlight.getEndDateTime().getTime())) {
                        constraintViolationNum += 1;
                        System.out.println("判断机场关闭限制 降落  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                }
            }
            //判断台风场景限制(起飞和降落限制)
            for(int i = 0; i < sceneList.size(); ++i){
                Scene scene = sceneList.get(i);
                if(scene.isInScene(flightId, airplaneId, startAirport, endAirport, newFlight.getStartDateTime(), newFlight.getEndDateTime())) {
                    constraintViolationNum += 1;
                    System.out.println("判断台风场景限制(起飞和降落限制) filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
            }

            //如果联程航班两段都不取消，那么两段必须使用同一架飞机
            if(!newFlight.isEmptyFly() && originFlight.isConnected()){
                String nextFlightId = originFlight.getConnectedFlightId();
                if(!resultFlightMap.get(nextFlightId).isCancel() //联程航班没有取消
                        && !newFlight.getAirplaneId().equals(resultFlightMap.get(nextFlightId).getAirplaneId())){ //但是使用了不同的飞机
                    constraintViolationNum += 1;
                    System.out.println("如果联程航班两段都不取消，那么两段必须使用同一架飞机  filghtID: " + newFlight.getFlightId());
                    isFeasible = false;
                }
            }

           //进行前后两个航班的约束判断
            if(index > 0){
               ResultFlight preNewResultFlight = airLine.get(index - 1);
                //判断航站衔接约束
               if(!preNewResultFlight.getEndAirport().equals(newFlight.getStartAirport())){
                   constraintViolationNum += 1;
                   System.out.println("进行前后两个航班的约束判断  filghtID: " + newFlight.getFlightId());
                   isFeasible = false;
                   continue;
               }
                //判断间隔时间
               String flightIntervalTimeKey = preNewResultFlight.getFlightId() + "#" + newFlight.getFlightId();
               long intervalTime = Configuration.maxIntervalTime;
               if(flightIntervalTimeMap.containsKey(flightIntervalTimeKey)){
                  if(intervalTime > flightIntervalTimeMap.get(flightIntervalTimeKey))
                      intervalTime = flightIntervalTimeMap.get(flightIntervalTimeKey);
               }
               if(newFlight.getStartDateTime().getTime() < preNewResultFlight.getEndDateTime().getTime()
                       || newFlight.getStartDateTime().getTime() - preNewResultFlight.getEndDateTime().getTime() < intervalTime){
                   constraintViolationNum += 1;
                   System.out.println("判断间隔时间  filghtID: " + newFlight.getFlightId());
                   isFeasible = false;
               }else {
                   //判断台风场景限制(停机限制)
                   for (int i = 0; i < sceneList.size(); ++i) {
                       Scene scene = sceneList.get(i);
                       Date earliestStartDate = new Date(preNewResultFlight.getEndDateTime().getTime()); //以免引起困惑，从飞机降落时间开始判断停机
                       if (scene.isStopInScene(flightId, airplaneId, startAirport, earliestStartDate, newFlight.getStartDateTime())) {
                           constraintViolationNum += 1;
                           System.out.println("判断台风场景限制(停机限制)  filghtID: " + newFlight.getFlightId());
                           isFeasible = false;
                       }
                   }
               }

            }
        }
    }

    /**
     * 全局判断基地平衡
     * @param newEndAirportMap
     */
    private void globalJudgeBaseBalanceOfResult(Map<String, Integer> newEndAirportMap){
        int totalLandNum = 0;
        Iterator<String> endAirportIter = endAirportMap.keySet().iterator();
        while(endAirportIter.hasNext()){
            String endAirport = endAirportIter.next();
            int landNum = endAirportMap.get(endAirport);
            totalLandNum += landNum;
            int newLandNum = 0;
            if(newEndAirportMap.containsKey(endAirport)){
                newLandNum = newEndAirportMap.get(endAirport);
            }
            if(landNum > newLandNum){
                totalLandNum -= newLandNum;
            }
            else{
                totalLandNum -= landNum;
            }
        }
        if(totalLandNum > 0){
            constraintViolationNum += totalLandNum;
            System.out.println("全局判断基地平衡   ");
            isFeasible = false;
        }
    }

    /**
     * 运行评估器，计算选手得分
     * @param inputStream 结果文件路径
     * @return
     */
    public double runEvaluation(InputStream inputStream){
//    	System.out.println("step1 isFeasible: " + isFeasible);
        //重置结果数据集
        resetStatisticsData();
        //读取结果数据
        readResultData(inputStream);
        //全局判断结果的合法性，结果数据中是否包含全部的飞机ID, 航班ID
        globalJudgeLegalityOfResult();
//        System.out.println("step2 isFeasible: " + isFeasible);
        //统计各项指标
        Iterator<String> iterator = resultAirLineMap.keySet().iterator();
        //统计飞机结束任务时，机场停的飞机次数
        Map<String, Integer> newEndAirportMap = new HashMap<>();
        while(iterator.hasNext()){
            String airplaneId = iterator.next();
            List<ResultFlight> resultFlightList = resultAirLineMap.get(airplaneId);
            for(int index = resultFlightList.size() - 1; index >= 0; -- index){
                ResultFlight rf = resultFlightList.get(index);
                Flight originFlight = flightMap.get(rf.getFlightId());
                //统计取消航班，并且删除取消航班
                if(rf.isCancel()){
                    if(!rf.isStraighten())  //如果不是联程拉直导致取消的航班，才能计算取消权重
                       cancelFlightNum += originFlight.getImportRatio();
                    else {
                        if(!flightMap.get(rf.getFlightId()).isConnected()){//如果是联程拉直导致取消的航班,判断是其否是联程航班
                            constraintViolationNum += 1;
                            System.out.println("如果是联程拉直导致取消的航班,判断是其否是联程航班  filghtID: " + rf.getFlightId());
                            isFeasible = false;
                        }
                    }
                    resultFlightList.remove(index);
                }
            }
            //对处理后的结果按时间从小到大进行排序
            Collections.sort(resultFlightList);
            //判断航线的合理性
            judgeLegalityOfAirLine(resultFlightList);
//            System.out.println("step3 isFeasible: " + isFeasible);
            //统计其他指标
            for(int index = 0; index < resultFlightList.size(); ++ index){
                ResultFlight newFlight = resultFlightList.get(index);
                Flight originFlight = flightMap.get(newFlight.getFlightId());
                //统计空飞的数量
                if(newFlight.isEmptyFly()){
                    emptyFlightNum += 1;
                    continue;
                }
                //统计机型发生变化的航班数量
                String newAirplaneType = airLineMap.get(newFlight.getAirplaneId()).get(0).getAirplaneType();
                if(!newAirplaneType.equals(originFlight.getAirplaneType())){
                    flightTypeChangeNum += originFlight.getImportRatio();
                }
                //统计联程航班拉直的数量（重要系数选择两者之和）
                if(newFlight.isStraighten()){
                    connectFlightStraightenNum += originFlight.getImportRatio();
                    //同时还要加上后置航班的重要系数
                    //首先判断联程航班拉直的方式是否有效
                    if(!originFlight.isConnected()) {
                        constraintViolationNum += 1;
                        System.out.println("首先判断联程航班拉直的方式是否有效  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                        continue;//联程航班拉直后，发现该航班是非联程航班，直接下一轮判断
                    }
                    String nextFlightId = originFlight.getConnectedFlightId();
                    Flight nextFlight = flightMap.get(nextFlightId);
                    connectFlightStraightenNum += nextFlight.getImportRatio();
                }
                //统计航班总延误时间或者总提前时间（小时）
                long timeOffset = newFlight.getStartDateTime().getTime() - originFlight.getStartDateTime().getTime();
                if(timeOffset > 0){
                    boolean isDomestic = originFlight.isDomestic();
                    if(isDomestic && timeOffset > Configuration.maxDomesticDelayTime){   //延迟时间不能超过赛题限制
                        constraintViolationNum += 1;
                        System.out.println("延迟时间不能超过赛题限制 国内  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                    else if((!isDomestic) && timeOffset > Configuration.maxAbroadDelayTime){
                        constraintViolationNum += 1;
                        System.out.println("延迟时间不能超过赛题限制 国际  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                    totalFlightDelayHours += (1.0 * originFlight.getImportRatio() * timeOffset / 1000 / 60 / 60);
                }
                else if(timeOffset < 0){
                    if(-1 * timeOffset > Configuration.maxAheadTime){   //提前时间不能超过赛题限制
                        constraintViolationNum += 1;
                        System.out.println("提前时间不能超过赛题限制 国内  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                    if(!originFlight.isDomestic()){  //必须为国内航班才能提前
                        constraintViolationNum += 1;
                        System.out.println("提前时间不能超过赛题限制 国际  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                    //仅针对在受影响的起飞时间段，受影响的机场起飞的航班
                    boolean affectFlag = false;
                    for(int i = 0; i < sceneList.size(); ++i){
                        Scene scene = sceneList.get(i);
                        if(scene.isStartInScene(originFlight.getFlightId(),
                                originFlight.getAirplaneId(),
                                originFlight.getStartAirport(),
                                originFlight.getStartDateTime())){
                            affectFlag = true;
                            break;
                        }
                    }
                    if(!affectFlag){
                        constraintViolationNum += 1;
                        System.out.println("仅针对在受影响的起飞时间段，受影响的机场起飞的航班  filghtID: " + newFlight.getFlightId());
                        isFeasible = false;
                    }
                    totalFlightAheadHours += (-1.0 * originFlight.getImportRatio() * timeOffset / 1000 / 60 / 60);
                }
            }
            
            if(resultFlightList.size() > 0) {
                String endAirport = resultFlightList.get(resultFlightList.size() - 1).getEndAirport();
                if (newEndAirportMap.containsKey(endAirport)) {
                    newEndAirportMap.put(endAirport, newEndAirportMap.get(endAirport) + 1);
                } else {
                    newEndAirportMap.put(endAirport, 1);
                }
            }
        }
//        System.out.println("step4 isFeasible: " + isFeasible);
        //判断基地平衡
        globalJudgeBaseBalanceOfResult(newEndAirportMap);
//        System.out.println("step5 isFeasible: " + isFeasible);
        return calculateScore();
    }

}
