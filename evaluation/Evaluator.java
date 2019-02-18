import java.io.*;

public class Evaluator{
    private static String __LABEL__ = " ZAP ";

    private static double computeRPrecision(BufferedReader br, int r) throws Exception{
        String line = null;        
        int found = 0, count = 0;
        while(count++ < r && (line = br.readLine()) != null){
            if(line.indexOf(__LABEL__) >= 0)
                found++;
        }
        return (double)found*100/r;
    }

    public static void main(String[] args) throws Exception{
        String folder = args[0];
        int r = Integer.parseInt(args[1]);
        BufferedWriter bw = new BufferedWriter(
                                    new OutputStreamWriter(new FileOutputStream("r_precision.txt")));
        File directory = new File("out/" + folder);
        File[] subFolders = directory.listFiles();
        for(File subFolder : subFolders){
            File[] files = subFolder.listFiles();
            for(File file : files){
                BufferedReader br = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file)));
                bw.write(file.getName().substring(0, file.getName().indexOf('.')) + ": " + computeRPrecision(br, r) + "\t");
                br.close();
            }
            bw.write("\n\n");
        }
        bw.flush();
        bw.close();
    }
}