package deadlock;

import common.TimeSleep;

public class Pizza {
    public enum Step{
        DOUGH(4), //面团
        ROLLED(1),
        SAUCED(1), //酱汁
        CHEESED(2), //奶酪
        TOPPED(5),
        BAKED(2), //烤
        SLICED(1), //切片
        BOXED(0); //装盒
        int effort; //用来到达下一个状态
        Step(int effort){
            this.effort = effort;
        }
        Step forward(){
            if(equals(BOXED))
                return BOXED;
            new TimeSleep(effort * 0.1);
            return values()[ordinal() + 1];
        }
    }
    private Step step = Step.DOUGH;
    private final int id;
    public Pizza(int id) {
        this.id = id;
    }
    public Pizza next(){
        this.step = step.forward();
        System.out.println("Pizza "+id+": "+step);
        return this;
    }
    public Pizza next(Step previousStep){
        if(!step.equals(previousStep)){
            throw new IllegalStateException("Excepted "+ previousStep + " but found "+ step);
        }
        return this.next();
    }
    public Pizza roll(){
        return next(Step.DOUGH);
    }
    public Pizza sauce(){
        return next(Step.ROLLED);
    }
    public Pizza cheese(){
        return next(Step.SAUCED);
    }
    public Pizza topping(){
        return next(Step.CHEESED);
    }
    public Pizza bake(){
        return next(Step.TOPPED);
    }
    public Pizza slice(){
        return next(Step.BAKED);
    }
    public Pizza box(){
        return next(Step.SLICED);
    }
    public boolean complete(){
        return this.step.equals(Step.BOXED);
    }
    @Override
    public String toString(){
        return "Pizza"+ id +": "+(step.equals(Step.BOXED)?"complete" : step);
    }
}
