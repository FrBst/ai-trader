package service;

import application.Configuration;
import org.deeplearning4j.rl4j.network.ac.ActorCriticSeparate;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.policy.ACPolicy;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.util.ModelSerializer;
import rl4j.BrokerMdp;
import rl4j.SimpleBroker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.stream.Collectors;

public class NetworkTest implements Runnable {
    private final SimpleBroker broker;
    private ACPolicy<SimpleBroker> policy;
    private final FileWriter writer;

    public NetworkTest(ACPolicy<SimpleBroker> policy, double startingCash, int id) {
        broker = new SimpleBroker(startingCash, new Random(), -1);
        this.policy = policy;

        File myObj = new File(Configuration.getConfig("temp-folder") + id + ".csv");
        try {
            myObj.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            writer = new FileWriter(myObj);
            writer.write("net_value,stocks_count,stock_price,action\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        while (!broker.isEnd()) {
            step();
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void step() {
        Integer action = policy.nextAction(broker.getData());
        action -= 12;
        int change = 0;
        if (action > 0) {
            change = (int)Math.pow(2, action);
            broker.buy(change);
        } else if (action < 0) {
            change = -(int)Math.pow(2, -action);
            broker.sell(-change);
        }
        broker.step();

        try {
            writer.write(broker.netValue() + "," + broker.getStocksCount() + "," + broker.getCurrentPrice() + "," + change + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
