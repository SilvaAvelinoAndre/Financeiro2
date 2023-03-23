package br.com.financeiro.bean;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.omnifaces.util.Messages;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import br.com.financeiro.dao.FornecedorDAO;
import br.com.financeiro.dao.ProdutoDAO;
import br.com.financeiro.dominio.Fornecedor;
import br.com.financeiro.dominio.Produto;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ProdutoBean implements Serializable {
	private Produto  produto;
	private List<Produto> produtos;
	private List<Fornecedor> fornecedor;
	
	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public List<Produto> getProdutos() {
		return produtos;
	}

	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}

	public List<Fornecedor> getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(List<Fornecedor> fornecedor) {
		this.fornecedor = fornecedor;
	}

	@PostConstruct
	public void listar() {
		try {
			ProdutoDAO produtoDAO = new ProdutoDAO();
			produtos = produtoDAO.listar();

			
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar listar os produtos");
			erro.printStackTrace();
		}
	}
	
	public void novo() {
		try {
			produto = new Produto();

			FornecedorDAO fabricanteDAO = new FornecedorDAO();
			fornecedor = fabricanteDAO.listar();
		} catch (RuntimeException erro) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar gerar um novo produto");
			erro.printStackTrace();
		}
	}
	
	public void editar(ActionEvent evento){
		try {
			produto = (Produto) evento.getComponent().getAttributes().get("produtoSelecionado");

			//Código da erro se não cadastrar uma imagem se ela já não existir , porém permite a edição de outros componentes
			produto.setCaminho("C:/Uploads Imagens/" + produto.getCodigo() + ".jpg");
			
			FornecedorDAO fornecedorDAO = new FornecedorDAO();
			fornecedor = fornecedorDAO.listar();
		} catch (RuntimeException erro) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar selecionar um produto");
			erro.printStackTrace();
		}	
	}
	
	public void salvar() {
		try {
			//validação para obrigar o usuario cadastrar uma imagem
			if(produto.getCaminho() == null) {
				Messages.addGlobalInfo("Por favor cadastre uma imagem!!");
				return; // faz sair do metodo salvar se entrar nes if.
			}
			
			ProdutoDAO produtoDAO = new ProdutoDAO();
			Produto produtoRetorno = produtoDAO.merge(produto);

			Path origem = Paths.get(produto.getCaminho());
			Path destino = Paths.get("C:/Uploads Imagens/" 
			+ produtoRetorno.getCodigo() + ".jpg");
			Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
			
			produto = new Produto();

			FornecedorDAO fornecedorDAO = new FornecedorDAO();
			fornecedor = fornecedorDAO.listar();

			produtos = produtoDAO.listar();

			Messages.addGlobalInfo("Produto salvo com sucesso");
		} catch (RuntimeException | IOException erro) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar salvar o produto");
			erro.printStackTrace();
		}
	}

	public void excluir(ActionEvent evento) {
		try {
			produto = (Produto) evento.getComponent().getAttributes().get("produtoSelecionado");

			ProdutoDAO produtoDAO = new ProdutoDAO();
			produtoDAO.excluir(produto);
			Path arquivo = Paths.get("C:/Uploads Imagens/" + produto.getCodigo()+ ".jpg");
			Files.deleteIfExists(arquivo);
			
			produtos = produtoDAO.listar();

			Messages.addGlobalInfo("Produto removido com sucesso");
		} catch (RuntimeException |IOException erro) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar remover o produto");
			erro.printStackTrace();
		}
	}
	
	
	public void upload(FileUploadEvent evento) {
		try {
		UploadedFile arquivoUpload = evento.getFile();
		
		
		Path arquivoTemporario = Files.createTempFile(null, null);
		Files.copy(arquivoUpload.getInputstream(), arquivoTemporario, StandardCopyOption.REPLACE_EXISTING);
		produto.setCaminho(arquivoTemporario.toString());
		Messages.addGlobalInfo("Imagem salva com sucesso");
		
		} catch (IOException e) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar carregar a imagem!");
			e.printStackTrace();
		}
		
	}
}
