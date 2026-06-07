package com.vtea.service;

import com.vtea.dto.CategoryDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.model.Topping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class POSCacheService {

    private static final POSCacheService INSTANCE = new POSCacheService();

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();

    private List<ProductDTO> cachedProducts = new ArrayList<>();
    private List<CategoryDTO> cachedCategories = new ArrayList<>();
    private List<ProductDTO> cachedToppings = new ArrayList<>();

    private boolean loaded = false;

    private POSCacheService() {
    }

    public static POSCacheService getInstance() {
        return INSTANCE;
    }

    public synchronized void loadIfNeeded() {
        if (!loaded) {
            refresh();
        }
    }

    public synchronized void refresh() {
        cachedProducts = productService.getAllActiveProducts();
        cachedCategories = categoryService.getAllActiveCategories();

        cachedToppings = new ArrayList<>();

        for (ProductDTO p : cachedProducts) {
            if (p.getCategoryName() != null
                    && p.getCategoryName().toLowerCase().contains("topping")) {
                cachedToppings.add(p);
            }
        }

        if (cachedToppings.isEmpty()) {
            List<Topping> tList = orderService.getAllActiveToppings();

            for (Topping t : tList) {
                ProductDTO pd = new ProductDTO();
                pd.setProductId(t.getToppingId());
                pd.setName(t.getName());
                pd.setPrice(t.getPrice());
                pd.setCategoryName("Topping");
                pd.setImageUrl(t.getImageUrl());
                cachedToppings.add(pd);
            }
        }

        loaded = true;
    }

    public List<ProductDTO> getProducts() {
        loadIfNeeded();
        return new ArrayList<>(cachedProducts);
    }

    public List<CategoryDTO> getCategories() {
        loadIfNeeded();
        return new ArrayList<>(cachedCategories);
    }

    public List<ProductDTO> getToppings() {
        loadIfNeeded();
        return new ArrayList<>(cachedToppings);
    }

    public List<ProductDTO> getProductsByCategory(int categoryId) {
        loadIfNeeded();

        return cachedProducts.stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    public ProductDTO findToppingById(int toppingId) {
        loadIfNeeded();

        for (ProductDTO topping : cachedToppings) {
            if (topping.getProductId() == toppingId) {
                return topping;
            }
        }

        return null;
    }
}