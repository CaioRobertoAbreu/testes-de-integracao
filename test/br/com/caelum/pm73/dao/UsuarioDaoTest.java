package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsuarioDaoTest {

    private static Session session;
    private UsuarioDao usuarioDao;
    private Usuario usuario;

    @BeforeEach
    public void inicializa() {
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);
        session.beginTransaction();
    }

    @AfterAll
    public static void closeTransactional() {
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveEncontrarPeloNomeEEmail() {
        //cenario

        usuario = new Usuario("Caio Abreu", "caio@abreu.com");
        usuarioDao.salvar(usuario);

        //acao
        Usuario usuarioEncontrado = usuarioDao.porNomeEEmail(usuario.getNome(), usuario.getEmail());

        //verificacao
        assertEquals(usuario.getNome(), usuarioEncontrado.getNome());
        assertEquals(usuario.getEmail(), usuarioEncontrado.getEmail());
    }

    @Test
    public void deveRetornarNullParaUsuarioInexistente(){
        //cenario

        //acao
        Usuario usuarioEncontrado = usuarioDao.porNomeEEmail("Joao Abreu", "joao@teste.com");

        //verificacao
        assertNull(usuarioEncontrado);
    }

}