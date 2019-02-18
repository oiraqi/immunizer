public class ResultLine implements Comparable<ResultLine>{
    private double score;
    private int count;
    private String label;

    public ResultLine(double score, String label){
        this.score = score;
        this.count = 1;
        this.label = label;
    }

    public double getScore(){
        return score;
    }

    public void addScore(double score){
        this.score += score;
        count++;
    }

    public int getCount(){
        return count;
    }

    public String getLabel(){
        return label;
    }

    public int compareTo(ResultLine other){
        if(other.getScore() > score)
            return 1;
        if(other.getScore() < score)
            return -1;
        return 0;
    }
}