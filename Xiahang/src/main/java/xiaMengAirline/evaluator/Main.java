package xiaMengAirline.evaluator;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by peicheng on 17/6/28.
 */
public class Main {

    public void evalutor(String csvFile) {
//        if(args.length != 2){
//            System.err.println("传入参数有误，使用方式为：java -jar xxx.jar  xxx.xlsx   xxx.csv");
//            return;
//        }
        String inputDataFilePath = "XiahangData.xlsx";
        String resultDataFilePath = csvFile;
        try {
            //计算所得分数
            InputStream inputDataStream = new FileInputStream(inputDataFilePath);
            InputStream resultDataStream = new FileInputStream(resultDataFilePath);
            ResultEvaluator resultEvaluator = new ResultEvaluator(inputDataStream);
//            resultEvaluator.generateBaselineResult(resultDataFilePath);
            double score = resultEvaluator.runEvaluation(resultDataStream);
            System.out.println("选手所得分数为：" + score);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
