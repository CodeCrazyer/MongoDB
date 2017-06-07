package com.allen.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC on 2017/6/7.
 */
public class MongoUtil {

    private static final Logger logger = LoggerFactory.getLogger(MongoUtil.class);

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 27017;
    private static final String host = StringUtils.isBlank(ResourceUtil.get("mongodb", "mongodb.host")) ? DEFAULT_HOST : ResourceUtil.get("mongodb", "mongodb.host");
    private static final int port = StringUtils.isBlank(ResourceUtil.get("mongodb", "mongodb.port")) ? DEFAULT_PORT : Integer.parseInt(ResourceUtil.get("mongodb", "mongodb.port"));
    private static final String username = ResourceUtil.get("mongodb", "mongodb.username");
    private static final String password = ResourceUtil.get("mongodb", "mongodb.password");
    private static final String dbname = ResourceUtil.get("mongodb", "mongodb.testdb");

//    private static MongoClient client = new MongoClient(host, port);
    private static ServerAddress serverAddress = new ServerAddress(host, port);
    private static MongoCredential credential = MongoCredential.createScramSha1Credential(username, dbname, password.toCharArray());
    private static List<MongoCredential> credentials = new ArrayList<MongoCredential>();
    private static MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
    static {
        credentials.add(credential);

        //连接池配置，其实MongoClient默认就使用了连接池，这里我们可以根据需要改变其连接池的配置
        builder.connectionsPerHost(500);//最大连接数
        builder.minConnectionsPerHost(1);//最小连接数
        builder.maxWaitTime(1 * 60 * 1000);//获取连接等待时间
        builder.connectTimeout(10 * 1000);//连接超时10秒钟
        builder.maxConnectionIdleTime(0);//连接最大空闲时间，0表示无限
        builder.maxConnectionLifeTime(0);//连接最大生存时间，0表示无限
    }
    private static MongoClientOptions options = builder.build();
    private static MongoClient client = new MongoClient(serverAddress, credentials, options);
    /**
     * 获取数据库
     * @return
     */
    public static MongoDatabase getDataBase() {
        if (StringUtils.isBlank(dbname)) {
            logger.error("database name must be not null or empty");
            return null;
        }
        return client.getDatabase(dbname);
    }

    /**
     * 获取集合
     * @param collection 集合名
     * @return
     */
    public static MongoCollection<Document> getCollection(String collection) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
            return null;
        }
        return getDataBase().getCollection(collection);
    }

    /**
     * 插入多条文档到指定集合
     * @param collection 集合名
     * @param document 需要插入的文档
     */
    public static void insertOne(String collection, Document document) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        coll.insertOne(document);
    }

    /**
     * 插入多条文档到指定集合
     * @param collection 集合名
     * @param list 需要插入的文档集合
     */
    public static void insertMany(String collection, List<Document> list) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        coll.insertMany(list);
    }

    /**
     * 删除一条数据
     * @param collection 集合名
     * @param bson 匹配数据
     */
    public static boolean deleteOne(String collection, Bson bson) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        DeleteResult result = coll.deleteOne(bson);
        if (result.getDeletedCount() == 1l) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除多条数据
     * @param collection 集合名
     * @param bson 匹配数据
     */
    public static long deleteMany(String collection, Bson bson) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        DeleteResult result = coll.deleteMany(bson);
        return result.getDeletedCount();
    }

    /**
     * 更新多条数据
     *
     * @param collection 集合名
     * @param likes      匹配数据
     * @param data       更新数据
     */
    public static boolean updateOne(String collection, Bson likes, Bson data) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        UpdateResult result = coll.updateOne(likes, data);
        if (result.getModifiedCount() == 1l) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新多条数据
     *
     * @param collection 集合名
     * @param likes      匹配数据
     * @param data       更新数据
     */
    public static long updateMany(String collection, Bson likes, Bson data) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        UpdateResult result = coll.updateMany(likes, data);
        return result.getModifiedCount();
    }

    /**
     * 查询指定集合所有的文档
     *
     * @param collection 集合名
     * @param sortBson   排序方式
     * @return
     */
    public static List<Document> find(String collection, Bson sortBson) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        FindIterable<Document> findIterable = coll.find().sort(sortBson);
        MongoCursor<Document> cursor = findIterable.iterator();
        List<Document> list = new ArrayList<Document>();
        while (cursor.hasNext()) {
            list.add(cursor.next());
        }
        return list;
    }

    /**
     * 查询指定集合的一个文档
     * @param collection 集合名
     * @param sortBson   排序方式
     * @return
     */
    public static Document findOne(String collection, Bson sortBson) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        FindIterable<Document> findIterable = coll.find().sort(sortBson);
        MongoCursor<Document> cursor = findIterable.iterator();
        while (cursor.hasNext()) {
            return cursor.next();
        }
        return null;
    }

    /**
     * 查询指定集合符合条件的一个文档
     *
     * @param collection 集合名
     * @param queryBson  查询条件
     * @param sortBson   排序方式
     * @return
     */
    public static Document findOne(String collection, Bson queryBson, Bson sortBson) {
        if (StringUtils.isBlank(collection)) {
            logger.error("collection name must be not null or empty");
        }
        MongoCollection<Document> coll = getCollection(collection);
        FindIterable<Document> findIterable = coll.find(queryBson).sort(sortBson);
        MongoCursor<Document> cursor = findIterable.iterator();
        while (cursor.hasNext()) {
            return cursor.next();
        }
        return null;
    }

    public static void main(String[] args) {
        List<Document> list = MongoUtil.find("user", null);
        System.out.println(list);
        Bson bson = Filters.and(Filters.eq("name", "yangtao"), Filters.eq("age", 24));
        long res = MongoUtil.deleteMany("user", bson);
        System.out.println("res = " + res);
    }

}