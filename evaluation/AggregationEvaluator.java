import java.io.*;

public class AggregationEvaluator{
    public static void main(String[] args) throws Exception{
        String folder = args[0];
        BufferedWriter bw = new BufferedWriter(
                                    new OutputStreamWriter(new FileOutputStream("ar_precision.txt")));
        File directory = new File("out/" + folder);
        File[] subFolders = directory.listFiles();
        for(File subFolder : subFolders){
            File[] files = subFolder.listFiles();
            for(File file : files){
                BufferedReader br = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file)));
                ResultManager rm = new ResultManager();
                rm.build(br);
                bw.write(file.getName().substring(0, file.getName().indexOf('.')) + ": " + rm.getRPrecision() + "\t");
                br.close();
            }
            bw.write("\n\n");
        }
        bw.flush();
        bw.close();
    }
}