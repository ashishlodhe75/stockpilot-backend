package com.stockpilot.product.service;

import com.stockpilot.product.dto.ProductPageResponse;
import com.stockpilot.product.dto.ProductRequest;
import com.stockpilot.product.dto.ProductResponse;
import com.stockpilot.product.entity.Product;
import com.stockpilot.product.exception.DuplicateSkuException;
import com.stockpilot.product.exception.InvalidRequestException;
import com.stockpilot.product.exception.ResourceNotFoundException;
import com.stockpilot.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "sku", "price", "quantity", "createdAt", "updatedAt"
    );

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateSkuException(sku);
        }

        Product product = new Product(
                request.name().trim(),
                sku,
                normalizeDescription(request.description()),
                request.price(),
                request.quantity()
        );

        return toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findProductOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductPageResponse getProducts(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (page < 0) {
            throw new InvalidRequestException("Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new InvalidRequestException("Page size must be between 1 and 100");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidRequestException("Unsupported sort field: " + sortBy);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Sort direction must be ASC or DESC");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Product> result;

        if (search == null || search.isBlank()) {
            result = productRepository.findAll(pageable);
        } else {
            String term = search.trim();
            result = productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                    term,
                    term,
                    pageable
            );
        }

        return new ProductPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        String newSku = normalizeSku(request.sku());

        if (!product.getSku().equals(newSku) && productRepository.existsBySku(newSku)) {
            throw new DuplicateSkuException(newSku);
        }

        product.updateDetails(
                request.name().trim(),
                newSku,
                normalizeDescription(request.description()),
                request.price(),
                request.quantity()
        );

        return toResponse(productRepository.saveAndFlush(product));
    }

    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
