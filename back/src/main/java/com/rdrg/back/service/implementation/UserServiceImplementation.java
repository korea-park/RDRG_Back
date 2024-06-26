package com.rdrg.back.service.implementation;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rdrg.back.dto.request.user.ChangePasswordRequestDto;
import com.rdrg.back.dto.response.ResponseDto;
import com.rdrg.back.dto.response.user.GetSignInUserResponseDto;
import com.rdrg.back.dto.response.user.GetUserInfoResponseDto;
import com.rdrg.back.entity.UserEntity;
import com.rdrg.back.repository.UserRepository;
import com.rdrg.back.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    

    @Override
    public ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String userId) {
        
        // try {
        //     userEntity = userRepository.findByUserId(userId);
        //     if (userEntity == null) return ResponseDto.authenticationFailed();
            
        // } catch (Exception exception) {
        //     exception.printStackTrace();
        //     return ResponseDto.databaseError();
        // }

        UserEntity userEntity = findUserById(userId);
        if (userEntity == null) return ResponseDto.authenticationFailed();
        return GetSignInUserResponseDto.success(userEntity);    
    }

    @Override
    public ResponseEntity<? super GetUserInfoResponseDto> getUserInfo(String userId) {

        // try {
        //     UserEntity userEntity = userRepository.findByUserId(userId);
        //     if (userEntity == null) return ResponseDto.authenticationFailed();

        //     return GetUserInfoResponseDto.success(userEntity);
        // } catch (Exception exception) {
        //     exception.printStackTrace();
        //     return ResponseDto.databaseError();
        // }

        UserEntity userEntity = findUserById(userId);
        if (userEntity == null) return ResponseDto.authenticationFailed();
        return GetUserInfoResponseDto.success(userEntity);
    }

    @Override
    public ResponseEntity<ResponseDto> changePassword(ChangePasswordRequestDto dto) {
        
        // try {
        //     String userId = dto.getUserId();
        //     String userPassword = dto.getUserPassword();
        //     String newPassword = dto.getNewUserPassword();
        //     UserEntity userEntity = userRepository.findByUserId(userId);
        //     if (userEntity == null) return ResponseDto.authenticationFailed();

        //     String encodedPassword = userEntity.getUserPassword();
        //     boolean isMatched = passwordEncoder.matches(userPassword, encodedPassword);
        //     if (!isMatched) return ResponseDto.passwordChangeFailed();

        //     boolean isNewMatched = passwordEncoder.matches(newPassword, encodedPassword);
        //     if (isNewMatched) return ResponseDto.passwordChangeFailed();

        //     String encodedNewPassword = passwordEncoder.encode(newPassword);
        //     userEntity.setUserPassword(encodedNewPassword);
        //     userRepository.save(userEntity);
        // } catch (Exception exception) {
        //     exception.printStackTrace();
        //     return ResponseDto.databaseError();
        // }

        UserEntity userEntity = findUserById(dto.getUserId());
        if (userEntity == null) {
            return ResponseDto.authenticationFailed();
        }

        String userPassword = dto.getUserPassword();
        String newPassword = dto.getNewUserPassword();
        String encodedPassword = userEntity.getUserPassword();

        boolean isMatched = passwordEncoder.matches(userPassword, encodedPassword);
        if (!isMatched || passwordEncoder.matches(newPassword, encodedPassword)) {
            return ResponseDto.passwordChangeFailed();
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userEntity.setUserPassword(encodedNewPassword);
        userRepository.save(userEntity);

        return ResponseDto.success();
    }

    @Override
    public ResponseEntity<ResponseDto> deleteUser(String userId) {
        
        // try {
        //     UserEntity userEntity = userRepository.findByUserId(userId);
        //     if (userEntity == null) return ResponseDto.authenticationFailed();
            
        //     userRepository.delete(userEntity);
        // } catch (Exception exception) {
        //     exception.printStackTrace();
        //     return ResponseDto.databaseError();
        // }

        UserEntity userEntity = findUserById(userId);
        if (userEntity == null) return ResponseDto.authenticationFailed();

        userRepository.delete(userEntity);

        invalidateUserSession();
        updateUserCache(userId);
        return ResponseDto.success();
    }
    

    private void invalidateUserSession (){
        SecurityContextHolder.clearContext();
    }

    @CacheEvict(key = "#userId")
    private void updateUserCache(String userId) {
    }

    private UserEntity findUserById(String userId) {
        try {
            UserEntity userEntity = userRepository.findByUserId(userId);
            return userEntity;

        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
