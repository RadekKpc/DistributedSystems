import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ChildrenInfoHandler implements Runnable, Watcher {

    ZooKeeper zooKeeper;
    String node;
    NodeInfoHandler handler;

    public ChildrenInfoHandler(ZooKeeper zooKeeper, String node) {
        this.zooKeeper = zooKeeper;
        this.node = node;
        handler = new NodeInfoHandler(zooKeeper);
    }

    @Override
    public void run() {
        try {
            if (handler.checkNodeExists(node)) {
                handler.subscribeChildren(node,this);
            }
        }catch (Exception e){}
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            handler.subscribeChildren(Main.Z_NODE,this);
            System.out.println("Descendants: " + zooKeeper.getAllChildrenNumber(Main.Z_NODE));
        } catch (KeeperException | InterruptedException e) {
        }
    }
}
