import java.util.Arrays;

/**
 * Created by snigdhc on 23/2/17.
 */
public class LineUsages {

    private static int count = 0;
    private int usageList[];

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        LineUsages.count = count;
        LineUsages.count++;
    }

    public int[] getUsageList() {
        return usageList;
    }

    public void setUsageList(int[] usageList) {
        this.usageList = usageList;
    }



    @Override
    public String toString(){
        return (getCount()+" : "+Arrays.toString(getUsageList()));
    }


}
