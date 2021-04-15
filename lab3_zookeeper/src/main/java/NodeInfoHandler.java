import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.util.List;

public class NodeInfoHandler {

    ZooKeeper zooKeeper;

    public NodeInfoHandler(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    boolean checkNodeExists(String node) {
        try {
            return zooKeeper.exists(node,false) != null;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void subscribeChildren(String node, Watcher watcher) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(node, watcher);
        for (String ch: children){
            subscribeChildren(node + "/" + ch,watcher);
        }
    }

    public void printAllChildren(String node) throws KeeperException, InterruptedException {
        for (String ch : zooKeeper.getChildren(node,false)) {
            System.out.println(node + "/" + ch);
            printAllChildren(node + "/" + ch);
        }
    }
}
