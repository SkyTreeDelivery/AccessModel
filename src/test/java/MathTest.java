import Access2.MathFunc;
import org.junit.Test;

public class MathTest {

    public void print(Object ss){
        System.out.println(ss.toString());
    }

    @Test
    public void test(){
        print(MathFunc.doubleIsLegal(2));
    }
}
