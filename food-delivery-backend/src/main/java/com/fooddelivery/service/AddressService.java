package com.fooddelivery.service;

import com.fooddelivery.dto.request.ShoppingRequests.AddressRequest;
import com.fooddelivery.dto.response.ShoppingResponses.AddressResponse;
import com.fooddelivery.entity.Address;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.AddressRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public static String format(Address a) {
        StringBuilder sb = new StringBuilder(a.getLine1());
        if (a.getLine2() != null && !a.getLine2().isBlank()) {
            sb.append(", ").append(a.getLine2());
        }
        sb.append(", ").append(a.getCity()).append(", ").append(a.getState()).append(" - ").append(a.getPincode());
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId).stream()
                .map(a -> AddressResponse.from(a, format(a))).toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest req) {
        List<Address> existing = addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId);
        boolean makeDefault = Boolean.TRUE.equals(req.defaultAddress()) || existing.isEmpty();
        if (makeDefault) {
            existing.forEach(a -> a.setDefaultAddress(false));
        }
        Address a = new Address();
        a.setUser(userRepository.getReferenceById(userId));
        a.setLabel(req.label());
        a.setLine1(req.line1().trim());
        a.setLine2(req.line2());
        a.setCity(req.city().trim());
        a.setState(req.state().trim());
        a.setPincode(req.pincode());
        a.setDefaultAddress(makeDefault);
        a = addressRepository.save(a);
        return AddressResponse.from(a, format(a));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address a = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepository.delete(a);
    }
}
