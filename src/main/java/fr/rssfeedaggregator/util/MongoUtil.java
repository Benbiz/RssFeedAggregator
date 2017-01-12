package fr.rssfeedaggregator.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class MongoUtil implements ServletContextListener
{
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		ServletContext ctx = arg0.getServletContext();
		MongoClient client = (MongoClient)ctx.getAttribute("MongoDB");
		
		client.close();
		ctx.removeAttribute("MongoDB");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ServletContext ctx = arg0.getServletContext();
		try
		{
			String URI = new String("mongodb://");
			
			URI += ctx.getInitParameter("DBUSER") + ":" + ctx.getInitParameter("DBPWD");
			URI += "@" + ctx.getInitParameter("DBURL");
			URI += "/?authSource=" + ctx.getInitParameter("DBNAME");

			MongoClient client = new MongoClient(new MongoClientURI(URI));
			ctx.setAttribute("MongoDB", client);
		}
		catch (MongoException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}