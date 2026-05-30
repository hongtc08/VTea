package com.vtea.service;

import com.vtea.dao.CustomerDAO;
import com.vtea.dto.CustomerDTO;
import com.vtea.model.Customer;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public CustomerDTO findCustomerByPhone(String phoneNumber) {
        return customerDAO.getCustomerByPhone(phoneNumber);
    }

    public boolean createCustomer(Customer customer) {
        return customerDAO.insertCustomer(customer);
    }

}