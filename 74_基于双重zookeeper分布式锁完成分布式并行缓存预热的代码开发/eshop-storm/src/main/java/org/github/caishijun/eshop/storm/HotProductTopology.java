package org.github.caishijun.eshop.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;
import org.github.caishijun.eshop.storm.bolt.LogParseBolt;
import org.github.caishijun.eshop.storm.bolt.ProductCountBolt;
import org.github.caishijun.eshop.storm.spout.AccessLogKafkaSpout;

/**
 * 热数据统计拓扑
 */
public class HotProductTopology {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("AccessLogKafkaSpout", new AccessLogKafkaSpout(), 1);
        builder.setBolt("LogParseBolt", new LogParseBolt(), 5)
                .setNumTasks(5)
                .shuffleGrouping("AccessLogKafkaSpout");
        builder.setBolt("ProductCountBolt", new ProductCountBolt(), 5)
                .setNumTasks(10)
                .fieldsGrouping("LogParseBolt", new Fields("productId"));

        Config config = new Config();

        if(args != null && args.length > 1) {
            config.setNumWorkers(3);
            try {
                StormSubmitter.submitTopology(args[0], config, builder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("HotProductTopology", config, builder.createTopology());
            Utils.sleep(30000);
            cluster.shutdown();
        }
    }

}

