import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

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
        String elkiOutFolder = args[0];
        String evaluationOutFolder = args[1];
        int r = Integer.parseInt(args[2]);
        BufferedWriter bw = new BufferedWriter(
                                    new OutputStreamWriter(new FileOutputStream(evaluationOutFolder + "/r_precision.txt")));
        File[] subFolders = new File(elkiOutFolder).listFiles();
        Arrays.sort(subFolders);
        for(File subFolder : subFolders){
            File[] files = subFolder.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2){
                    if(f1 == null && f2 == null)
                        return 0;
                    if(f1 == null)
                        return -1;
                    if (f2 == null)
                        return 1;
                    if ((f1.getName().indexOf('_') > 0) && (f2.getName().indexOf('_') > 0)){
                        String suffix1 = f1.getName().substring(f1.getName().indexOf('_') + 1, f1.getName().indexOf('.'));
                        String suffix2 = f2.getName().substring(f2.getName().indexOf('_') + 1, f2.getName().indexOf('.'));
                        try{
                            return Integer.parseInt(suffix1) - Integer.parseInt(suffix2);
                        }catch(Exception ex){
                            return f1.compareTo(f2);
                        }
                    }else{
                        return f1.compareTo(f2);
                    }
                }
            });
            for(File file : files){
                BufferedReader br = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file)));
                bw.write(file.getName().substring(0, file.getName().indexOf('.')) + ": " + computeRPrecision(br, r) + "\n");
                br.close();
            }
            bw.write("\n\n");
        }
        bw.flush();
        bw.close();
    }
}