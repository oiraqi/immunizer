import java.io.BufferedReader;
import java.util.*;

public class ResultManager{
    private HashMap<String, ResultLine> map = new HashMap<String, ResultLine>();
    private int r = 0;

    public void build(BufferedReader br) throws Exception{
        String line = null;
        while((line = br.readLine()) != null)
            addLine(line);
    }

    public double getRPrecision(){
        int count = 0;
        ArrayList<ResultLine> al = getSortedResults();
        for(int i=0; i<r; i++)
            if(al.get(i).getLabel().equals("ZAP"))
                count++;
        return (double)count*100/r;
    }

    private void addLine(String line){
        String threadTag = getThreadTag(line);
        double score = getScore(line);
        String label = getLabel(line);        

        if(!map.containsKey(threadTag)){
            map.put(threadTag, new ResultLine(score, label));            
        }
        else{
            ResultLine rl = map.get(threadTag);
            rl.addScore(score);
            map.put(threadTag, rl);
            if(label.equals("ZAP"))
                r++;
        }
    }

    private ArrayList<ResultLine> getSortedResults(){
        Iterator<ResultLine> iterator = map.values().iterator();
        ArrayList<ResultLine> al = new ArrayList<ResultLine>();
        while(iterator.hasNext()){
            ResultLine rl = iterator.next();
            if(rl.getCount() == 2)
                al.add(rl);
            else if(rl.getCount() < 2)
                System.out.print(rl.getLabel() + " ");
        }
        System.out.println();
        al.sort(null);
        return al;
    }

    private String getThreadTag(String line){
        int threadStartIndex = line.indexOf("Thread_");
        int threadEndIndex = line.indexOf(" ", threadStartIndex);
        return line.substring(threadStartIndex, threadEndIndex);
    }

    private double getScore(String line){
        //System.out.println(line);
        int lofOutlierIndex = line.indexOf("lof-outlier=") + 12;
        //System.out.println(lofOutlierIndex);
        return Double.parseDouble(line.substring(lofOutlierIndex).trim());
    }

    private String getLabel(String line){
        if(line.indexOf(" JMeter ") > 0)
            return "JMeter";
        if(line.indexOf(" ZAPNULL ") > 0)
            return "ZAPNULL";
        return "ZAP";
    }
}