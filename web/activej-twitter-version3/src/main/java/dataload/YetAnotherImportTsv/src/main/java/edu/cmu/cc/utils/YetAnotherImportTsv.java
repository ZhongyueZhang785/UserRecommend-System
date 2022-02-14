package edu.cmu.cc.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Import delimiter-separated file(s) from HDFS to HBase table.
 *
 * Please note:
 * This is a simplified tool to demonstrate how to write MapReduce jobs to
 * load data into HBase.
 *
 * This tool is not compliant with the RFC 4180 standard, and quoted fields that
 * contain tabs are not allowed.
 * e.g. col1,"co,l2",col3
 *
 * This tool only supports the simplest CSVs without any quotes.
 * e.g. col,col2,col3
 *
 * Usage:
 * {@code hadoop jar import_tsv.jar edu.cmu.cc.utils.YetAnotherImportTsv}
 *
 * Before you run this program, you should:
 * 1. Make sure that lines in TSV file(s) are in the following format:
 * value1(,value2,...,valueN)
 * 2. Put the TSV file(s) into HDFS
 * e.g., hadoop fs -mkdir /input
 * hadoop fs -put data.csv /input
 * 3. Create the HBase table, along with a ColFamily
 * e.g., hbase shell
 * create 'table_name', 'col_family_name'
 */
public class YetAnotherImportTsv {

    /**
     * The mapper to tokenize the input with at least two columns per line.
     *
     * Input format: value1(,value2,...,valueN)
     *
     * Output format:
     * K: rowKey V: a Map of col,value pairs
     */
    private static class TsvTokenizerMapper
            extends Mapper<Object, Text, Text, MapWritable> {

        /**
         * The index of the column to use as row key.
         *
         * Other columns will be used as cell values.
         */
        final int ROW_KEY_INDEX = 0;
        /**
         * The separator.
         */
        final String SEPARATOR = "\t";

        /**
         * Logger.
         */
        private static final Logger LOGGER = Logger.getLogger(TsvTokenizerMapper.class);
        /**
         * Output K,V.
         */
        private Text outputKey = new Text();
        private MapWritable outputValue = new MapWritable();

        /**
         * Called once at the beginning of the task.
         */
        @Override
        protected void setup(Context context
        ) throws IOException, InterruptedException {
            LOGGER.setLevel(Level.WARN);
        }

        /**
         * Called once for each key/value pair in the input split.
         */
        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            outputValue.clear();
            String[] columns = value.toString().split(SEPARATOR);
            if (columns.length == 1) {
                LOGGER.warn(String.format("Malformed TSV record: %s", value));
            } else {
                String rowKey = columns[ROW_KEY_INDEX];
                outputKey.set(rowKey);

                for (int i = 0; i < columns.length; i++) {
                    if (i == ROW_KEY_INDEX) {
                        continue;
                    }
                    outputValue.put(new VIntWritable(i), new Text(columns[i]));
                }
                context.write(outputKey, outputValue);
            }
        }
    }

    /**
     * The reducer to read from intermediate KV pairs and write to HBase table.
     */
    private static class HBaseTableReducer extends
            TableReducer<Text, MapWritable, ImmutableBytesWritable> {


        final byte[] COLUMN_FAMILY = Bytes.toBytes("data");

        /**
         * The column header as per the dataset.
         */
        final String[] COLUMN_HEADER = {
                "create_time",
                "text",
                "reply_to_id",
                "retweet_to_id",
                "user_id",
                "tweet_id",
                "hash_tag"};
        /**
         * This method is called once for each key.
         *
         * Output: write a cell to the table with:
         * ROW_KEY
         * ColFamily
         * ColQualifier
         * CellValue
         */
        @Override
        protected void reduce(Text key, Iterable<MapWritable> values, Context context
        ) throws IOException, InterruptedException {
            Put put = new Put(Bytes.toBytes(key.toString()));
            for (MapWritable value : values) {
                for (Map.Entry<Writable, Writable> entry : value.entrySet()) {
                    String columnQualifier = COLUMN_HEADER[Integer.parseInt(entry.getKey().toString())];
                    String cellValue = entry.getValue().toString();
                    put.addColumn(COLUMN_FAMILY, Bytes.toBytes(columnQualifier), Bytes.toBytes(cellValue));
                }
            }
            context.write(null, put);
        }
    }

    /**
     * Main entry.
     *
     * You may update the job configurations.
     *
     * @param args run args
     * @throws Exception when IO error occurs
     */
    public static void main(String[] args) throws Exception {
        String inputPath = "/input/try2.csv";
        String tableName = "tweet2";

        Configuration conf = HBaseConfiguration.create();
        // TODO: update the configuration

        String zkAddr = "172.31.29.230";
        conf.set("hbase.master", zkAddr + ":14000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");

        Job job = Job.getInstance(conf, "YetAnotherImportTsv Job");
        job.setJarByClass(YetAnotherImportTsv.class);
        job.setMapperClass(TsvTokenizerMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MapWritable.class);
        TableMapReduceUtil.initTableReducerJob(
                tableName, // output table name
                HBaseTableReducer.class, // reducer class
                job);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
