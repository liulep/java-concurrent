package dealLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 破坏不可剥夺条件来预防死锁
 */
public class LockTransferAccount {

    //账户余额
    private long balance;
    //转出账户的锁
    private Lock thisLock = new ReentrantLock();
    //转入账户的锁
    private Lock targetAccountLock = new ReentrantLock();

    /**
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(LockTransferAccount targetAccount, long transferMoney) {
        try {
            if (thisLock.tryLock()) {
                try {
                    if (targetAccountLock.tryLock()) {
                        if (this.balance >= transferMoney) {
                            this.balance -= transferMoney;
                            targetAccount.balance -= transferMoney;
                        }
                    }
                } finally {
                    targetAccountLock.unlock();
                }
            }
        }finally {
            thisLock.unlock();
        }
    }
}
