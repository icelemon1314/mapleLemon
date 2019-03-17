package database;

import database.dao.BaseDao;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DaoFactory {

    private static  DaoFactory instance = new DaoFactory();
    private DaoFactory(){
    }
    public static DaoFactory getInstance(){
        return instance;
    }

    /*---------------dao工厂，产生dao的实现类----------------------*/
    public <T> T createDao(Class<T> interfaceClass){	//传入dao接口的接口名.class，返回接口的实现类
        String simplename = interfaceClass.getSimpleName();
        System.out.println("简单类名是："+simplename);	//UserDao
        String className = interfaceClass.getName();
        try {
            return (T) Class.forName(className).newInstance();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
        return null;
    }
}
