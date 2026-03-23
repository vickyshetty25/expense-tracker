package com.vicky.expense_tracker.service;

import com.vicky.expense_tracker.model.Category;
import com.vicky.expense_tracker.model.User;
import com.vicky.expense_tracker.repository.CategoryRepository;
import com.vicky.expense_tracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Category> getCategories() {
        return categoryRepository.findByUserId(getLoggedInUser().getId());
    }

    public Category createCategory(String name) {
        User user = getLoggedInUser();

        Category category = new Category();
        category.setName(name);
        category.setUser(user);

        return categoryRepository.save(category);
    }
}