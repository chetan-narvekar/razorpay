package com.razorpay.app.merchant.service.impl;

import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.merchant.entity.Customer;
import com.razorpay.app.merchant.entity.Merchant;
import com.razorpay.app.merchant.repository.CustomerRepository;
import com.razorpay.app.merchant.repository.MerchantRepository;
import com.razorpay.app.merchant.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public UUID findOrCreate(UUID merchantId, String email, String name, String phone) {
       if( email == null || email.isBlank()){
           return  null;
       }

        return customerRepository.findByMerchant_IdAndEmail(merchantId, email).map(Customer::getId).orElseGet(()-> createNew(merchantId, email , name, phone));
    }

    private UUID createNew(UUID merchantId, String email, String name, String phone) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        Customer customer = Customer.builder()
                .merchant(merchant)
                .email(email)
                .name(name)
                .phone(phone)
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created via findOrCreate id={} merchantId={} email={}",
                customer.getId(), merchantId, email);
        return customer.getId();
    }

}
