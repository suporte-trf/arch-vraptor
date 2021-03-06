package com.viniciuscardoso.arch.vraptor.dao.common;

import com.viniciuscardoso.arch.vraptor.domain.common.AbstractEntity;
import com.viniciuscardoso.arch.vraptor.utility.ConvertUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

import static ch.lambdaj.Lambda.join;

/**
 * Project: arch
 * User: Vinicius Cardoso
 * Date: 28/03/14
 * Time: 14:38
 */

public abstract class AbstractDao<T extends AbstractEntity> {

    //<editor-fold desc="[Attributes, Constructors, Setters/Getters]">
	private final Logger logger;
    private final Session session;
    private Class<T> classe;

    @SuppressWarnings("unchecked")
    public AbstractDao(Session session, Class<? extends AbstractDao<T>> clazz) {
        this.session = session;
        this.classe = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.logger = Logger.getLogger(clazz);
    }

    protected final Session getSession() {
        return this.session;
    }

	protected final Logger getLogger() {
		return this.logger;
	}
    //</editor-fold>

    //<editor-fold desc="[Create]">
    public void save(T entity) {
        try {
            session.getTransaction().begin();
            session.save(entity);
            session.getTransaction().commit();
            logger.info("Objeto [" + this.getEntityName() + "] adicionado com id " + String.valueOf(entity.getId()));
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.info("Erro ao adicionar objeto. " + e.getMessage());
            throw e;
        }
    }

    public void save(List<T> lista) {
        try {
            session.getTransaction().begin();
            for (T obj : lista) {
                session.save(obj);
            }
            session.getTransaction().commit();
            logger.info("Objetos [" + this.getEntityName() + "] adicionados com sucesso.");
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.info("Erro ao adicionar objeto. " + e.getMessage());
            throw e;
        }
    }
    //</editor-fold>

    //<editor-fold desc="[Retrieve]">
    @SuppressWarnings("unchecked")
    public List<T> list() {
        return session.createQuery("from " + this.classe.getName()).list();
    }

    @SuppressWarnings("unchecked")
    public T getById(Long id) {
        Query q = session.createQuery("from " + this.classe.getName() + " where id = :id");
        q.setParameter("id", id);
        return (T) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<T> getList(List<Long> ids) {
        if (ids != null && ids.size() != 0) {
            Query q = session.createQuery("from " + this.classe.getName() + " where id in (:ids)");
            q.setParameterList("ids", ids);
            return q.list();
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> listSorted(String... listFields) {
        String q = "from " + this.classe.getName();
        if (listFields.length > 0) q += " order by " + join(listFields, ", ");
        return session.createQuery(q).list();
    }

    public int countAll() {
        return ConvertUtils.convertTo(this.session.createQuery("select count(*) from " + this.classe.getName()).uniqueResult(), Integer.class);
    }
    //</editor-fold>

    //<editor-fold desc="[Update]">
    public void update(T entity) {
        try {
            session.getTransaction().begin();
            session.merge(entity);
            session.getTransaction().commit();
            logger.info("Objeto [" + this.getEntityName() + "] atualizado com id = " + String.valueOf(entity.getId()));
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.info("Erro ao atualizar objeto. " + e.getMessage());
            throw e;
        }
    }
    //</editor-fold>

    //<editor-fold desc="[Delete]">
    public void remove(T entity) {
        try {
            session.getTransaction().begin();
            session.delete(entity);
            session.getTransaction().commit();
            logger.info("Objeto [" + this.getEntityName() + "] removido com id = " + String.valueOf(entity.getId()));
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.info("Erro ao remover objeto. " + e.getMessage());
            throw e;
        }
    }
    //</editor-fold>

    //<editor-fold desc="[Private methods]">
    private String getEntityName() {
        return this.classe.getSimpleName();
    }

    private Class<T> getEntityClass() {
        return this.classe;
    }
    //</editor-fold>
}
