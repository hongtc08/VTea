package com.vtea.dao;

import com.vtea.model.Category;
import com.vtea.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class CategoryHibernateDAO {

    private final SessionFactory sessionFactory =
            HibernateUtil.getSessionFactory();

    /**
     * Lấy danh sách phân loại đang Active
     */
    public List<Category> getAllActiveCategories(){
        List<Category> list = new ArrayList<>();

        try(Session session = sessionFactory.openSession()){
            String hql = "FROM Category WHERE isAvailable = true";

            list = session.createQuery(hql, Category.class).list();
        } catch(Exception e){
            System.err.println("Lỗi khi lấy danh sách Category: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy danh sách TẤT CẢ các phân loại (Dùng cho màn hình của ADMIN)
     */
    public List<Category> getAllCategories() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Category";
            return session.createQuery(hql, Category.class).list();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách Category: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

     //Thêm phân loại
    public boolean insertCategory(Category category){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();

            category.setAvailable(true);
            session.save(category); //hibernate tự insert

            transaction.commit();
            return true;
        } catch (Exception e){
            if(transaction != null)
                transaction.rollback();
            System.err.println("Lỗi khi thêm Category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


     //Cập nhật phân loại
    public boolean updateCategory(Category category){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();

            // Hibernate sẽ nhìn vào khóa chính (categoryId) bên trong object
            // để tự động tạo câu lệnh UPDATE cho các trường còn lại
            session.update(category);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Lỗi khi cập nhật Category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Xóa phân loại
    public boolean softDeleteCategory(int categoryId){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();

            String hql = "UPDATE Category SET isAvailable = false WHERE categoryId = :id";
            int result = session.createQuery(hql)
                    .setParameter("id", categoryId)
                    .executeUpdate();

            transaction.commit();
            return result > 0;
        } catch (Exception e){
            if(transaction != null)
                transaction.rollback();
            System.err.println("Lỗi khi xóa Category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
