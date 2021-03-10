import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Team {

    public static void main(String[] argv) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please enter team name: ");
        String teamName = br.readLine();

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        String MESSAGE_QUEUE_NAME = Configuration.MESSAGE_QUEUE_PREFIX + teamName;
        channel.queueDeclare(MESSAGE_QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);

        // exchanges
        channel.exchangeDeclare(Configuration.MESSAGE_EXCHANGER, BuiltinExchangeType.TOPIC);

        // queue & bind
        channel.queueBind(MESSAGE_QUEUE_NAME, Configuration.MESSAGE_EXCHANGER, teamName);
        channel.queueBind(MESSAGE_QUEUE_NAME, Configuration.MESSAGE_EXCHANGER, "#.all_teams.#");

        // consumer (handle msg)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Message: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(MESSAGE_QUEUE_NAME, false, consumer);

        while(true) {
            System.out.println("What do you want to order?");
            String message = br.readLine();
            if(message.equals("exit"))break;
            channel.basicPublish(Configuration.ITEM_EXCHANGER, teamName + "." + message, null, (teamName + "." + message).getBytes());
            System.out.println("Order: " + message);
        }
        // close
        channel.close();
        connection.close();
    }

}
