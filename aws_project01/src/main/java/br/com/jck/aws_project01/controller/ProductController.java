package br.com.jck.aws_project01.controller;

import br.com.jck.aws_project01.enums.EventType;
import br.com.jck.aws_project01.model.Product;
import br.com.jck.aws_project01.repository.ProductRepository;
import br.com.jck.aws_project01.request.NewProduct;
import br.com.jck.aws_project01.request.UpdateProduct;
import br.com.jck.aws_project01.service.ProductSnsPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductRepository productRepository;
    private ProductSnsPublisher productSnsPublisher;

    @Autowired
    public ProductController(ProductRepository productRepository, ProductSnsPublisher productSnsPublisher){
        this.productRepository = productRepository;
        this.productSnsPublisher = productSnsPublisher;
    }

    @GetMapping
    public Iterable<Product> findAll(){
        return  productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable long id){
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isPresent()){
            return new ResponseEntity<Product>(optionalProduct.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody NewProduct newProduct) {
        Product entity = new Product();
        entity.setName(newProduct.getName());
        entity.setCode(newProduct.getCode());
        entity.setModel(newProduct.getModel());
        entity.setPrice(newProduct.getPrice());
        entity = productRepository.save(entity);
        productSnsPublisher.publishProductEvent(entity, EventType.PRODUCT_CREATED, "João");
        return new ResponseEntity<Product>(entity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProduct requestDto, @PathVariable long id){
        Product entity = new Product();
        entity.setName(requestDto.getName());
        entity.setCode(requestDto.getCode());
        entity.setModel(requestDto.getModel());
        entity.setPrice(requestDto.getPrice());
        if(productRepository.existsById(id)){
            entity.setId(id);
            Product productUpdated = productRepository.save(entity);
            productSnsPublisher.publishProductEvent(productUpdated, EventType.PRODUCT_UPDATE, "Izadora");
            return new ResponseEntity<Product>(productUpdated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable long id){
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            productRepository.delete(product);
            productSnsPublisher.publishProductEvent(product, EventType.PRODUCT_UPDATE, "Martinha");
            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/bycode")
    public ResponseEntity<Product> findByCode(@RequestParam String code){
        Optional<Product> optProduct = productRepository.findByCode(code);
        if (optProduct.isPresent()){
            return new ResponseEntity<Product>(optProduct.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
