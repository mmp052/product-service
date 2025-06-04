package store.product;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;


@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public Product findById(String id) {
        System.out.println("Buscando produto por ID do banco de dados: " + id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id))
                .to();
    }

    // Ao criar um produto, precisamos:
    // 1. Adicionar/Atualizar o produto no cache (se findById for chamado para ele).
    // 2. Invalidar o cache de 'findAll' para que a lista completa seja recarregada na próxima vez.
    @CacheEvict(value = "products", allEntries = true) // Invalida o cache de findAll
    @CachePut(value = "products", key = "#result.id()") // Atualiza o cache para o produto específico, usando o ID do resultado
    public Product create(Product product) {
        // Log para depuração: indica quando o método realmente é chamado
        System.out.println("Criando novo produto: " + product.name()); // Usando o getter fluent
        // Certifique-se de que o ID do produto é gerado antes de salvar, se não for feito pelo construtor
        // ou pelo banco de dados automaticamente. Se o ID for gerado pelo banco,
        // o @CachePut pode precisar de um #result.id se o ID for definido após o save.
        // Assumindo que o ID já está no objeto 'product' ou é gerado e retornado por 'save'.
        Product savedProduct = productRepository.save(new ProductModel(product)).to();
        return savedProduct;
    }


    @Cacheable(value = "products", key = "'allProducts'")
    public List<Product> findAll() {

        System.out.println("Buscando todos os produtos do banco de dados.");
        return StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .map(ProductModel::to)
                .toList();
    }


    // Combinando as duas anotações @CacheEvict em uma só
    @CacheEvict(value = "products", allEntries = true) // Invalida o cache de todos os produtos e a lista 'allProducts'
    public String deleteById(String id) {

        System.out.println("Deletando produto com ID: " + id);
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for deletion with ID: " + id));
        productRepository.delete(product);
        return id;
    }
}