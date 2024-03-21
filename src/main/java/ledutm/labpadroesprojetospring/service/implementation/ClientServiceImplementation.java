package ledutm.labpadroesprojetospring.service.implementation;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ledutm.labpadroesprojetospring.model.Client;
import ledutm.labpadroesprojetospring.model.ClientRepository;
import ledutm.labpadroesprojetospring.model.Endereco;
import ledutm.labpadroesprojetospring.model.EnderecoRepository;
import ledutm.labpadroesprojetospring.service.ClientService;
import ledutm.labpadroesprojetospring.service.CepService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClientServiceImplementation implements ClientService {

	// Singleton: Injetar os componentes do Spring com @Autowired.
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private CepService CepService;
	
	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Client> buscarTodos() {
		// Buscar todos os Clientes.
		return clientRepository.findAll();
	}

	@Override
	public Client buscarPorId(Long id) {
		// Buscar Cliente por ID.
		Optional<Client> cliente = clientRepository.findById(id);
		return cliente.get();
	}

	@Override
	public void inserir(Client client) {
		salvarClienteComCep(client);
	}

	@Override
	public void atualizar(Long id, Client client) {
		// Buscar Cliente por ID, caso exista:
		Optional<Client> clientBd = clientRepository.findById(id);
		if (clientBd.isPresent()) {
			salvarClienteComCep(client);
		}
	}

	@Override
	public void deletar(Long id) {
		// Deletar Cliente por ID.
		clientRepository.deleteById(id);
	}

	private void salvarClienteComCep(Client client) {
		// Verificar se o Endereco do Cliente já existe (pelo CEP).
		String cep = client.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			// Caso não exista, integrar com o ViaCEP e persistir o retorno.
			Endereco novoEndereco = CepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});
		client.setEndereco(endereco);
		// Inserir Cliente, vinculando o Endereco (novo ou existente).
		clientRepository.save(client);
	}

}