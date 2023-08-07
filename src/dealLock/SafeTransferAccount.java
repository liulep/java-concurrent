package dealLock;

/**
 * 安全的账户转账
 */
public class SafeTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(SafeTransferAccount targetAccount, long transferMoney){
        synchronized (SafeTransferAccount.class){ //通过锁住class对象来达到串行执行,相当于锁全局都使用了SafeTransferAccount的对象
            if(this.balance >= transferMoney){
                this.balance -= transferMoney;
                targetAccount.balance += transferMoney;
            }
        }
    }
    /**
     * 缺点：会导致排队现象，转账的时候必须等待前一个人转账成功，对于银行转账操作是不可取的
     * 之前都是对当前转账人账户进行加锁或者是对全局的账户对象进行加锁导致了线程安全问题和排队现象问题，并没有对收款人账户进行加锁
     * 那如果我们只对这两个账户进行加锁，是不是并行执行呢
     * 往下看
     */
}
