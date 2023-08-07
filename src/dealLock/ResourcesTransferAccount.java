package dealLock;

/**
 * 破坏请求与保持条件来预防死锁
 * 通过一次性获取所需要的资源来破坏请求和保持条件
 */
public class ResourcesTransferAccount {

    //账户余额
    private long balance;
    private static ResourcesRequester resourcesRequester = new ResourcesRequester();

    /**
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(ResourcesTransferAccount targetAccount, long transferMoney) {
        //以循环的方式确保申请到所有资源
        while (true){
            if(resourcesRequester.applyResources(this, targetAccount)){
                break;
            }
        }
        try{
            synchronized (this){
                synchronized (targetAccount){
                    if(this.balance >= transferMoney){
                        this.balance -= transferMoney;
                        targetAccount.balance += transferMoney;
                    }
                }
            }
        }finally {
            resourcesRequester.releaseResources(this, targetAccount);
        }
    }
}
