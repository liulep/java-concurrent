package deadlock;

public class StaticIDField implements HasID{
    private static int counter = 0;
    private int id = counter++;
    @Override
    public int getId() {
        return id;
    }
}
