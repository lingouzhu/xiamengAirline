package xiaMengAirline.evaluator;


import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by peicheng on 17/6/28.
 */
public class Main {

    public static void evalutor(String resultDataFilePath) {
//        if(args.length != 2){
//            System.err.println("传入参数有误，使用方式为：java -jar xxx.jar  xxx.xlsx   xxx.csv");
//            return;
//        }
//        String inputDataFilePath = "data/厦航大赛数据20170814.xlsx";
//        String resultDataFilePath = "data/baseline_result_2.csv";
//        String inputDataFilePath = args[0];
//        String resultDataFilePath = args[1];
        try {
            //计算所得分数
            InputStream inputDataStream = new FileInputStream("XiahangData20170814.xlsx");
            InputStream resultDataStream = new FileInputStream(resultDataFilePath);
            ResultEvaluator resultEvaluator = new ResultEvaluator(inputDataStream);
            double score = resultEvaluator.runEvaluation(resultDataStream);
            System.out.println("选手所得分数为：" + score);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
