package ru.terra.btobd.server.db;

import org.slf4j.LoggerFactory;
import ru.terra.btobd.server.db.exceptions.NonexistentEntityException;
import ru.terra.btobd.server.entity.OBDInfo;
import ru.terra.btobd.server.entity.User;
import ru.terra.server.db.controllers.AbstractJpaController;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 17.11.14
 * Time: 2:48
 */
public class ObdJpaController extends AbstractJpaController<OBDInfo> {
    public ObdJpaController() {
        super(OBDInfo.class);
    }

    @Override
    public void create(OBDInfo obdInfo) throws Exception {
        EntityManager em = getEntityManager();
        try {
            User userId = obdInfo.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                obdInfo.setUserId(userId);
            }
            em.getTransaction().begin();
            em.persist(obdInfo);
            em.getTransaction().commit();
            LoggerFactory.getLogger(this.getClass()).info("Storing " + obdInfo);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        EntityManager em = getEntityManager();
        try {
            OBDInfo info;
            try {
                info = em.getReference(OBDInfo.class, id);
                info.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transaction with id " + id + " no longer exists.", enfe);
            }
            User userId = info.getUserId();
            if (userId != null) {
                userId.getInfoList().remove(info);
                userId = em.merge(userId);
            }
            em.remove(info);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(OBDInfo obdInfo) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            OBDInfo persistentInfo = em.find(OBDInfo.class, obdInfo.getId());
            User userIdOld = persistentInfo.getUserId();
            User userIdNew = obdInfo.getUserId();
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getInfoList().remove(obdInfo);
                userIdOld = em.merge(userIdOld);
            }
            em.merge(obdInfo);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public OBDInfo getCurrent(User uid) {
        EntityManager em = getEntityManager();
        try {
            return em.createNamedQuery("OBDInfo.findCurrent", OBDInfo.class).setParameter("uid", uid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<OBDInfo> listByUser(boolean all, int page, int perpage, User uid) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<OBDInfo> query = em.createNamedQuery("OBDInfo.findByUser", OBDInfo.class).setParameter("uid", uid);
            if (!all) {
                query.setMaxResults(perpage);
                query.setFirstResult(page * perpage);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<String> getParams(User uid) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<OBDInfo> q = em.createNamedQuery("OBDInfo.findParameters", OBDInfo.class).setParameter("uid", uid);
            List<String> ret = new ArrayList<>();
            for (OBDInfo info : q.getResultList())
                ret.add(info.getCommand());
            return ret;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<String> getParamValues(User uid, String command) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<OBDInfo> q = em.createNamedQuery("OBDInfo.findParameterValues", OBDInfo.class).setParameter("uid", uid).setParameter("command", command);
            List<String> ret = new ArrayList<>();
            for (OBDInfo info : q.getResultList())
                ret.add(info.getResult());
            return ret;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
