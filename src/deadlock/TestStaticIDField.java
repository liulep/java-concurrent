package deadlock;

//测试StaticIDField
public class TestStaticIDField {
    public static void main(String[] args) {
        IDChecker.test(StaticIDField::new);
        //可以发现重复的id比较多，单纯的static int对于构造过程来说并不安全
    }
}
