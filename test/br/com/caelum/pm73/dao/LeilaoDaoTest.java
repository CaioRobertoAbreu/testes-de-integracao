package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.builder.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LeilaoDaoTest {

    private Session session;
    private LeilaoDao leilaoDao;
    private UsuarioDao usuarioDao;
    private static Usuario usuario;
    private LeilaoBuilder leilaoBuilder;

    @BeforeAll
    public static void inicilizaUsuario() {
        usuario = new Usuario("Mauricio", "mauricio@medeiros.com");
    }

    @BeforeEach
    public void inicializa() {
        session = new CriadorDeSessao().getSession();
        session.beginTransaction();
        leilaoDao = new LeilaoDao(session);
        usuarioDao = new UsuarioDao(session);
        leilaoBuilder = new LeilaoBuilder();
    }

    @AfterEach
    public void closeTransaction() {
        session.getTransaction().rollback();
        session.close();
    }


    @Test
    public void deveRetornarQuantidadeLeiloesEncerrados() {
        //cenario
        Leilao ativo1 = leilaoBuilder.comNome("Geladeira").comValor(24000.).comDono(usuario).constroi();
        Leilao ativo2 = leilaoBuilder.comNome("Xbox").comValor(22000.).comDono(usuario).constroi();
        Leilao encerrado = leilaoBuilder.comNome("TV Ouro").comValor(700000.).comDono(usuario)
                .encerrado().constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(ativo1);
        leilaoDao.salvar(ativo2);
        leilaoDao.salvar(encerrado);

        //acao
        Long total = leilaoDao.total();

        //verificacao
        assertEquals(2, total);
    }

    @Test
    public void deveRetornar0ParaLeiloesNaoEncerrados() {
        //cenario
        Leilao encerrado = leilaoBuilder.comDono(usuario).encerrado().constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(encerrado);

        //acao
        Long total = leilaoDao.total();

        //verificacao
        assertEquals(0, total);
    }

    @Test
    public void deveRetornarLeiloesNaoUsados() {
        //cenario
        Leilao usado1 = leilaoBuilder.comNome("TV Ouro").comDono(usuario).usado().constroi();
        Leilao novo = leilaoBuilder.comNome("XBox Ultra Game Plus").comDono(usuario).constroi();
        novo.setUsado(false);

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(usado1);
        leilaoDao.salvar(novo);

        //acao
        List<Leilao> leiloesList = leilaoDao.novos();

        //verificacao
        assertEquals(1, leiloesList.size());
        assertEquals(novo.getNome(), leiloesList.get(0).getNome());
    }

    @Test
    public void deveRetornarLeiloesAntigos() {
        //cenario
        Leilao leilao1 = leilaoBuilder.comNome("TV Ouro").comDono(usuario).constroi();
        Leilao leilao2 = leilaoBuilder.comNome("XBox Ultra Game Plus").comDono(usuario).diasAtras(3).constroi();
        Leilao antigo = leilaoBuilder.comNome("Redmi note 9 Pro").comDono(usuario).diasAtras(10).constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);
        leilaoDao.salvar(antigo);

        //acao
        List<Leilao> leiloesList = leilaoDao.antigos();

        //verificacao
        assertEquals(1, leiloesList.size());
        assertEquals(antigo.getNome(), leiloesList.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

        // Cenario
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Leilao leilao1 = leilaoBuilder.diasAtras(2).comDono(usuario).constroi();
        Leilao leilao2 = leilaoBuilder.diasAtras(20).comDono(usuario).constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        //Acao
        List<Leilao> leiloes =
                leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // Verificacao
        assertEquals(1, leiloes.size());
        assertEquals("XBox", leiloes.get(0).getNome());
    }

    @Test
    public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {
        //Cenario
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();
        Calendar dataDoLeilao1 = Calendar.getInstance();
        dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);

        Leilao leilao1 = leilaoBuilder.diasAtras(2).comDono(usuario).encerrado().constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao1);

        // Acao
        List<Leilao> leiloes =
                leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // Verificacao
        assertEquals(0, leiloes.size());
    }

    @Test
    public void deveRetornarLeiloesDisputados() {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@aniche.com.br");

        Leilao leilao1 = new LeilaoBuilder()
                .comDono(usuario)
                .comValor(3000.0)
                .constroi();

        leilao1.getLances().add(new Lance(Calendar.getInstance(), usuario, 3000.0, leilao1));
        leilao1.getLances().add(new Lance(Calendar.getInstance(), mauricio, 3000.0, leilao1));

        Leilao leilao2 = new LeilaoBuilder()
                .comDono(mauricio)
                .comValor(3200.0)
                .constroi();

        leilao2.getLances().add(new Lance(Calendar.getInstance(), mauricio, 3000.0, leilao2));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), usuario, 3100.0, leilao2));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), mauricio, 3200.0, leilao2));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), usuario, 3300.0, leilao2));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), mauricio, 3400.0, leilao2));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), usuario, 3500.0, leilao2));

        usuarioDao.salvar(usuario);
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

        assertEquals(1, leiloes.size());
        assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);
    }

    @Test
    public void deveRetornarListaLeiloesDoUsuarioQueDeuPeloMenosUmLance() {
        Leilao leilao = leilaoBuilder.comDono(usuario).comNome("Notebook").constroi();
        Leilao leilao2 = leilaoBuilder.comDono(usuario).comNome("Playstation 5 ouro").constroi();

        leilao.getLances().add(new Lance(Calendar.getInstance(), usuario, 7000., leilao));
        leilao2.getLances().add(new Lance(Calendar.getInstance(), usuario, 8000, leilao2));

        Leilao leilao3 = leilaoBuilder.comNome("Geladeira").constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);
        leilaoDao.salvar(leilao3);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(usuario);

        assertEquals(2, leiloes.size());
    }

    @Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao(){
        Leilao leilao = leilaoBuilder.comDono(usuario).comNome("Notebook").constroi();
        Leilao leilao2 = leilaoBuilder.comDono(usuario).comNome("Playstation 5 ouro").constroi();

        leilao.getLances().add(new Lance(Calendar.getInstance(), usuario, 7000., leilao));
        leilao.getLances().add(new Lance(Calendar.getInstance(), usuario, 8000., leilao));

        leilao2.getLances().add(new Lance(Calendar.getInstance(), usuario, 6000, leilao2));

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(usuario);

        assertEquals(2, leiloes.size());
    }

    @Test
    public void deveDeletarUsuario() {

        usuarioDao.salvar(usuario);

        //Para enviar os comandos SQL criados até então para o banco de dados
        // e garantir que o banco os processou
        session.flush();
        session.clear();
        usuarioDao.deletar(usuario);
        Usuario deletado
                = usuarioDao.porNomeEEmail(usuario.getNome(), LeilaoDaoTest.usuario.getEmail());

        assertNull(deletado);
    }

    @Test
    public void deveDeletarLeilao() {
        Leilao leilao = leilaoBuilder
                .comNome("Leilao para ser deletado")
                .comDono(usuario)
                .constroi();

        Leilao leilao2 = leilaoBuilder
                .comNome("Leilao para ser deletado 2")
                .comDono(usuario)
                .constroi();

        usuarioDao.salvar(usuario);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        session.flush();
        session.clear();

        leilaoDao.deleta(leilao);
        Leilao leilaoEncontrado = leilaoDao.porId(2);

        assertEquals("Leilao para ser deletado 2", leilaoEncontrado.getNome());
    }

    @Test
    public void deveAlterarUsuario() {

        Usuario usuario = new Usuario("Caio Abreu", "caio@abreu.com");

        usuarioDao.salvar(usuario);
        session.flush();

        usuario.setNome("Joao Gabriel");
        usuario.setEmail("joao@gabriel.com");

        usuarioDao.atualizar(usuario);
        session.flush();

        Usuario usuarioEncontrado = usuarioDao.porId(1);

        assertEquals("Joao Gabriel", usuarioEncontrado.getNome());
        assertEquals("joao@gabriel.com", usuarioEncontrado.getEmail());


    }
}