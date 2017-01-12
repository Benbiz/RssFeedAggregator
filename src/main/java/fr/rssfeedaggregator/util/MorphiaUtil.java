package fr.rssfeedaggregator.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

public class MorphiaUtil implements ServletContextListener
{
	private final Morphia morphia = new Morphia();
	private Datastore datastore;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		ServletContext ctx = arg0.getServletContext();
		ctx.removeAttribute("DataStore");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext ctx = arg0.getServletContext();
		
		morphia.mapPackage("fr.rssfeedaggregator.entity");
		datastore = morphia.createDatastore((MongoClient)ctx.getAttribute("MongoDB"), ctx.getInitParameter("DBNAME"));
		datastore.ensureIndexes();
		ctx.setAttribute("DataStore", datastore);
	}
}
