import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Supplier {

    public static void main(String[] argv) throws Exception {

        List<String> items = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please enter supplier name: ");
        String supplierName = br.readLine();

        while(true){
            System.out.println("Please enter next item or 'exit':");
            String item = br.readLine();
            if(item.equals("exit"))break;
            items.add(item);

        }

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(Configuration.ITEM_EXCHANGER, BuiltinExchangeType.TOPIC);

        // queue
        for (String item : items) {
            String ITEM_QUEUE_NAME = Configuration.ITEM_QUEUE_PREFIX + item;
            channel.queueDeclare(ITEM_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);
            channel.queueBind(ITEM_QUEUE_NAME, Configuration.ITEM_EXCHANGER, "*." + item);
        }

        String MESSAGE_QUEUE_NAME = Configuration.MESSAGE_QUEUE_PREFIX + supplierName;
        channel.queueDeclare(MESSAGE_QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);

        // exchanges
        channel.exchangeDeclare(Configuration.MESSAGE_EXCHANGER, BuiltinExchangeType.TOPIC);

        // queue & bind
        channel.queueBind(MESSAGE_QUEUE_NAME, Configuration.MESSAGE_EXCHANGER, supplierName);
        channel.queueBind(MESSAGE_QUEUE_NAME, Configuration.MESSAGE_EXCHANGER, "#.all_suppliers.#");

        // consumer (handle orders and messages)
        Consumer itemConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                String[] team_item = message.split("\\.");
                if (team_item.length > 1){
                    String team = team_item[0];
                    String item = team_item[1];
                    int orderNumber = (int) (Math.random() * 100000);
                    System.out.println("New order nr " + orderNumber +", item: " + item);
                    String responseToClient = "Your order nr" + Integer.toString(orderNumber) + ", item: " + item + " has been finished";
                    channel.basicPublish(Configuration.MESSAGE_EXCHANGER, team, null, responseToClient.getBytes());
                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Consumer messageConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Message: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // listen message queue
        System.out.println("Waiting for orders...");
        channel.basicConsume(MESSAGE_QUEUE_NAME, false, messageConsumer);

//        listen items queues
        for (String item : items) {
            String ITEM_QUEUE_NAME = Configuration.ITEM_QUEUE_PREFIX + item;
            channel.basicConsume(ITEM_QUEUE_NAME, false, itemConsumer);

        }
//        while(true) {
//            System.out.println("Enter team name");
//            String team = br.readLine();
//            System.out.println("Enter message");
//            String message = br.readLine();
//            if(team.equals("exit"))break;
//            channel.basicPublish(Configuration.MESSAGE_EXCHANGER, team, null, message.getBytes());
//            System.out.println("Message sended.");
//        }
        // close
        //channel.close();
        //connection.close();
    }
}
