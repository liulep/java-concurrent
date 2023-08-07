package dealLock;

/**
 * 通过破坏循环等待条件来防止死锁
 * 最简单的做法为每一个账户赋值一个long类型的编号
 */
public class SortedTransferAccount {

    //账户编号
    private long no;
    //账户余额
    private long balance;

    public void transferMoney(SortedTransferAccount targetAccount, long transferMoney){
        SortedTransferAccount beforeLockAccount = null;
        SortedTransferAccount afterLockAccount = targetAccount;
        if(this.no > targetAccount.no){
            beforeLockAccount = targetAccount;
            afterLockAccount = this;
        }
        synchronized (beforeLockAccount){
            synchronized (afterLockAccount){
                if(this.balance >= transferMoney){
                    this.balance -= transferMoney;
                    targetAccount.balance += transferMoney;
                }
            }
        }
    }
}
