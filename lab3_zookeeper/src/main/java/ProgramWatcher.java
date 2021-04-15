import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;

public class ProgramWatcher implements Watcher {

    private final String program;
    private final ZooKeeper zooKeeper;
    private Process process;

    public ProgramWatcher(String program, ZooKeeper zooKeeper ) {
        this.program = program;
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType().equals(Event.EventType.NodeCreated)){
            if(process == null){
                try {
                    process = new ProcessBuilder(program).start();
                    ChildrenInfoHandler childrenInfoHandler = new ChildrenInfoHandler(zooKeeper,Main.Z_NODE);
                    Thread thread = new Thread(childrenInfoHandler);
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(watchedEvent.getType().equals(Event.EventType.NodeDeleted)){
            if(process != null){
                process.destroy();
                process = null;
                }
        }
        try {
            zooKeeper.exists(Main.Z_NODE,this);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
