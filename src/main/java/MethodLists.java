import java.util.Arrays;

/**
 * Created by snigdhc on 23/2/17.
 */
public class MethodLists {

    private String declaredMethodName;
    private int lineUsage[];
    private LineUsages lineusage;

    public LineUsages getLineusage() {
        return lineusage;
    }

    public void setLineusage(LineUsages lineusage) {
        this.lineusage = lineusage;
    }



    public String getDeclaredMethodName() {
        return declaredMethodName;
    }

    public void setDeclaredMethodName(String declaredMethodName) {
        this.declaredMethodName = declaredMethodName;
    }



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Declared Method="+getDeclaredMethodName()+"\n");
        sb.append("Lines: "+getLineusage()+"\n");

        return sb.toString();
    }
}
