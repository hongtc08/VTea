package com.vtea.service;

import com.vtea.dao.CustomerDAO;
import com.vtea.model.Customer;

public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public Customer findCustomerByPhone(String phoneNumber) {
        return customerDAO.getCustomerByPhone(phoneNumber);
    }

    public boolean createCustomer(Customer customer) {
        return customerDAO.insertCustomer(customer);
    }

    public boolean addRewardPoints(int customerId, int pointsToAdd) {
        if (customerId <= 0 || pointsToAdd <= 0) {
            return false;
        }

        return customerDAO.updateRewardPoints(customerId, pointsToAdd);
    }
}