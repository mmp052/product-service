
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



@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Cacheable("products")
    public Product findById(String id) {
        return productRepository.findById(id).get().to();
    }

    public Product create(Product product) {

        return productRepository.save(new ProductModel(product)).to();
    }

    @Cacheable("products")
    public List<Product> findAll() {
        return StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .map(ProductModel::to)
                .toList();
    }

    public String deleteById(String id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        productRepository.delete(product);
        return id;
    }
}
