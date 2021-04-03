package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsuarioDaoTest {

    @Test
    public void deveEncontrarPeloNomeEEmail() {
        //Cenario
        Session session = mock(Session.class);
        Query query = Mockito.mock(Query.class);
        UsuarioDao usuarioDao = new UsuarioDao(session);

        Usuario usuario = new Usuario("Joao da Silva", "joao@dasilva.com");
        String sql = "from Usuario u where u.nome = :nome and u.email = :email";
        session.save(usuario);

        //Acao
        when(session.createQuery(sql)).thenReturn(query);
        when(query.setParameter("nome", "Joao da Silva")).thenReturn(query);
        when(query.setParameter("email", "joao@dasilva.com")).thenReturn(query);
        when(query.uniqueResult()).thenReturn(usuario);

        usuarioDao.porNomeEEmail("Joao da Silva", "joao@dasilva.com");
        //Verificacao
        Assertions.assertEquals("Joao da Silva", usuario.getNome());
        Assertions.assertEquals("joao@dasilva.com", usuario.getEmail());

        session.close();

    }

}