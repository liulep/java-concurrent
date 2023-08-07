package dealLock;

/**
 * 线程不安全的转账操作
 */
public class UnsafeTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(UnsafeTransferAccount targetAccount, long transferMoney){
        synchronized (this){
            if(this.balance >= transferMoney){
                this.balance -= transferMoney;
                targetAccount.balance += transferMoney;
            }
        }
    }
}
