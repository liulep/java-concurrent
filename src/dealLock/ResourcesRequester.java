package dealLock;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 一次性存放和释放资源
 */
public class ResourcesRequester {

    //申请资源的集合
    private List<Object> resources = new ArrayList<>();

    /**
     * 一次性获取所有的资源
     */
    public synchronized boolean applyResources(Object source, Object target){
        if(resources.contains(source) || resources.contains(target)){
            return false;
        }
        this.resources.add(source);
        this.resources.add(target);
        return true;
    }

    /**
     * 一次性释放资源
     */
    public synchronized void releaseResources(Object source, Object target){
        this.resources.remove(source);
        this.resources.remove(target);
    }
}
