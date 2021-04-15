import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public final static String Z_NODE = "/z";
    public final static String ZOOKEEPER_SERVERS = "127.0.0.1:2191,127.0.0.1:2192,127.0.0.1:2193";

    public static void main(String[] args) {

        try {
            ZooKeeper zooKeeper = new ZooKeeper(ZOOKEEPER_SERVERS,5000,null);
            ProgramWatcher structureWatcher = new ProgramWatcher(args[0],zooKeeper);
            zooKeeper.exists(Z_NODE,structureWatcher);
            ChildrenInfoHandler childrenInfoHandler = new ChildrenInfoHandler(zooKeeper,Main.Z_NODE);
            NodeInfoHandler nodeInfoHandler = new NodeInfoHandler(zooKeeper);
            Thread thread = new Thread(childrenInfoHandler);
            thread.start();

            while (true) {
                System.out.println("Put sth to print all children");
                Scanner scanner = new Scanner(System.in);
                scanner.next();
                if(nodeInfoHandler.checkNodeExists(Z_NODE)) {
                    nodeInfoHandler.printAllChildren(Z_NODE);
                }
                else{
                    System.out.println("Node not exists");
                }
            }

        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
