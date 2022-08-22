/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.exceptions.NonexistentEntityException;

/**
 *
 * @author miri
 */
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Persona idpersonau = usuario.getIdpersonaUsu();
            if (idpersonau != null) {
                idpersonau = em.getReference(idpersonau.getClass(), idpersonau.getIdpersona());
                usuario.setIdpersonaUsu(idpersonau);
            }
            em.persist(usuario);
            if (idpersonau != null) {
                idpersonau.getUsuarioList().add(usuario);
                idpersonau = em.merge(idpersonau);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuario usuario) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getIdusuario());
            Persona idpersonauOld = persistentUsuario.getIdpersonaUsu();
            Persona idpersonauNew = usuario.getIdpersonaUsu();
            if (idpersonauNew != null) {
                idpersonauNew = em.getReference(idpersonauNew.getClass(), idpersonauNew.getIdpersona());
                usuario.setIdpersonaUsu(idpersonauNew);
            }
            usuario = em.merge(usuario);
            if (idpersonauOld != null && !idpersonauOld.equals(idpersonauNew)) {
                idpersonauOld.getUsuarioList().remove(usuario);
                idpersonauOld = em.merge(idpersonauOld);
            }
            if (idpersonauNew != null && !idpersonauNew.equals(idpersonauOld)) {
                idpersonauNew.getUsuarioList().add(usuario);
                idpersonauNew = em.merge(idpersonauNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = usuario.getIdusuario();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getIdusuario();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            Persona idpersonau = usuario.getIdpersonaUsu();
            if (idpersonau != null) {
                idpersonau.getUsuarioList().remove(usuario);
                idpersonau = em.merge(idpersonau);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Usuario findUsuario(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    public Usuario buscarUsuario(String usuario,String clave){
        Usuario u = null;
        for(Usuario user:findUsuarioEntities()){
            if(user.getUsuarioUsu().equals(usuario)&&user.getClaveUsu().equals(clave)){
                u=user;
            }
        }
        return u;
    }
    
    public List<Usuario> buscarUSU(String nombre) {
        System.out.println(nombre);
        EntityManager em = getEntityManager();
        try {
            //Para realizar consultas 
            TypedQuery<Usuario> query = em.createNamedQuery("Usuario.findByUsuarioUsu", Usuario.class);
            query.setParameter("usuarioUsu", nombre);
            List<Usuario> list = query.getResultList();
            return list;
        } finally {
            em.close();
        }
    }
}
