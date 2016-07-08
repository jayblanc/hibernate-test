package fr.ortolang.hibernate.test;

import static org.junit.Assert.assertEquals;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.ortolang.hibernate.model.Toto;


public class DeathTest {
    
    private static final Logger LOGGER = Logger.getLogger(DeathTest.class.getName());

    private static String connectionUrl;
    private static String shutdownUrl;
    private static EntityManagerFactory emf;
    private static EntityManager em;
    
    @BeforeClass
    public static void beforeClass() {
        LOGGER.log(Level.INFO, "Initialising...");
        try {
            LOGGER.log(Level.INFO, "Starting database");
            System.setProperty("derby.system.home", System.getProperty("user.dir") + ".hibernatetest");
            shutdownUrl = "jdbc:derby:tooldb;shutdown=true";
            connectionUrl = "jdbc:derby:tooldb;create=true";
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            DriverManager.getConnection(connectionUrl).close();
            LOGGER.log(Level.INFO, "Database started");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to start database", e);
        }
        try {
            LOGGER.log(Level.INFO, "Building Hibernate EntityManager");
            Map<String, String> props = new HashMap<String, String>();
            props.put("hibernate.connection.url", connectionUrl);
            emf = Persistence.createEntityManagerFactory("totoPU");
            em = emf.createEntityManager();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to build hibernate EntityManager", e);

        }
        LOGGER.log(Level.INFO, "Init done");
    }

    @AfterClass
    public static void afterClass() {
        LOGGER.log(Level.INFO, "Stopping Persistence...");
        if (em != null) {
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
        LOGGER.log(Level.INFO, "Persistence stopped...");
        LOGGER.log(Level.INFO, "Stopping Database.");
        try {
            DriverManager.getConnection(shutdownUrl).close();
            LOGGER.log(Level.INFO, "Database stopped");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database shutdown problem", e);
        }
        LOGGER.log(Level.INFO, "Job Manager stopped");
    }

    @Test
    public void testUpdateClonedObject() throws Exception {
        try {
            em.getTransaction().begin();
            Toto toto = new Toto();
            toto.setId("1");
            toto.setName("THE toto one");
            toto.setSurname("Dadaduc");
            em.persist(toto);
            em.getTransaction().commit();
            
            em.getTransaction().begin();
            Toto origin = em.find(Toto.class, "1");
            origin.setName("THE toto two");
            origin.setSurname("Kabadoc");
            
            Toto clone = new Toto();
            clone.setId("2");
            clone.setName(origin.getName());
            clone.setSurname(origin.getSurname());
            em.persist(clone);
            
            origin = clone;
            em.merge(origin);
            em.getTransaction().commit();
            
            em.getTransaction().begin();
            Toto t1 = em.find(Toto.class, "1");
            Toto t2 = em.find(Toto.class, "2");
            assertEquals("1", t1.getId());
            assertEquals("THE toto one", t1.getName());
            assertEquals("Dadaduc", t1.getSurname());
            assertEquals("2", t2.getId());
            assertEquals("THE toto two", t2.getName());
            assertEquals("Kadadoc", t2.getSurname());
            em.getTransaction().commit();
            
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new Exception("problem", e);
        }

    }

}
