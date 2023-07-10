package deadlock;

// 哲学家类
public class Philosopher implements Runnable{
    private final int seat;
    private final StickHolder left, right;
    public Philosopher(int seat, StickHolder left, StickHolder right){
        this.seat = seat;
        this.left = left;
        this.right = right;
    }
    @Override
    public void run() {
        while (true){
            right.pickUp();
            left.pickUp();
            System.out.println(this + " eating");
            right.pickDown();
            left.pickDown();
        }
    }

    @Override
    public String toString(){
        return "P"+this.seat;
    }
}
