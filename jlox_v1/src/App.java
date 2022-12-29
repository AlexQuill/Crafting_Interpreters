import java.util.ArrayList;

import javax.xml.bind.PrintConversionEvent;

public class App {
    public static void main(String[] args) throws Exception {
        
        ArrayList<String> testList = new ArrayList<>();
        testList.add("hello");
        testList.add("world");
        for (int i = 0; i < testList.size(); i++) {
            System.out.println(testList.get(i));
        }
    }
}
