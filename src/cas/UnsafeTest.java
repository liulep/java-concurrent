package cas;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe案例
 */
public class UnsafeTest {
    private static final Unsafe unsafe = getUnsafe();
    private static long staticNameOffset = 0;
    private static long memberVariableOffset = 0;
    private static String staticName = "liulep";
    private static String memberVariable = "001";

    static {
        try {
            staticNameOffset = unsafe.staticFieldOffset(UnsafeTest.class.getDeclaredField("staticName"));
            memberVariableOffset = unsafe.staticFieldOffset(UnsafeTest.class.getDeclaredField("memberVariable"));
        }catch (NoSuchFieldException e){
            e.printStackTrace();
        }
    }

    private static Unsafe getUnsafe(){
       Unsafe unsafe = null;
       try {
           Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
           theUnsafe.setAccessible(true);
           unsafe = (Unsafe) theUnsafe.get(null);
       }catch (Exception e){
           e.printStackTrace();
       }
       return unsafe;
    }

    public static void main(String[] args) {
        UnsafeTest unsafeTest = new UnsafeTest();
        System.out.println("修改前的数值如下：");
        System.out.println("staticName = "+staticName+",memberVariable = "+unsafeTest.memberVariable);

        unsafe.putObject(UnsafeTest.class, staticNameOffset, "yueue");
        unsafe.compareAndSwapObject(unsafeTest, memberVariableOffset, "001", "liulep");
        System.out.println("修改后的数值如下");
        System.out.println("staticName = "+staticName+",memberVariable = "+unsafeTest.memberVariable);
    }

}
