import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Admin {


    public static void main(String[] argv) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("ADMIN CONSOLE");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        channel.queueDeclare(Configuration.ADMIN_QUEUE, false, false, false, null);
        channel.basicQos(1);

        // exchanges
        channel.exchangeDeclare(Configuration.MESSAGE_EXCHANGER, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(Configuration.ITEM_EXCHANGER, BuiltinExchangeType.TOPIC);

        // queue & bind
        channel.queueBind(Configuration.ADMIN_QUEUE, Configuration.ITEM_EXCHANGER, "*.*");
        channel.queueBind(Configuration.ADMIN_QUEUE, Configuration.MESSAGE_EXCHANGER, "*");

        // consumer (handle msg)
        Consumer sifferConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Message: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Sniffer...");
        channel.basicConsume(Configuration.ADMIN_QUEUE, false, sifferConsumer);

        while(true) {
            System.out.println("Choose command to send a message, direct | all_suppliers | all_teams | all | exit :");
            String command = br.readLine();
            if(command.equals("exit"))break;
            System.out.println("Enter message");
            String message = br.readLine();
            switch (command){
                case "direct":
                    System.out.println("Enter supplier or team name");
                    String name = br.readLine();
                    channel.basicPublish(Configuration.MESSAGE_EXCHANGER, name, null, message.getBytes());
                    break;
                case "all_suppliers":
                    channel.basicPublish(Configuration.MESSAGE_EXCHANGER, "all_suppliers", null, message.getBytes());
                    break;
                case "all_teams":
                    channel.basicPublish(Configuration.MESSAGE_EXCHANGER, "all_teams", null, message.getBytes());
                    break;
                case "all":
                    channel.basicPublish(Configuration.MESSAGE_EXCHANGER, "all_suppliers.all_teams", null, message.getBytes());
                    break;
            }

            System.out.println("Message sended.");
        }

        channel.close();
        connection.close();
    }
}
