package dealLock;

public class DeadLockTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(DeadLockTransferAccount targetAccount, long transferMoney){
        synchronized (this){
            synchronized (targetAccount){
                if(this.balance >= transferMoney){
                    this.balance -= transferMoney;
                    targetAccount.balance += transferMoney;
                }
            }
        }
    }

    /**
     * 结果就是会导致死锁的产生
     * 当两个线程A,B 都进入方法的时候，线程A获取到账户A的锁，线程B获取到了B的锁
     * A账户向B账户转账，B账户向A转账
     * 线程A获取账户B的锁时发现被线程B占用
     * 线程B获取账户A的锁时发现被线程A占用
     * 导致双方等待对方释放锁进行转账操作
     */
}
