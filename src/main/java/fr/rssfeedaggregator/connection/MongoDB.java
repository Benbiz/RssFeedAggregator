package fr.rssfeedaggregator.connection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

import fr.rssfeedaggregator.task.SyncFeedEntries;

public class MongoDB implements ServletContextListener {
	
	private ScheduledExecutorService scheduler;
	
	private static String url = "ds159188.mlab.com:59188";
	private static String user = "benbiz";
	private static String passwd = "*inj=7KE";
	private static String database = "rssfeedaggregator";
	public static MongoClient client;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		scheduler.shutdownNow();
		client.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			MongoClientURI uri = new MongoClientURI(
					"mongodb://" + user + ":" + passwd + "@" + url + "/?authSource=" + database);
			client = new MongoClient(uri);
		} catch (MongoException e) {
			e.printStackTrace();
			throw e;
		}
		
		scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new SyncFeedEntries(), 0, 15, TimeUnit.SECONDS);
	}
}
