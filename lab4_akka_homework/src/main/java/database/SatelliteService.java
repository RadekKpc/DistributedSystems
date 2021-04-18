package database;

import database.model.Satellite;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SatelliteService {

    private static SatelliteService satelliteService;

    public  static SatelliteService getInstance(){
        if(satelliteService == null){
            satelliteService = new SatelliteService();
        }
        return satelliteService;
    }

    Session session;

    private SatelliteService() {
        session = HibernateUtil.getSessionFactory().openSession();
    }

    public Satellite getSatellite(int id){
        Satellite satellite = null;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            satellite = session.get(Satellite.class,id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return satellite;
    }

    public void initDB(){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for(int i=100;i<200;i++){
                Satellite satellite = new Satellite(i,0);
                session.save(satellite);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void updateSatelliteErrors(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Satellite satellite = session.get(Satellite.class,id);
            satellite.setErrors(satellite.getErrors() + 1);
            session.save(satellite);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
