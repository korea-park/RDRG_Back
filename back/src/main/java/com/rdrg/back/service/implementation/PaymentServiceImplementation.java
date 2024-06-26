package com.rdrg.back.service.implementation;

import java.util.List;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rdrg.back.common.object.AdminRentItem;
import com.rdrg.back.common.object.KakaoReady;
import com.rdrg.back.common.object.RentItem;
import com.rdrg.back.common.util.KakaoPayUtil;
import com.rdrg.back.dto.request.payment.PatchRentStatusResponseDto;
import com.rdrg.back.dto.request.payment.PostPaymentRequestDto;
import com.rdrg.back.dto.response.ResponseDto;
import com.rdrg.back.dto.response.payment.GetAdminPaymentListResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentDetailListResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentListResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentResponseDto;
import com.rdrg.back.dto.response.payment.GetSearchAdminPaymentListResponseDto;
import com.rdrg.back.dto.response.payment.PostPaymentResponseDto;
import com.rdrg.back.entity.DeviceEntity;
import com.rdrg.back.entity.DeviceRentStatusEntity;
import com.rdrg.back.entity.RentDetailEntity;
import com.rdrg.back.entity.UserEntity;
import com.rdrg.back.repository.DeviceRepository;
import com.rdrg.back.repository.PaymentRepository;
import com.rdrg.back.repository.RentDetailRepository;
import com.rdrg.back.repository.UserRepository;
import com.rdrg.back.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImplementation implements PaymentService {
    private final KakaoPayUtil kakaoPayUtil;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PaymentRepository paymentRepository;
    private final RentDetailRepository rentDetailRepository;
    
    @Override
    public ResponseEntity<? super GetPaymentResponseDto> getPayment(String rentUserId) {

        try {
            boolean isExistUser = userRepository.existsByUserId(rentUserId);
            if(!isExistUser) return ResponseDto.authenticationFailed();

            DeviceRentStatusEntity reservations = paymentRepository.findTop1ByRentUserIdOrderByRentNumberDesc(rentUserId);
            if(reservations == null) return ResponseDto.notFound();

            return GetPaymentResponseDto.success(reservations);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetPaymentListResponseDto> getAdminPaymentList() {
        
        try {
            List<DeviceRentStatusEntity> deviceRentStatusEntities = paymentRepository.findByOrderByRentNumberDesc();
            List<AdminRentItem> adminRentList = new ArrayList<>();

            for (DeviceRentStatusEntity deviceRentStatusEntity: deviceRentStatusEntities) {
                // Integer rentNumber =  deviceRentStatusEntity.getRentNumber();
                // List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);
                // AdminRentItem adminRentItem = new AdminRentItem(deviceRentStatusEntity, rentDetailEntities);
                // adminRentList.add(adminRentItem);

                AdminRentItem adminRentItem = createRentItem(deviceRentStatusEntity, AdminRentItem.class);
                adminRentList.add(adminRentItem);
            }
            return GetAdminPaymentListResponseDto.success(adminRentList);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetPaymentListResponseDto> getPaymentList(String rentUserId) {

        try {
            boolean isExistUser = userRepository.existsByUserId(rentUserId);
            if(!isExistUser) return ResponseDto.authenticationFailed();

            List<DeviceRentStatusEntity> deviceRentStatusEntities = paymentRepository.findByRentUserIdOrderByRentNumberDesc(rentUserId);
            List<RentItem> rentList = new ArrayList<>();

            for (DeviceRentStatusEntity deviceRentStatusEntity: deviceRentStatusEntities) {
                // Integer rentNumber =  deviceRentStatusEntity.getRentNumber();
                // List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);
                // RentItem rentItem = new RentItem(deviceRentStatusEntity, rentDetailEntities);
                // rentList.add(rentItem);

                RentItem rentItem = createRentItem(deviceRentStatusEntity, RentItem.class);
                rentList.add(rentItem);
            }
            return GetPaymentListResponseDto.success(rentList);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetSearchAdminPaymentListResponseDto> getSearchAdminPaymentList(String searchWord) {
        
        try {
            List<DeviceRentStatusEntity> deviceRentStatusEntities = paymentRepository.findByRentUserIdOrderByRentNumberDesc(searchWord);
            List<AdminRentItem> adminRentList = new ArrayList<>();
            
            for (DeviceRentStatusEntity deviceRentStatusEntity: deviceRentStatusEntities) {
                // Integer rentNumber =  deviceRentStatusEntity.getRentNumber();
                // List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);
                // AdminRentItem adminRentItem = new AdminRentItem(deviceRentStatusEntity, rentDetailEntities);

                AdminRentItem adminRentItem = createRentItem(deviceRentStatusEntity, AdminRentItem.class);
                adminRentList.add(adminRentItem);
            }
            return GetSearchAdminPaymentListResponseDto.success(adminRentList);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetPaymentDetailListResponseDto> getPaymentDetailList(String rentUserId, int rentNumber) {
        try {
            UserEntity userEntity = userRepository.findByUserId(rentUserId);
            if (userEntity == null) return ResponseDto.noExistUserId();

            DeviceRentStatusEntity deviceRentStatusEntity = paymentRepository.findByRentNumber(rentNumber);
            List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);

            return GetPaymentDetailListResponseDto.success(deviceRentStatusEntity, rentDetailEntities);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super PostPaymentResponseDto> postPayment(PostPaymentRequestDto dto, String userId) {

        KakaoReady kakaoReady = null;
        
        try {
            boolean isExistUser = userRepository.existsByUserId(userId);
            if (!isExistUser) return ResponseDto.authenticationFailed();

            DeviceRentStatusEntity deviceRentStatusEntity = new DeviceRentStatusEntity(dto, userId);
            paymentRepository.save(deviceRentStatusEntity);

            List<String> rentSerialNumbers = dto.getRentSerialNumber();
            List<DeviceEntity> deviceEntities = deviceRepository.findAllById(rentSerialNumbers);
            
            List<RentDetailEntity> rentDetailEntities = new ArrayList<>();
            Integer rentNumber =  deviceRentStatusEntity.getRentNumber();

            for (DeviceEntity deviceEntity: deviceEntities) {
                RentDetailEntity rentDetailEntity = new RentDetailEntity(rentNumber, deviceEntity);
                rentDetailEntities.add(rentDetailEntity);
            }

            rentDetailRepository.saveAll(rentDetailEntities);
            
            kakaoReady = kakaoPayUtil.prepareKakaoPayment(dto, rentNumber);

            deviceRentStatusEntity.setRentStatus("결제 완료");
            paymentRepository.save(deviceRentStatusEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return PostPaymentResponseDto.success(kakaoReady);
    }

    @Override
    public ResponseEntity<ResponseDto> patchRentStatus(int rentNumber, PatchRentStatusResponseDto patchRentStatusResponseDto ) {

        try {
            DeviceRentStatusEntity deviceRentStatusEntity = paymentRepository.findByRentNumber(rentNumber);
            if (deviceRentStatusEntity == null) return ResponseDto.noExistRentDetail();

            deviceRentStatusEntity.setRentStatus(patchRentStatusResponseDto.getRentStatus());

            if("반납 완료".equals(patchRentStatusResponseDto.getRentStatus())){
            //     String returnPlace = deviceRentStatusEntity.getRentReturnPlace();
            //     List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);
            //     for (RentDetailEntity rentDetailEntity : rentDetailEntities) {
            //         String serialNumber = rentDetailEntity.getSerialNumber();
            //         DeviceEntity deviceEntity = deviceRepository.findBySerialNumber(serialNumber);
            //         if (deviceEntity != null) {
            //             deviceEntity.setPlace(returnPlace);
            //             deviceRepository.save(deviceEntity);
            //         }
            //     }
            // }
                changePlace(deviceRentStatusEntity, rentNumber);
            }

            paymentRepository.save(deviceRentStatusEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return ResponseDto.success();
    }

    @Override
    public ResponseEntity<ResponseDto> deletePayment(int rentNumber, String userId) {

        try {
            DeviceRentStatusEntity deviceRentStatusEntity = paymentRepository.findByRentNumber(rentNumber);
            if (deviceRentStatusEntity == null) return ResponseDto.noExistRentDetail();

            boolean isEqual = deviceRentStatusEntity.getRentUserId().equals(userId);
            if (!isEqual) return ResponseDto.authorizationFailed();

            paymentRepository.delete(deviceRentStatusEntity);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return ResponseDto.success();
    }    

    
    private <T> T createRentItem(DeviceRentStatusEntity deviceRentStatusEntity, Class<T> clazz) throws Exception {

        Integer rentNumber = deviceRentStatusEntity.getRentNumber();
        List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);

        if (clazz == RentItem.class) {
            return clazz.cast(new RentItem(deviceRentStatusEntity, rentDetailEntities));
        } else if (clazz == AdminRentItem.class) {
            return clazz.cast(new AdminRentItem(deviceRentStatusEntity, rentDetailEntities));
        }
        
        throw new IllegalArgumentException(clazz.getName());
    }

    private void changePlace(DeviceRentStatusEntity deviceRentStatusEntity, Integer rentNumber){

        String returnPlace = deviceRentStatusEntity.getRentReturnPlace();
        List<RentDetailEntity> rentDetailEntities = rentDetailRepository.findByRentNumber(rentNumber);

        for (RentDetailEntity rentDetailEntity : rentDetailEntities) {
            String serialNumber = rentDetailEntity.getSerialNumber();
            DeviceEntity deviceEntity = deviceRepository.findBySerialNumber(serialNumber);
            if (deviceEntity != null) {
                deviceEntity.setPlace(returnPlace);
                deviceRepository.save(deviceEntity);
            }
        }
    }
}
